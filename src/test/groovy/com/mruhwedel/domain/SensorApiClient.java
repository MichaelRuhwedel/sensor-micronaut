package com.mruhwedel.domain;

import com.mruhwedel.SensorAPI.StatusDto;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

/**
 * Declarative HTTP Client used for testing
 * This will create proper GET and POST requests
 */
@Client("/api/v1/sensors")
public interface SensorApiClient {

    @Get("/{uuid}")
    StatusDto status(@QueryValue("uuid") String uuid);

    @Get("/{uuid}/metrics")
    SensorMetrics metrics(@QueryValue("uuid") String uuid);

    @Get("/{uuid}/alerts")
    List<Alert> alerts(@QueryValue("uuid") String uuid);

    @Post("/{uuid}/measurements")
    void measurements(@QueryValue("uuid") String uuid, @Body SensorMeasurement measurement);
}
