package com.mruhwedel

import com.mruhwedel.InfluxDbFactory.DatabaseConfig
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration
import java.time.ZonedDateTime

import static com.mruhwedel.SensorTestData.*
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static java.util.stream.Collectors.toList

@MicronautTest(environments = ['functional-test'])
class SensorAPIFunctionalSpec extends Specification {

    @Inject
    @Client("/api/v1/sensors/")
    HttpClient client;

    @Inject
    InfluxDB influxDB

    @Inject
    DatabaseConfig databaseConfig

    def setup() {
        wipeDatabase()
    }

    // Status & Collecting sensor measurements
    def '404-Not Found: when nothing is recorded'() {
        when:
        client.toBlocking().exchange(ANY_UUID)

        then:
        def e = thrown HttpClientResponseException
        e.status == NOT_FOUND
    }

    def 'OK: after a measurement below threshold is recorded'() {
        when:
        collectMeasurement(createBelowThreshold())

        then:
        status == 'OK'
    }

    def 'WARN: after a measurement above is recorded'() {
        when:
        collectMeasurement(createAboveThreshold())

        then:
        status == 'WARN'
    }

    def 'WARN: after two above threshold'() {
        given:
        def now = ZonedDateTime.now()

        when:
        collectMeasurement(createAboveThreshold(now))
        collectMeasurement(createAboveThreshold(now.plusMinutes(1)))

        then:
        status == 'WARN'
    }

    def 'ALERT: after three above threshold'() {
        given:
        def now = ZonedDateTime.now()

        when:
        collectMeasurement(createAboveThreshold(now))
        collectMeasurement(createAboveThreshold(now.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(now.plusMinutes(2)))

        then:
        status == 'ALERT'
    }

    def 'ALERT: after three above threshold & one below'() {
        given:
        def now = ZonedDateTime.now()

        when: 'an alert is triggered'
        collectMeasurement(createAboveThreshold(now))
        collectMeasurement(createAboveThreshold(now.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(now.plusMinutes(2)))

        and: 'measurement is back to normal'
        collectMeasurement(createBelowThreshold(now.plusMinutes(3)))

        then:
        status == 'ALERT'
    }

    def 'ALERT: after three above threshold & two below'() {
        given:
        def now = ZonedDateTime.now()

        when: 'an alert is triggered'
        collectMeasurement(createAboveThreshold(now))
        collectMeasurement(createAboveThreshold(now.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(now.plusMinutes(2)))

        and: 'measurement is back to normal'
        collectMeasurement(createBelowThreshold(now.plusMinutes(3)))
        collectMeasurement(createBelowThreshold(now.plusMinutes(4)))

        then:
        status == 'ALERT'
    }

    def 'OK: after three above threshold & three below'() {
        given:
        def now = ZonedDateTime.now()

        when: 'an alert is triggered'
        collectMeasurement(createAboveThreshold(now))
        collectMeasurement(createAboveThreshold(now.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(now.plusMinutes(2)))

        and: 'measurement is back to normal'
        collectMeasurement(createBelowThreshold(now.plusMinutes(3)))
        collectMeasurement(createBelowThreshold(now.plusMinutes(4)))
        collectMeasurement(createBelowThreshold(now.plusMinutes(5)))

        then:
        status == 'OK'
    }
    //END: Status & Collecting sensor measurements

    // Sensor Metrics
    def 'Metrics: 404-Not Found: when nothing is recorded'() {
        when:
        readMetrics(ANY_UUID)

        then:
        def e = thrown HttpClientResponseException
        e.status == NOT_FOUND
    }

    private SensorMetrics readMetrics(uuid) {
        client.toBlocking()
                .exchange("$uuid/metrics", SensorMetrics)
                .body()
    }

    def 'will return the maximum & average of the last 30 days'() {
        given:
        def measurements = generateRandomMeasurementsForA30DayWindow()
        def expectedMax = measurements.stream()
                .mapToInt(Measurement::getCo2)
                .max()
                .orElseThrow()

        int expectedAvg = Math.round(
                measurements.stream()
                        .mapToInt(Measurement::getCo2)
                        .average()
                        .orElseThrow()
        )

        when: 'all the samples have collected by the API '
        measurements.forEach(this::collectMeasurement)

        and: 'the metrics are read'
        def metrics = readMetrics(ANY_UUID)

        then:
        metrics == new SensorMetrics(
                expectedMax,
                expectedAvg
        )

    }

    private static List<Measurement> generateRandomMeasurementsForA30DayWindow() {
        def sampleSize = 200
        def averagingWindow = Duration.ofDays(30)

        def beginningOfPeriodToAverage = ZonedDateTime.now().minusDays(averagingWindow.toDays())
        def random = new Random(123)
        (1..sampleSize).stream()
                .map(i -> {
                    def randomCo2Level = Math.abs(random.nextInt(8000))
                    def randomMinutes = i
                    createMeasurement(
                            randomCo2Level,
                            beginningOfPeriodToAverage.plusMinutes(randomMinutes)
                    )
                }
                )
                .collect(toList())
    }

    private HttpResponse<Object> collectMeasurement(Measurement measurements) {
        client.toBlocking().exchange(POST("$ANY_UUID/measurements", measurements))
    }

    String getStatus() {
        client.toBlocking()
                .exchange(ANY_UUID, SensorAPI.StatusDto)
                .body()
                .status
    }

    private void wipeDatabase() {
        def database = databaseConfig.database
        influxDB.query(
                new Query("""
                         drop database $database;
                         create database $database
                         """
                )
        )
    }

}
