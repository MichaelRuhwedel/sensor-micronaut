package com.mruhwedel;

import com.mruhwedel.domain.*;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.HttpStatus.CREATED;
import static javax.validation.constraints.Pattern.Flag.CASE_INSENSITIVE;

@Slf4j
@Validated
@Controller("/api/v1/sensors/{uuid}")
public class SensorAPI {

    @Inject
    private SensorService sensorService;

    @Get // media-type defaults to application/json, null will be 404
    public StatusDto status(@RequestBean UUIDValidated request) {
        return sensorService
                .readStatus(request.uuid)
                .map(StatusDto::new)
                .orElse(null);
    }

    @Get("metrics") // media-type defaults to application/json, null will be 404
    public @NonNull Optional<SensorMetrics> metrics(@RequestBean UUIDValidated request) {
        return sensorService.readMetrics(request.getUuid());
    }

    @Get("alerts") // media-type defaults to application/json, null will be 404
    public @NonNull List<Alert> alerts(@RequestBean UUIDValidated request) {
        return sensorService.getAlerts(request.getUuid());
    }

    @Post("measurements")
    @Status(CREATED)
    public void measurements(@RequestBean UUIDValidated request,
                             @Body SensorMeasurement measurement
    ) {
        sensorService.recordAndUpdateAlert(request.getUuid(), measurement);
    }

    @Data
    @NoArgsConstructor // for deserialization
    @AllArgsConstructor
    public static class StatusDto {
        private SensorStatus status;
    }

    @Data
    @Introspected
    public static class UUIDValidated {
        // https://stackoverflow.com/questions/37320870/is-there-a-uuid-validator-annotation
        private static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

        @Pattern(regexp = UUID_PATTERN, flags = CASE_INSENSITIVE)
        @PathVariable("uuid")
        private String uuid;

        public void setUuid(String uuid) {
            this.uuid = uuid.toLowerCase();
        }
    }
}