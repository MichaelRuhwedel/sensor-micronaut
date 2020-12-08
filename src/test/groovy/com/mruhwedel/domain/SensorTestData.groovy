package com.mruhwedel.domain

import com.mruhwedel.domain.Alert
import com.mruhwedel.domain.SensorMeasurement

import java.time.ZoneId
import java.time.ZonedDateTime

import static java.time.ZonedDateTime.now
import static java.time.temporal.ChronoUnit.SECONDS

abstract class SensorTestData {
    static final String ANY_UUID = UUID.randomUUID() as String

    private static final int CO_2_THRESHOLD = 2000

    private static final int CO_2_BELOW_THRESHOLD = CO_2_THRESHOLD - 1
    private static final int CO_2_ABOVE_THRESHOLD = CO_2_THRESHOLD + 1

    static final ZonedDateTime NOW = now(ZoneId.of("UTC")).truncatedTo(SECONDS)

    static final MEASUREMENT_BELOW_THRESHOLD = createBelowThreshold()
    static final MEASUREMENT_AT_THRESHOLD = createMeasurement(CO_2_THRESHOLD)
    static final MEASUREMENT_ABOVE_THRESHOLD = createAboveThreshold()

    static final ALERT_ONGOING = createAlertOngoing()
    static final ALERT_ENDED = createAlertEnded()

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
