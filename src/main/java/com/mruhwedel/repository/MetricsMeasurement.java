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
@Measurement(name = "co2_ppa", database = "sensors")
public class MetricsMeasurement {

    @Column(name = "mean")
    private int mean;

    @Column(name = "max")
    private int max;

}
