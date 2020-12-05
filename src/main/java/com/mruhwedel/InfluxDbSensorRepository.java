package com.mruhwedel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBMapper;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.influxdb.dto.BoundParameterQuery.QueryBuilder.newQuery;

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
        return newQuery(
                "select * from co2_ppa " +
                        "where uuid=$uuid " +
                        "order by time desc " +
                        "limit $limit")
                .bind("uuid", uuid)
                .bind("limit", limit)
                .create();
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
                        Optional.ofNullable(measurement.getStatus())
                        .map(SensorStatus::valueOf)
                        .orElse(null)
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

    @Override
    public Optional<SensorMetrics> readMetrics(@NonNull String uuid) {
        Query query = newQuery(
                "SELECT MEAN(\"co2_level\"), MAX(\"co2_level\") " +
                        "FROM co2_ppa " +
                        "WHERE uuid = $uuid AND time > now() - 30d"
        )
                .bind("uuid", uuid)
                .create();
        log.info("{}", query.getCommand());

        return influxDB.query(query, MetricsCO2.class).stream()
                .findFirst()
                .map(it -> new SensorMetrics(
                        it.getMax(),
                        it.getMean())
                );
    }
}
