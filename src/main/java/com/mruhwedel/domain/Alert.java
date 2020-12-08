package com.mruhwedel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * When enough consecutive measurements are above the warn threshold an
 * {@code Alarm} will be recorded.
 *
 * @see Alert#LIMIT_FOR_ALARM
 * @see SensorMeasurement#CO_PPM_WARN_THRESHOLD
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {
    /**
     * Consecutive measurements ABOVE the limit that will trigger an alarm
     */
    public static final int LIMIT_FOR_ALARM = 3;

    /**
     * Consecutive measurements BELOW the limit that will end an alarm (all-clear)
     */
    public static final int LIMIT_FOR_ALL_CLEAR = 3;

    /**
     * When the alarm started, the timestamp of measurement3
     */
    private ZonedDateTime startTime;

    /**
     * When the alarm ended, the timestamp of the 3rd OK
     */
    private ZonedDateTime endTime;

    /**
     * the earliest measurement that lead on to the alarm
     */
    private int measurement1;

    /**
     * the second to last measurement that lead on to the alarm
     */
    private int measurement2;

    /**
     * the ultimate measurement that triggered the alarm
     */
    private int measurement3;
}
