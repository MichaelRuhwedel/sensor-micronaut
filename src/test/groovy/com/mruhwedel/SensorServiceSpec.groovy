package com.mruhwedel

import spock.lang.Specification

import static com.mruhwedel.SensorStatus.OK
import static com.mruhwedel.SensorTestData.ANY_UUID
import static com.mruhwedel.SensorTestData.MEASUREMENT_BELOW_THRESHOLD

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
        def currentMeasurement = MEASUREMENT_BELOW_THRESHOLD
        def currentStatus = new QualifiedMeasurement(currentMeasurement, OK)
        def previousMeasurements = []

        when:
        service.recordAndUpdateStatus(ANY_UUID, currentMeasurement)

        then:
        1 * service.sensorRepository.fetchTwoPreviousMeasurements(ANY_UUID) >> previousMeasurements
        1 * service.statusCalculator.calculateCurrentStatus(currentMeasurement, previousMeasurements) >> currentStatus
        1 * service.sensorRepository.record(ANY_UUID, currentStatus)

    }
}