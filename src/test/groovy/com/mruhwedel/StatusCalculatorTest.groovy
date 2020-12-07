package com.mruhwedel

import spock.lang.Specification
import spock.lang.Unroll

import static com.mruhwedel.SensorStatus.*
import static com.mruhwedel.SensorTestData.*
import static com.mruhwedel.SensorTestData.MEASUREMENT_BELOW_THRESHOLD

class StatusCalculatorTest extends Specification {

//    static final QM_OK = new QualifiedMeasurement(MEASUREMENT_AT_THRESHOLD, OK)
//    static final QM_WARN = new QualifiedMeasurement(MEASUREMENT_ABOVE_THRESHOLD, WARN)
//    static final QM_ALERT = new QualifiedMeasurement(MEASUREMENT_ABOVE_THRESHOLD, ALERT)
//    def calculator = new StatusCalculator()
//
//    @Unroll("expected: #expected when current: #current.co2 previous: #previous ")
//    def 'does the job'(current, previous, SensorStatus expected) {
//
//        expect:
//        calculator.calculateCurrentStatus(current, previous) == new QualifiedMeasurement(current, expected)
//
//        where:
//        previous            | current                     || expected
//        []                  | MEASUREMENT_BELOW_THRESHOLD || OK
//        []                  | MEASUREMENT_AT_THRESHOLD    || OK
//        []                  | MEASUREMENT_ABOVE_THRESHOLD || WARN
//
//        [QM_OK]             | MEASUREMENT_AT_THRESHOLD    || OK
//        [QM_OK]             | MEASUREMENT_ABOVE_THRESHOLD || WARN
//
//
//        [QM_ALERT]          | MEASUREMENT_BELOW_THRESHOLD || ALERT
//        [QM_ALERT] * 2      | MEASUREMENT_BELOW_THRESHOLD || ALERT
//        [QM_OK, QM_ALERT]   | MEASUREMENT_BELOW_THRESHOLD || OK
//        [QM_OK] * 2         | MEASUREMENT_BELOW_THRESHOLD || OK
//
//
//        [QM_OK, QM_WARN]    | MEASUREMENT_ABOVE_THRESHOLD || WARN
//
//        [QM_WARN]           | MEASUREMENT_ABOVE_THRESHOLD || WARN
//        [QM_ALERT, QM_WARN] | MEASUREMENT_BELOW_THRESHOLD || ALERT
//
//        [QM_WARN] * 2       | MEASUREMENT_ABOVE_THRESHOLD || ALERT
//        [QM_OK, QM_WARN]    | MEASUREMENT_ABOVE_THRESHOLD || WARN
//
//    }
}
