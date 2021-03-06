package com.mruhwedel.domain

import com.mruhwedel.repository.AlertRepository
import com.mruhwedel.repository.SensorRepository
import spock.lang.Specification

import static com.mruhwedel.domain.SensorStatus.*
import static com.mruhwedel.domain.SensorTestData.*

class SensorServiceSpec extends Specification {

    public static final int ALERT_AND_ALL_CLEAR_LIMIT = 3
    def service = new SensorService(
            Mock(SensorRepository),
            Mock(AlertRepository),
    )

    def "readStatus() OK: No ongoing alert  AND the current measurement is below threshold"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        1 * service.sensorRepository.fetchCurrent(ANY_UUID) >> Optional.of(MEASUREMENT_BELOW_THRESHOLD)

        status.map(OK::equals).orElse(false)
    }

    def "readStatus() WARN: No ongoing Alert and current measurement is above threshold"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        1 * service.sensorRepository.fetchCurrent(ANY_UUID) >> Optional.of(MEASUREMENT_ABOVE_THRESHOLD)

        status.map(WARN::equals).orElse(false)
    }

    def "readStatus() ALERT: There's an ongoing  alert (and we don't care what's currently measured)"() {
        when:
        def status = service.readStatus(ANY_UUID)

        then:
        1 * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.of(ALERT_ONGOING)
        0 * service.sensorRepository.fetchCurrent(_)

        status.map(ALERT::equals).orElse(false)
    }

    def "recordAndUpdateAlert() will collect the measurement"() {
        given:
        def currentMeasurement = MEASUREMENT_BELOW_THRESHOLD

        when:
        service.recordAndUpdateAlert(ANY_UUID, currentMeasurement)

        then:
        1 * service.sensorRepository.write(ANY_UUID, currentMeasurement)
        _ * service.sensorRepository.fetchLastThreeMeasurements(_) >> []
    }

    def "recordAndUpdateAlert() NO Alert: No alert is recorded when the alert limit is not crossed"(
            SensorMeasurement current,
            List<SensorMeasurement> previousThree
    ) {

        when:
        service.recordAndUpdateAlert(ANY_UUID, MEASUREMENT_BELOW_THRESHOLD)

        then:
        1 * service.sensorRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousThree
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        0 * service.alertRepository.write(ANY_UUID, _)

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
        1 * service.sensorRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousThree
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.empty()
        1 * service.alertRepository.write(ANY_UUID, newOngoingAlert) //
    }


    def "recordAndUpdateAlert() ONGOING Alert: An Alert is NOT ended when the all-clear limit (3) isn't reached"() {
        when:
        service.recordAndUpdateAlert(ANY_UUID, Stub(SensorMeasurement)) // we can stub the measurement, we only care about what's recorded

        then:
        1 * service.sensorRepository.fetchLastThreeMeasurements(ANY_UUID) >> previousThree
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.of(Stub(Alert))
        0 * service.alertRepository.write(ANY_UUID, _) //

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
        1 * service.sensorRepository.fetchLastThreeMeasurements(ANY_UUID) >> [MEASUREMENT_BELOW_THRESHOLD] * ALERT_AND_ALL_CLEAR_LIMIT
        _ * service.alertRepository.getLatestOngoing(ANY_UUID) >> Optional.of(ongoingAlert)
        1 * service.alertRepository.write(ANY_UUID, {
            it == ongoingAlert
            it.endTime == previousMeasurements[0].time
        }) //
    }
}
