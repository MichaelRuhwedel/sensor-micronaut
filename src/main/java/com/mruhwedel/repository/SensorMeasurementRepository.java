package com.mruhwedel.repository;

import com.mruhwedel.domain.SensorMeasurement;
import com.mruhwedel.domain.SensorMetrics;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;


public interface SensorMeasurementRepository {

    /**
     * Returns the current (last) measurements for the given uuid of a sensor.
     */
    Optional<SensorMeasurement> fetchCurrent(@NonNull String uuid);

    /**
     * Returns the last three measurements for the given uuid of a sensor.
     * Youngest first, oldest last:<br>
     * <code>[current, 'one minute before', 'two minutes before']</code>
     */
    @NonNull
    List<SensorMeasurement> fetchLastThreeMeasurements(@NonNull String uuid);

    /**
     * Will collect (save) the measurement under the uuid of a sensor.
     */
    void write(@NonNull String uuid, @NonNull SensorMeasurement measurement);

    /**
     * Let the repository fetch the metrics for sensor of the given uuid.
     * @see SensorMetrics
     */
    Optional<SensorMetrics> readMetrics(String uuid);
}
