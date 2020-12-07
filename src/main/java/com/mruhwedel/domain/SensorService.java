package com.mruhwedel.domain;

import com.mruhwedel.StatusCalculator;
import com.mruhwedel.repository.AlertRepository;
import com.mruhwedel.repository.SensorMeasurementRepository;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.mruhwedel.domain.SensorStatus.*;
import static java.time.ZonedDateTime.now;
import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Singleton
@Getter(PACKAGE)
@RequiredArgsConstructor
public class SensorService {

    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final AlertRepository alertRepository;
    private final StatusCalculator statusCalculator;

    @NonNull
    Optional<SensorStatus> readStatus(@NonNull String uuid) {
        return alertRepository
                .getLatestOngoing(uuid)
                .filter(Alert::isOngoing)
                .map(ongoingAlert -> ALERT) // an ongoing alert is returned as such
                .or(() -> sensorMeasurementRepository.fetchCurrent(uuid)
                        .map(current -> current.isAboveThreshold() ?
                                WARN : OK
                        )
                );
    }


    @NonNull
    Optional<SensorMetrics> readMetrics(@NonNull String uuid) {
        Optional<SensorMetrics> metrics = sensorMeasurementRepository.readMetrics(uuid);
        log.info("{}:  {}", uuid, metrics.map(Object::toString).orElse("UNKNOWN"));
        return metrics;
    }

    void recordAndUpdateAlert(@NonNull String uuid, @NonNull SensorMeasurement measurement) {
        sensorMeasurementRepository.collect(uuid, measurement);
        List<SensorMeasurement> measurements = sensorMeasurementRepository.fetchLastThreeMeasurements(uuid);
        int aboveThreshold = (int) measurements.stream().filter(SensorMeasurement::isAboveThreshold).count();
        int belowThreshold = (int) measurements.stream().filter(SensorMeasurement::isBelowThreshold).count();

        if (aboveThreshold == Alert.LIMIT_FOR_ALARM) {
            if (alertRepository.getLatestOngoing(uuid).isEmpty()) {
                createNewAlarm(uuid, measurements);
            }
        } else if (belowThreshold == Alert.LIMIT_FOR_ALL_CLEAR) {
            alertRepository.getLatestOngoing(uuid).ifPresent(ongoingAlert -> {
                endAlarm(uuid, ongoingAlert, measurements.stream().findFirst().orElseThrow());
            });
        }
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
