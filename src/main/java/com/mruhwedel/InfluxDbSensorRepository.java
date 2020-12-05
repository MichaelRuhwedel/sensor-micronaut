package com.mruhwedel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBMapper;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@RequiredArgsConstructor // if there's only one, it'll be used for injection
public class InfluxDbSensorRepository implements SensorRepository {

    public static final int LIMIT_PREVIOUS = 2;

    @SuppressWarnings("unused") // @injected
    private final InfluxDBMapper influxDB;

    @Override
    public Optional<SensorStatus> readStatus(@NonNull String uuid) {
        Query query = createQuery(uuid, 1);

        return influxDB
                .query(query, MeasurementCO2.class)
                .stream()
                .findFirst()
                .map(MeasurementCO2::getStatus)
                .map(SensorStatus::valueOf);
    }

    @NotNull
    private Query createQuery(String uuid, int limit) {
        return new Query(String.format(
                "select * from co2_ppa " +
                        "where uuid='%s' " +
                        "order by time desc " +
                        "limit %d ",
                uuid, limit
        ));
    }

    @Override
    public @NonNull List<QualifiedMeasurement> fetchTwoPreviousMeasurements(@NonNull String uuid) {
        Query query = createQuery(uuid, LIMIT_PREVIOUS);

        List<QualifiedMeasurement> results = influxDB.query(query, MeasurementCO2.class)
                .stream()
                .filter(Objects::nonNull)
                .limit(LIMIT_PREVIOUS) // redundant, just in case the query would return more

                .peek(fr -> log.info("{}}", fr))
                .map(measurement -> new QualifiedMeasurement(
                        new Measurement(
                                measurement.getCo2Level(),
                                ZonedDateTime.ofInstant(measurement.getTime(), ZoneId.of("UTC"))
                        ),
                        SensorStatus.valueOf(measurement.getStatus())
                ))
                .collect(Collectors.toList());
        log.info("{}: {} of up to {} measurements in repository",
                uuid, results.size(), LIMIT_PREVIOUS);
        return results;
    }

    @Override
    public void record(@NonNull String uuid, QualifiedMeasurement qualifiedMeasurement) {
        Measurement measurement = qualifiedMeasurement.getMeasurement();
        MeasurementCO2 measurementCO2 = new MeasurementCO2(
                uuid,
                measurement.getTime().toInstant(),
                qualifiedMeasurement.getSensorStatus().name(),
                measurement.getCo2()
        );

        influxDB.save(measurementCO2);

    }

}
