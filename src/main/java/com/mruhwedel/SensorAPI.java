package com.mruhwedel;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

import static io.micronaut.http.HttpStatus.CREATED;

@Controller("/api/v1/sensors/{uuid}")
@Slf4j
public class SensorAPI {

    @Get // media-type defaults to application/json
    public StatusDto status(@QueryValue("uuid") String uuid) {

        StatusDto statusDto = new StatusDto(StatusDto.Status.OK);
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

    @Data
    private static class Measurements {
        private int co2;
        private ZonedDateTime time;
    }

    @Value
    private static class StatusDto {
        private enum Status {
            OK //,
            // ALERT,
            /// WARNING
        }

        Status status;
    }
}