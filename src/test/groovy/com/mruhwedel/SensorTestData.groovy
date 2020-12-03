package com.mruhwedel


import java.time.ZonedDateTime

import static java.time.ZonedDateTime.now

abstract class SensorTestData {
    static ANY_UUID = 'any-uuid'

    private static final int CO_2_ALERT_THRESHOLD = 2000
    private static final int CO_2_OK = CO_2_ALERT_THRESHOLD - 1
    private static final int CO_2_CRITICAL = CO_2_ALERT_THRESHOLD + 1
    private static final ZonedDateTime SOME_TIME = now()

    static MEASUREMENT_OK = create(CO_2_OK)
    static MEASUREMENT_LIMIT = create(CO_2_ALERT_THRESHOLD)
    static MEASUREMENT_CRITICAL = create(CO_2_CRITICAL)

    private static Measurement create(int co2) {
        return new Measurement(co2, SOME_TIME) // if you want sth. more realistic, use a unique timestamp here
    }
}
