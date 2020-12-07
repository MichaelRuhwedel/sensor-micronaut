package com.mruhwedel.domain;

import lombok.Value;

@Value
public class QualifiedMeasurement {
    SensorMeasurement measurement;
    SensorStatus sensorStatus;
}
