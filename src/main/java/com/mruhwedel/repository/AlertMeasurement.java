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
@Measurement(name = "alert_co2")
public class AlertMeasurement {
    @Column(name = "time")
    private Instant startTime;

    @Column(name = "end_time")
    private long endTime;

    @Column(name = "measurement_1")
    private int measurement1;

    @Column(name = "measurement_2")
    private int measurement2;

    @Column(name = "measurement_3")
    private int measurement3;

    @Column(name = "uuid", tag = true) // we don't want alerts to overwrite each other so we tag
    private String uuid;
}
