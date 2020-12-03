package com.mruhwedel;

import io.micronaut.http.annotation.*;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static io.micronaut.http.HttpStatus.CREATED;

@Slf4j
@Controller("/api/v1/sensors/{uuid}")

public class SensorAPI {

    @Get // media-type defaults to application/json
    public StatusDto status(@QueryValue("uuid") String uuid) {
        StatusDto statusDto = new StatusDto(SensorStatus.OK);
        log.info("{}: {}", uuid, statusDto.getStatus());
        return statusDto;
    }

    @Post("/measurements")
    @Status(CREATED)
    public void measurements(
            @QueryValue("uuid") String uuid,
            @Body Measurements measurements
    ) {
        log.info("{}: {}@{}",
                uuid, measurements.getTime(), measurements.getCo2());
    }

    @Value
    private static class StatusDto {
        SensorStatus status;
    }
}