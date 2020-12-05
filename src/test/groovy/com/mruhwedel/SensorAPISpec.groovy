package com.mruhwedel

import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

import static com.mruhwedel.SensorTestData.ANY_UUID
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.CREATED

@MicronautTest
class SensorAPISpec extends Specification {

    @Inject
    @Client("/api/v1/sensors/")
    HttpClient client;

    def 'status() returns'() {
        expect:
        client.toBlocking().retrieve(ANY_UUID)
    }

    def 'measurements() creates'() {
        given:
        def measurements = '''
        {
            "co2" : 2000,
            "time" : "2019-02-01T18:55:47+00:00"
        }
        '''

        when:
        def response = client.toBlocking().exchange(
                POST(
                        "$ANY_UUID/measurements",
                        measurements
                ))

        then:
        response.status() == CREATED
    }
}
