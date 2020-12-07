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
public class MeasurementCO2 {

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "time")
    private Instant time;

    @Column(name = "co2_level")
    private int co2Level;
}
