package com.mruhwedel.domain

import com.mruhwedel.StatusCalculator
import com.mruhwedel.repository.AlertRepository
import com.mruhwedel.repository.SensorMeasurementRepository
import org.influxdb.annotation.Measurement
import spock.lang.Specification
import spock.lang.Unroll

import static com.mruhwedel.domain.SensorStatus.*
import static com.mruhwedel.domain.SensorTestData.*
import static com.mruhwedel.domain.SensorTestData.ANY_UUID
import static com.mruhwedel.domain.SensorTestData.ANY_UUID
import static com.mruhwedel.domain.SensorTestData.ANY_UUID
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_ABOVE_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_ABOVE_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_ABOVE_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_BELOW_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_BELOW_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_BELOW_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_BELOW_THRESHOLD
import static com.mruhwedel.domain.SensorTestData.MEASUREMENT_BELOW_THRESHOLD

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
    }

    def "recordAndUpdateAlert() No-Alarm: No alert is recorded when the alert limit is not crossed"(
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
        MEASUREMENT_ABOVE_THRESHOLD | [MEASUREMENT_BELOW_THRESHOLD] * 3

        MEASUREMENT_BELOW_THRESHOLD | [MEASUREMENT_ABOVE_THRESHOLD]

        MEASUREMENT_BELOW_THRESHOLD | [MEASUREMENT_BELOW_THRESHOLD,
                                       MEASUREMENT_ABOVE_THRESHOLD,
                                       MEASUREMENT_ABOVE_THRESHOLD]

    }
}
