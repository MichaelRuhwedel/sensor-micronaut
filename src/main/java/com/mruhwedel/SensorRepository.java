package com.mruhwedel;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;


public interface SensorRepository {

    Optional<Measurement> fetchCurrent(@NonNull String uuid);

    @NonNull
    List<Measurement> fetchLastThreeMeasurements(@NonNull String uuid);

    void record(@NonNull String uuid, @NonNull Measurement measurement);

    Optional<SensorMetrics> readMetrics(String uuid);
}
