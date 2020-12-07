package com.mruhwedel.repository;

import com.mruhwedel.domain.Alert;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    /**
     * Will get the latest Alert (by start time), whose endTime isn't set
     */
    Optional<Alert> getLatestOngoing(@NonNull String uuid);

    /**
     * Writes an Alert, alerts with the same start time
     */
    void save(@NonNull String uuid, Alert alert);

    @NonNull
    List<Alert> getAll(@NonNull String uuid);
}
