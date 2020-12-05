package com.mruhwedel


import java.time.ZonedDateTime

import static java.time.ZonedDateTime.now

abstract class SensorTestData {
    static ANY_UUID = 'any-uuid'

    private static final int CO_2_THRESHOLD = 2000

    private static final int CO_2_BELOW_THRESHOLD = CO_2_THRESHOLD - 1
    private static final int CO_2_ABOVE_THRESHOLD = CO_2_THRESHOLD + 1

    private static final ZonedDateTime SOME_TIME = now()

    static MEASUREMENT_BELOW_THRESHOLD = createBelowThreshold()
    static MEASUREMENT_AT_THRESHOLD = createMeasurement(CO_2_THRESHOLD)
    static MEASUREMENT_ABOVE_THRESHOLD = createAboveThreshold()

    static createBelowThreshold(ZonedDateTime time = SOME_TIME) {
        createMeasurement(CO_2_BELOW_THRESHOLD, time)
    }

    static createAboveThreshold(ZonedDateTime time = SOME_TIME) {
        createMeasurement(CO_2_ABOVE_THRESHOLD, time)
    }

    static Measurement createMeasurement(int co2, ZonedDateTime time = SOME_TIME) {
        new Measurement(co2, time)
    }
}
