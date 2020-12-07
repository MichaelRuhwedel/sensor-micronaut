package com.mruhwedel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorMetrics {
    /**
     * Highest co2 ppa recorded by a sensor in the last 30 days
     */
    private int maxLast30Days;

    /**
     * average(mean) co2 ppa recorded by a sensor in the last 30 days
     */
    private int avgLast30Days;
}
