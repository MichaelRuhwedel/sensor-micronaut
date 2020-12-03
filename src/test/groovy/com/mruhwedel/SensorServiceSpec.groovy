package com.mruhwedel

import spock.lang.Specification

import static com.mruhwedel.SensorStatus.OK
import static com.mruhwedel.SensorTestData.ANY_UUID
import static com.mruhwedel.SensorTestData.MEASUREMENT_OK

class SensorServiceSpec extends Specification {

    def service = new SensorService(
            Mock(SensorRepository),
            Mock(StatusCalculator)
    )

    def "read() will get the status from the repo"() {
        given:
        def expectedStatus = OK

        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.sensorRepository.readStatus(ANY_UUID) >> Optional.of(expectedStatus)
        status.map(expectedStatus::equals)
    }

    def "recordAndUpdateStatus() will record the measurement and the correct status"() {
        given:
        def expectedStatus = OK
        def previousMeasurement = []

        when:
        service.recordAndUpdateStatus(ANY_UUID, MEASUREMENT_OK)

        then:
        1 * service.sensorRepository.fetchThreePreviousMeasurements(ANY_UUID) >> previousMeasurement
        1 * service.statusCalculator.calculateCurrentStatus(previousMeasurement) >> expectedStatus
        1 * service.sensorRepository.record(ANY_UUID, MEASUREMENT_OK, expectedStatus)

    }
}
