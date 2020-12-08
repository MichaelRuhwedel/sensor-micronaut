package com.mruhwedel.domain;

import com.mruhwedel.repository.AlertRepository;
import com.mruhwedel.repository.SensorMeasurementRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.mruhwedel.domain.Alert.LIMIT_FOR_ALARM;
import static com.mruhwedel.domain.Alert.LIMIT_FOR_ALL_CLEAR;
import static com.mruhwedel.domain.SensorStatus.*;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Singleton
@Getter(PACKAGE)
@RequiredArgsConstructor
public class SensorService {

    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final AlertRepository alertRepository;

    @NonNull
    public Optional<SensorStatus> readStatus(@NonNull String uuid) {
        return alertRepository.getLatestOngoing(uuid)
                .map(ongoingAlert -> ALERT) // an ongoing alert is returned as such
                .or(() -> sensorMeasurementRepository.fetchCurrent(uuid)
                        .map(current -> current.isAboveThreshold() ?
                                WARN : OK
                        )
                );
    }

    @NonNull
    public Optional<SensorMetrics> readMetrics(@NonNull String uuid) {
        Optional<SensorMetrics> metrics = sensorMeasurementRepository.readMetrics(uuid);
        log.info("{}:  {}", uuid, metrics.map(Object::toString).orElse("UNKNOWN"));
        return metrics;
    }

    public void recordAndUpdateAlert(@NonNull String uuid, @NonNull SensorMeasurement measurement) {
        sensorMeasurementRepository.write(uuid, measurement);
        List<SensorMeasurement> measurements = sensorMeasurementRepository.fetchLastThreeMeasurements(uuid);

        if (LIMIT_FOR_ALARM == count(measurements, SensorMeasurement::isAboveThreshold)) {
            if (alertRepository.getLatestOngoing(uuid).isEmpty()) {
                createNewAlarm(uuid, measurements);
            }
        } else if (LIMIT_FOR_ALL_CLEAR == count(measurements, SensorMeasurement::isBelowThreshold)) {
            alertRepository.getLatestOngoing(uuid).ifPresent(ongoingAlert -> {
                endAlarm(uuid, ongoingAlert, measurements.stream().findFirst().orElseThrow());
            });
        }
    }

    private int count(List<SensorMeasurement> measurements, Predicate<SensorMeasurement> predicate) {
        return (int) measurements.stream()
                .filter(predicate)
                .count();
    }

    private void endAlarm(String uuid, Alert ongoingAlert, SensorMeasurement sensorMeasurement) {
        ongoingAlert.setEndTime(sensorMeasurement.getTime());
        alertRepository.save(uuid, ongoingAlert);
    }

    private void createNewAlarm(String uuid, List<SensorMeasurement> measurements) {
        alertRepository.save(
                uuid,
                new Alert(
                        measurements.get(0).getTime(),
                        null,
                        measurements.get(2).getCo2(),
                        measurements.get(1).getCo2(),
                        measurements.get(0).getCo2()
                ));
    }

    @NonNull
    public List<Alert> getAlerts(@NonNull String uuid) {
        return alertRepository.getAll(uuid);
    }
}
