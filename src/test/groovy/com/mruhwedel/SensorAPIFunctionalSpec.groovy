package com.mruhwedel

import com.mruhwedel.InfluxDbFactory.DatabaseConfig
import groovy.json.JsonSlurper
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBMapper
import spock.lang.Specification

import javax.inject.Inject
import java.time.ZonedDateTime

import static com.mruhwedel.SensorTestData.*
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(environments = ['functional-test'])
class SensorAPIFunctionalSpec extends Specification {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/api/v1/sensors/")
    HttpClient client;

    @Inject
    InfluxDB influxDB

    @Inject
    DatabaseConfig databaseConfig

    static final JsonSlurper SLURPER = new JsonSlurper()

    def setup() {
        wipeDatabase()
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

    def '404-Not Found: when nothing is recorded'() {
        when:
        client.toBlocking().exchange(ANY_UUID).status() == NOT_FOUND

        then:
        thrown HttpClientResponseException
    }

    def 'OK: after a measurement below threshold is recorded'() {
        when:
        postMeasurement(createBelowThreshold())

        then:
        status == 'OK'
    }

    def 'WARN: after a measurement above is recorded'() {
        when:
        postMeasurement(createAboveThreshold())

        then:
        status == 'WARN'
    }

    def 'WARN: after two above threshold'() {
        given:
        def now = ZonedDateTime.now()

        when:
        postMeasurement(createAboveThreshold(now))
        postMeasurement(createAboveThreshold(now.plusMinutes(1)))

        then:
        status == 'WARN'
    }

    def 'ALERT: after three above threshold'() {
        given:
        def now = ZonedDateTime.now()

        when:
        postMeasurement(createAboveThreshold(now))
        postMeasurement(createAboveThreshold(now.plusMinutes(1)))
        postMeasurement(createAboveThreshold(now.plusMinutes(2)))

        then:
        status == 'ALERT'
    }

    def 'ALERT: after three above threshold & one below'() {
        given:
        def now = ZonedDateTime.now()

        when: 'an alert is triggered'
        postMeasurement(createAboveThreshold(now))
        postMeasurement(createAboveThreshold(now.plusMinutes(1)))
        postMeasurement(createAboveThreshold(now.plusMinutes(2)))

        and: 'measurement is back to normal'
        postMeasurement(createBelowThreshold(now.plusMinutes(3)))

        then:
        status == 'ALERT'
    }

    def 'ALERT: after three above threshold & two below'() {
        given:
        def now = ZonedDateTime.now()

        when: 'an alert is triggered'
        postMeasurement(createAboveThreshold(now))
        postMeasurement(createAboveThreshold(now.plusMinutes(1)))
        postMeasurement(createAboveThreshold(now.plusMinutes(2)))

        and: 'measurement is back to normal'
        postMeasurement(createBelowThreshold(now.plusMinutes(3)))
        postMeasurement(createBelowThreshold(now.plusMinutes(4)))

        then:
        status == 'ALERT'
    }

    def 'OK: after three above threshold & three below'() {
        given:
        def now = ZonedDateTime.now()

        when: 'an alert is triggered'
        postMeasurement(createAboveThreshold(now))
        postMeasurement(createAboveThreshold(now.plusMinutes(1)))
        postMeasurement(createAboveThreshold(now.plusMinutes(2)))

        and: 'measurement is back to normal'
        postMeasurement(createBelowThreshold(now.plusMinutes(3)))
        postMeasurement(createBelowThreshold(now.plusMinutes(4)))
        postMeasurement(createBelowThreshold(now.plusMinutes(5)))

        then:
        status == 'OK'
    }

    private HttpResponse<Object> postMeasurement(Measurement threshold) {
        client.toBlocking().exchange(POST("$ANY_UUID/measurements", threshold))
    }

    String getStatus() {
        SLURPER
                .parseText(client.toBlocking().retrieve(ANY_UUID))
                .status // status field of the json body
    }
}
