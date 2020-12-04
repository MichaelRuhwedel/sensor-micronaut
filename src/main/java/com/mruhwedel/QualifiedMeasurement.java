package com.mruhwedel;

import lombok.Value;

@Value
public class QualifiedMeasurement {
    Measurement measurement;
    SensorStatus sensorStatus;
}
