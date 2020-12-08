package com.mruhwedel.domain

import com.mruhwedel.application.InfluxDbFactory.DatabaseConfig
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration

import static com.mruhwedel.domain.SensorStatus.*
import static com.mruhwedel.domain.SensorTestData.*
import static java.util.stream.Collectors.toList

@MicronautTest(environments = ['functional-test'])
class SensorAPIFunctionalSpec extends Specification {

    @Inject
    SensorApiClient client

    @Inject
    InfluxDB influxDB

    @Inject
    DatabaseConfig databaseConfig

    @SuppressWarnings('unused')// used by Spock
    def setup() {
        wipeDatabase()
    }

    // Status & Collecting sensor measurements
    def '404-Not Found: when nothing is recorded'() {
        expect:
        !client.status(ANY_UUID)
    }

    def 'OK: after a measurement below threshold is recorded'() {
        when:
        collectMeasurement(MEASUREMENT_BELOW_THRESHOLD)

        then:
        status == OK
    }

    def 'WARN: after a measurement above is recorded'() {
        when:
        collectMeasurement(MEASUREMENT_ABOVE_THRESHOLD)

        then:
        status == WARN
    }

    def 'WARN/OK: the sensors do not interfere'() {
        given:
        def anotherUUID = UUID.randomUUID() as String

        when:
        collectMeasurement(ANY_UUID, MEASUREMENT_ABOVE_THRESHOLD)
        collectMeasurement(anotherUUID, MEASUREMENT_BELOW_THRESHOLD)

        then:
        getStatus(ANY_UUID) == WARN
        getStatus(anotherUUID) == OK
    }

    def 'WARN: after two above threshold'() {
        when:
        collectMeasurement(createAboveThreshold(NOW))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(1)))

        then:
        status == WARN
    }

    def 'ALERT: after three above threshold'() {
        when:
        (0..2)
                .collect { createAboveThreshold(NOW.plusMinutes(it)) }
                .forEach(this::collectMeasurement)

        then:
        status == ALERT
    }

    def 'ALERT: after three above threshold & one below'() {
        when: 'an alert is triggered'
        collectMeasurement(createAboveThreshold(NOW))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(2)))

        and: 'measurement is back to normal'
        collectMeasurement(createBelowThreshold(NOW.plusMinutes(3)))

        then:
        status == ALERT
    }

    def 'ALERT: after three above threshold & two below'() {
        when: 'an alert is triggered'
        collectMeasurement(createAboveThreshold(NOW))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(2)))

        and: 'measurement is back to normal'
        collectMeasurement(createBelowThreshold(NOW.plusMinutes(3)))
        collectMeasurement(createBelowThreshold(NOW.plusMinutes(4)))

        then:
        status == ALERT
    }

    def 'OK: after three above threshold & three below'() {
        when: 'an alert is triggered'
        collectMeasurement(createAboveThreshold(NOW))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(1)))
        collectMeasurement(createAboveThreshold(NOW.plusMinutes(2)))

        and: 'measurement is back to normal'
        collectMeasurement(createBelowThreshold(NOW.plusMinutes(3)))
        collectMeasurement(createBelowThreshold(NOW.plusMinutes(4)))
        collectMeasurement(createBelowThreshold(NOW.plusMinutes(5)))

        then:
        status == OK
    }
    //END: Status & Collecting sensor measurements

    // Sensor Metrics
    def 'Metrics: 404-Not Found: when nothing is recorded'() {
        expect:
        readMetrics(ANY_UUID) == null
    }

    private SensorMetrics readMetrics(uuid) {
        client.metrics(uuid)
    }

    def 'Metrics: Will return the maximum & average of the last 30 days'() {
        given:
        def measurements = generateRandomMeasurementsForA30DayWindow()
        def expectedMax = measurements.stream()
                .mapToInt(SensorMeasurement::getCo2)
                .max()
                .orElseThrow()

        int expectedAvg = (int) Math.round(
                measurements.stream()
                        .mapToInt(SensorMeasurement::getCo2)
                        .average()
                        .orElseThrow()
        )

        when: 'all the samples have collected by the API '
        measurements.forEach(this::collectMeasurement)

        and: 'the metrics are read'
        def metrics = readMetrics(ANY_UUID)

        then:
        metrics == new SensorMetrics(expectedMax, expectedAvg)
    }

    def 'Alerts: Will return an empty list if none have been recorded'() {
        expect:
        getAlerts(ANY_UUID) == []
    }

    def 'Alerts: will return an alarm with the startTime set to the 3rd measurement above threshold'() {
        given:
        def measurementsAboveThreshold = (0..2)
                .collect { createAboveThreshold(NOW.plusMinutes(it)) }

        def expected = new Alert(
                measurementsAboveThreshold[2].time,
                null,
                measurementsAboveThreshold[0].co2,
                measurementsAboveThreshold[1].co2,
                measurementsAboveThreshold[2].co2,
        )

        def someOtherId = UUID.randomUUID() as String
        when:
        measurementsAboveThreshold.forEach(this::collectMeasurement)
        def actualAlerts = getAlerts(ANY_UUID)

        then:
        actualAlerts == [expected]

        and: "alerts won't interfere"
        !getAlerts(someOtherId)
    }

    def 'Alerts: We can record multiple alerts and they\'ll be returned'() {
        given:
        def measurementsAboveThreshold = (0..2)
                .collect { createAboveThreshold(NOW.plusMinutes(it)) }

        def measurementsBelow = (3..5)
                .collect { createBelowThreshold(NOW.plusMinutes(it)) }

        def measurementsAboveThreshold2 = (6..8)
                .collect { createAboveThreshold(NOW.plusMinutes(it)) }


        when:
        [measurementsAboveThreshold, measurementsBelow, measurementsAboveThreshold2].flatten().forEach(this::collectMeasurement)
        def actualAlerts = getAlerts(ANY_UUID)

        then:
        actualAlerts.size() == 2
    }

    private List<Alert> getAlerts(id) {
        client.alerts(id)
    }

    private static List<SensorMeasurement> generateRandomMeasurementsForA30DayWindow() {
        def sampleSize = 100
        def averagingWindow = Duration.ofDays(30)

        def beginningOfPeriodToAverage = NOW.minusDays(averagingWindow.toDays())
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

    private void collectMeasurement(id = ANY_UUID, SensorMeasurement measurements) {
        client.measurements(id, measurements)
    }

    SensorStatus getStatus(id = ANY_UUID) {
        client.status(id).status
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
