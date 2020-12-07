package com.mruhwedel.domain


import com.mruhwedel.repository.AlertRepository
import com.mruhwedel.repository.SensorMeasurementRepository
import spock.lang.Specification

import static com.mruhwedel.domain.SensorStatus.*
import static com.mruhwedel.domain.SensorTestData.*

class SensorServiceSpec extends Specification {

    public static final int ALERT_AND_ALL_CLEAR_LIMIT = 3
    def service = new SensorService(
            Mock(SensorMeasurementRepository),
            Mock(AlertRepository),
    )

    def "readStatus() OK: No previous alert or an ended one AND the current measurement is below threshold"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> alert
        1 * service.sensorMeasurementRepository.fetchCurrent(ANY_UUID) >> Optional.of(MEASUREMENT_BELOW_THRESHOLD)

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
        1 * service.sensorMeasurementRepository.fetchCurrent(ANY_UUID) >> Optional.of(MEASUREMENT_ABOVE_THRESHOLD)

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

    def "recordAndUpdateAlert() will collect the measurement"() {
        given:
        def currentMeasurement = MEASUREMENT_BELOW_THRESHOLD

        when:
        service.recordAndUpdateAlert(ANY_UUID, currentMeasurement)

        then:
        1 * service.sensorMeasurementRepository.collect(ANY_UUID, currentMeasurement)
        _ * service.sensorMeasurementRepository.fetchLastThreeMeasurements(_) >> []
    }

    def "recordAndUpdateAlert() NO Alert: No alert is recorded when the alert limit is not crossed"(
            SensorMeasurement current,
            List<SensorMeasurement> previousThree) {
        when:
        service.recordAndUpdateAlert(ANY_UUID, MEASUREMENT_BELOW_THRESHOLD)

        then:
        1 * service.sensorMeasurementRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousThree
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        0 * service.alertRepository.save(ANY_UUID, _)

        where:
        current                     | previousThree
        MEASUREMENT_BELOW_THRESHOLD | []
        MEASUREMENT_AT_THRESHOLD    | [MEASUREMENT_BELOW_THRESHOLD]
        MEASUREMENT_ABOVE_THRESHOLD | [MEASUREMENT_BELOW_THRESHOLD] * ALERT_AND_ALL_CLEAR_LIMIT

        MEASUREMENT_BELOW_THRESHOLD | [MEASUREMENT_ABOVE_THRESHOLD]

        MEASUREMENT_BELOW_THRESHOLD | [MEASUREMENT_BELOW_THRESHOLD,
                                       MEASUREMENT_ABOVE_THRESHOLD,
                                       MEASUREMENT_ABOVE_THRESHOLD]

    }


    def "recordAndUpdateAlert() NEW Alert: an alert is saved when the alert limit (3) is crossed"() {
        given:
        def previousThree = [
                createAboveThreshold(NOW),
                createAboveThreshold(NOW.minusMinutes(1)),
                createAboveThreshold(NOW.minusMinutes(2))
        ]

        def newOngoingAlert = new Alert(
                previousThree[0].time,
                null,
                previousThree[2].co2,
                previousThree[1].co2,
                previousThree[0].co2
        )

        when:
        service.recordAndUpdateAlert(ANY_UUID, Stub(SensorMeasurement)) // we can stub the measurement, we only care about what's recorded

        then:
        1 * service.sensorMeasurementRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousThree
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        1 * service.alertRepository.save(ANY_UUID, newOngoingAlert) //
    }


    def "recordAndUpdateAlert() ONGOING Alert: An Alert is NOT ended when the all-clear limit (3) isn't reached"() {
        when:
        service.recordAndUpdateAlert(ANY_UUID, Stub(SensorMeasurement)) // we can stub the measurement, we only care about what's recorded

        then:
        1 * service.sensorMeasurementRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousThree
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.of(Stub(Alert))
        0 * service.alertRepository.save(ANY_UUID, _) //

        where:
        previousThree << [
                [],
                [MEASUREMENT_ABOVE_THRESHOLD],
                [MEASUREMENT_ABOVE_THRESHOLD] * 2,
                [MEASUREMENT_ABOVE_THRESHOLD] * ALERT_AND_ALL_CLEAR_LIMIT,

                [MEASUREMENT_BELOW_THRESHOLD] * 2 + MEASUREMENT_ABOVE_THRESHOLD,

                [MEASUREMENT_BELOW_THRESHOLD] * 2 + MEASUREMENT_ABOVE_THRESHOLD,
        ]
    }

    def "recordAndUpdateAlert() ENDING an Alert: An Alert is Ended when the all-clear limit (3) is reached"() {
        given:
        def ongoingAlert = new Alert(
                NOW,
                null,
                1,
                2,
                3
        )

        def previousMeasurements = [
                createBelowThreshold(NOW),
                createBelowThreshold(NOW.minusMinutes(1)),
                createBelowThreshold(NOW.minusMinutes(2)),
        ]

        when:
        service.recordAndUpdateAlert(ANY_UUID, Stub(SensorMeasurement)) // we can stub the measurement, we only care about what's recorded

        then:
        1 * service.sensorMeasurementRepository.fetchLastThreeMeasurements(ANY_UUID) >> [MEASUREMENT_BELOW_THRESHOLD] * ALERT_AND_ALL_CLEAR_LIMIT
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.of(ongoingAlert)
        1 * service.alertRepository.save(ANY_UUID, {
            it == ongoingAlert
            it.endTime == previousMeasurements[0].time
        }) //
    }
}
