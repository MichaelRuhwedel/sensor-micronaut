package com.mruhwedel;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mruhwedel.SensorStatus.OK;

@Slf4j
@Singleton
public class InfluxDbSensorRepository implements SensorRepository {
    @Override
    public Optional<SensorStatus> readStatus(@NonNull String uuid) {
        log.info("reading");
        return Optional.of(OK);
    }

    @Override
    public @NonNull List<Measurements> fetchThreePreviousMeasurements(@NonNull String uuid) {
        log.info("fetching");
        return Collections.emptyList();
    }

    @Override
    public void record(@NonNull String uuid, @NonNull Measurements measurements, @NonNull SensorStatus sensorStatus) {
        log.info("recording");
    }
}
