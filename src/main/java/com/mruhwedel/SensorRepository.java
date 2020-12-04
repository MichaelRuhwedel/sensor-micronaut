package com.mruhwedel;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;


public interface SensorRepository {
    Optional<SensorStatus> readStatus(@NonNull String uuid);

    @NonNull
    List<QualifiedMeasurement> fetchTwoPreviousMeasurements(@NonNull String uuid);

    void record(@NonNull String uuid, @NonNull QualifiedMeasurement measurement);
}
