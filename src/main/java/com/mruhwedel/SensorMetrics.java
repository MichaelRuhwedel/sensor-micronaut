package com.mruhwedel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensorMetrics {
    private int maxLast30Days;
    private double avgLast30Days;
}
