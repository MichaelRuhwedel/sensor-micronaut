package com.mruhwedel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * When enough consecutive measurements are above the warn threshold an
 * {@code Alarm} will be recorded.
 *
 * @see Alert#LIMIT_FOR_ALARM
 * @see Measurement#CO_PPM_WARN_THRESHOLD
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {
    public static final int LIMIT_FOR_ALARM = 3;

    /**
     * When the alarm started, the timestamp of measurement3
     */
    private ZonedDateTime startTime;

    /**
     * When the alarm started ended, the timestamp of the 3rd measurement below threshold
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

    boolean isOngoing() {
        return endTime == null;
    }
}
