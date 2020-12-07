package com.mruhwedel;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    /**
     * Will get the latest Aler (by start time)
     */
    Optional<Alert> getLatestOngoing(@NonNull String uuid);

    /**
     * Writes an Alert, alerts with the same start time
     */
    void save(@NonNull String uuid, Alert alert);

    List<Alert> getAll(@NonNull String uuid);
}
