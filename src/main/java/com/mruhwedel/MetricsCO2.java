package com.mruhwedel;

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
public class MetricsCO2 {

    @Column(name = "mean")
    private double mean;

    @Column(name = "max")
    private int max;

}
