package com.mruhwedel;

import io.micronaut.http.annotation.*;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import static io.micronaut.http.HttpStatus.CREATED;

@Slf4j
@Controller("/api/v1/sensors/{uuid}")

public class SensorAPI {

    @Inject
    SensorService sensorService;

    @Get // media-type defaults to application/json, null will be 404
    public StatusDto status(@QueryValue("uuid") String uuid) {
        return sensorService
                .readStatus(uuid)
                .map(StatusDto::new)
                .orElse(null);
    }

    @Post("/measurements")
    @Status(CREATED)
    public void measurements(
            @QueryValue("uuid") String uuid,
            @Body Measurements measurements
    ) {
        sensorService.recordAndUpdateStatus(uuid, measurements);
    }

    @Value
    private static class StatusDto {
        SensorStatus status;
    }
}