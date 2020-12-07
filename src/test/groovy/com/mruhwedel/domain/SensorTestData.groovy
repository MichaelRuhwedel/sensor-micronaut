package com.mruhwedel.domain

import com.mruhwedel.domain.Alert
import com.mruhwedel.domain.SensorMeasurement

import java.time.ZoneId
import java.time.ZonedDateTime

import static java.time.ZonedDateTime.now
import static java.time.temporal.ChronoUnit.SECONDS

abstract class SensorTestData {
    static ANY_UUID = 'any-uuid'

    private static final int CO_2_THRESHOLD = 2000

    private static final int CO_2_BELOW_THRESHOLD = CO_2_THRESHOLD - 1
    private static final int CO_2_ABOVE_THRESHOLD = CO_2_THRESHOLD + 1

    static final ZonedDateTime NOW = ZonedDateTime.of(2020, 10, 1, 1, 0, 0, 0,ZoneId.of('UTC')).truncatedTo(SECONDS)

    static MEASUREMENT_BELOW_THRESHOLD = createBelowThreshold()
    static MEASUREMENT_AT_THRESHOLD = createMeasurement(CO_2_THRESHOLD)
    static MEASUREMENT_ABOVE_THRESHOLD = createAboveThreshold()

    static ALERT_ONGOING = createAlertOngoing()
    static ALERT_ENDED = createAlertEnded()

    static createBelowThreshold(ZonedDateTime time = NOW) {
        createMeasurement(CO_2_BELOW_THRESHOLD, time)
    }

    static createAboveThreshold(ZonedDateTime time = NOW) {
        createMeasurement(CO_2_ABOVE_THRESHOLD, time)
    }

    static SensorMeasurement createMeasurement(int co2, ZonedDateTime time = NOW) {
        new SensorMeasurement(co2, time)
    }

    static createAlertOngoing() {
        createAlert()
    }

    static createAlertEnded() {
        createAlert(NOW, NOW.plusMinutes(4))
    }

    static createAlert(
            ZonedDateTime start = NOW,
            ZonedDateTime end = null,
            int m1 = CO_2_ABOVE_THRESHOLD,
            int m2 = CO_2_ABOVE_THRESHOLD,
            int m3 = CO_2_ABOVE_THRESHOLD
    ) {
        new Alert(start, end, m1, m2, m3)
    }
}
