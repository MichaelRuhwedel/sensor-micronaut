package com.mruhwedel;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@Singleton
@Getter(PACKAGE)
public class SensorService {

    @Inject
    public SensorService(SensorRepository sensorRepository, StatusCalculator statusCalculator) {
        this.sensorRepository = sensorRepository;
        this.statusCalculator = statusCalculator;
    }

    private final SensorRepository sensorRepository;
    private final StatusCalculator statusCalculator;

    @NonNull
    Optional<SensorStatus> readStatus(@NonNull String uuid) {
        Optional<SensorStatus> sensorStatus = sensorRepository.readStatus(uuid);
        log.info("{}: {}", uuid, sensorStatus.map(Enum::name).orElse("UNKNOWN"));
        return sensorStatus;
    }

    void recordAndUpdateStatus(@NonNull String uuid, @NonNull Measurement measurement) {
        List<QualifiedMeasurement> measurements = sensorRepository.fetchTwoPreviousMeasurements(uuid);
        QualifiedMeasurement qualifiedMeasurement = statusCalculator.calculateCurrentStatus(measurement, measurements);
        log.info("{}: {}@{}", uuid, qualifiedMeasurement.getSensorStatus(), measurement.getTime());
        sensorRepository.record(uuid, qualifiedMeasurement);
    }
}