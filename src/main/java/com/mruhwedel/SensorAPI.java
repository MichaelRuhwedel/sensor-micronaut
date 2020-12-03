package com.mruhwedel;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Controller("/api/v1/sensors/{uuid}")
public class SensorAPI {

    @Get // media-type defaults to application/json
    public String status(@QueryValue("uuid") String uuid) {
        return "{\"status\" : \"OK\"}";
    }

    @Post("/measurements")
    @Status(HttpStatus.CREATED)
    public void measurements(
            @QueryValue("uuid") String uuid,
            @Body Measurements measurements
    ) {

    }

    @Data
    private static class Measurements {
        private int co2;
        private ZonedDateTime time;
    }

}