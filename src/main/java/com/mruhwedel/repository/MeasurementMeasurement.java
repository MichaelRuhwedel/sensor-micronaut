package com.mruhwedel.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "co2_ppa")
public class MeasurementMeasurement { // suffixed all indexDb measurements with measurement -> measurementMeasurement :)

    @Column(name = "uuid", tag = true) // we don't want measurements to overwrite each other
    private String uuid;

    @Column(name = "time")
    private Instant time;

    @Column(name = "co2_level")
    private int co2Level;
}
