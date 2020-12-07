package com.mruhwedel.domain

import com.mruhwedel.StatusCalculator
import com.mruhwedel.repository.AlertRepository
import com.mruhwedel.repository.SensorMeasurementRepository
import spock.lang.Specification

import static com.mruhwedel.domain.SensorStatus.*
import static com.mruhwedel.domain.SensorTestData.*

class SensorServiceSpec extends Specification {

    def service = new SensorService(
            Mock(SensorMeasurementRepository),
            Mock(AlertRepository),
            Mock(StatusCalculator)
    )

    def "readStatus() OK: No previous alert or an ended one AND the current measurement is below threshold"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> alert
        1 * service.sensorMeasurementRepository.fetchCurrent(ANY_UUID) >> MEASUREMENT_BELOW_THRESHOLD

        status
                .map(OK::equals)
                .orElseThrow()

        where:
        alert << [
                Optional.empty(),
                Optional.of(ALERT_ENDED)
        ]
    }

    def "readStatus() WARN: No Alert or Ended Alert and current measurement is above threshold"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        1 * service.sensorMeasurementRepository.fetchCurrent(ANY_UUID) >> MEASUREMENT_ABOVE_THRESHOLD

        status
                .map(WARN::equals)
                .orElseThrow()

        where:
        alert << [
                Optional.empty(),
                Optional.of(ALERT_ENDED)
        ]
    }

    def "readStatus() ALERT: There's an ongoing  alert (and we don't care what's currently measured)"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.of(ALERT_ONGOING)
        0 * service.sensorMeasurementRepository.fetchCurrent(_)

        status
                .map(ALERT::equals)
                .orElseThrow()
    }

    def "recordAndUpdateStatus() will record the measurement and the correct status"() {
        given:
        def currentMeasurement = MEASUREMENT_BELOW_THRESHOLD
        def currentStatus = new QualifiedMeasurement(currentMeasurement, OK)
        def previousMeasurements = []

        when:
        service.recordAndUpdateAlert(ANY_UUID, currentMeasurement)

        then:
        1 * service.sensorMeasurementRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousMeasurements
//        1 * service.statusCalculator.calculateCurrentStatus(currentMeasurement, previousMeasurements) >> currentStatus
        1 * service.sensorMeasurementRepository.collect(ANY_UUID, currentStatus)

    }
}
