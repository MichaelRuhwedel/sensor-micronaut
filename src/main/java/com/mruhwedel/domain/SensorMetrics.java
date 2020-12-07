package com.mruhwedel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorMetrics {
    private int maxLast30Days;
    private int avgLast30Days;
}
