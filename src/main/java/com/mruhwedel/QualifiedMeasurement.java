package com.mruhwedel;

import lombok.Value;

@Value
public class QualifiedMeasurement {
    SensorMeasurement measurement;
    SensorStatus sensorStatus;
}
