package com.mruhwedel;

import io.micronaut.http.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;

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

    @Get("/metrics") // media-type defaults to application/json, null will be 404
    public @NonNull Optional<SensorMetrics> metrics(@QueryValue("uuid") String uuid) {
        return sensorService.readMetrics(uuid);
    }

    @Post("/measurements")
    @Status(CREATED)
    public void measurements(
            @QueryValue("uuid") String uuid,
            @Body Measurement measurement
    ) {
        sensorService.recordAndUpdateStatus(uuid, measurement);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class StatusDto {
        private SensorStatus status;
    }
}