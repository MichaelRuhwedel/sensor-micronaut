package com.mruhwedel;

import lombok.NonNull;

import javax.inject.Singleton;
import java.util.List;

import static com.mruhwedel.SensorStatus.*;

@Singleton
public class StatusCalculator {

    private static final int ALERT_THRESHOLD_CONSECUTIVE_WARN = 2;
    private static final int CO_PPM_WARN_THRESHOLD = 2000;

    @NonNull
    public QualifiedMeasurement calculateCurrentStatus(
            @NonNull Measurement measurement,
            @NonNull List<QualifiedMeasurement> previousMeasurements
    ) {
        SensorStatus sensorStatus = computeStatus(measurement.getCo2(), previousMeasurements);

        return new QualifiedMeasurement(measurement, sensorStatus);
    }

    private SensorStatus computeStatus(int co2, List<QualifiedMeasurement> previousMeasurements) {
        int countAboveThreshold = countAboveThreshold(previousMeasurements);

        if (co2 > CO_PPM_WARN_THRESHOLD) {
            return countAboveThreshold >= ALERT_THRESHOLD_CONSECUTIVE_WARN ?
                    ALERT :
                    WARN;
        } else {
            return countAboveThreshold > 0 && getPreviousStatus(previousMeasurements) == ALERT ?
                    ALERT :
                    OK;
        }
    }

    @NonNull
    private SensorStatus getPreviousStatus(List<QualifiedMeasurement> measurementList) {
        return measurementList.isEmpty() ?
                OK :
                measurementList // get last, assuming the measurements are sorted by time
                        .get(measurementList.size() - 1)
                        .getSensorStatus();
    }

    private int countAboveThreshold(List<QualifiedMeasurement> measurementList) {
        return (int) measurementList.stream()
                .map(it -> it.getMeasurement().getCo2())
                .filter(co2 -> co2 > CO_PPM_WARN_THRESHOLD)
                .count();
    }
}