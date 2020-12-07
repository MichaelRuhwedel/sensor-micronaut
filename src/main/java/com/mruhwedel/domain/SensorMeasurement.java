package com.mruhwedel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor // used by micronaut
@AllArgsConstructor // convenience
public class SensorMeasurement {
    /**
     * CO2 Levels between 2000 and 5000 ppm are associated with headaches, sleepiness
     * poor concentration, loss of attention, increased heart rate and slight nausea
     * may also be present
     */
    static final int CO_PPM_WARN_THRESHOLD = 2000;

    /**
     * the co2 level recorded by a sensor
     */
    private int co2;

    /**
     * When the recording took place
     */
    private ZonedDateTime time;

    /**
     * @see this#CO_PPM_WARN_THRESHOLD
     */
    public boolean isAboveThreshold() {
        return co2 > CO_PPM_WARN_THRESHOLD;
    }
    /**
     * @see this#CO_PPM_WARN_THRESHOLD
     */
    public boolean isBelowThreshold() {
        return co2 < CO_PPM_WARN_THRESHOLD;
    }
}
