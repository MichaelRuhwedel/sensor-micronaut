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

    private static Measurement measurementToDomain(MeasurementCO2 measurement) {
        return new Measurement(
                measurement.getCo2Level(),
                ZonedDateTime.ofInstant(measurement.getTime(), ZoneId.of("UTC"))
        );
    }

    @Override
    public Optional<Measurement> fetchCurrent(@NonNull String uuid) {
        return influxDB.query(createQuery(uuid, 1), MeasurementCO2.class).stream()
                .findFirst()
                .map(InfluxDbSensorRepository::measurementToDomain);
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
    public @NonNull List<Measurement> fetchLastThreeMeasurements(@NonNull String uuid) {
        List<Measurement> results = influxDB.query(createQuery(uuid, 3), MeasurementCO2.class)
                .stream()
                .map(InfluxDbSensorRepository::measurementToDomain)
                .collect(Collectors.toList());
        log.info("{}: {} of up to {} measurements in repository",
                uuid, results.size(), LIMIT_PREVIOUS);
        return results;
    }

    @Override
    public void record(@NonNull String uuid, Measurement measurement) {
        MeasurementCO2 measurementCO2 = new MeasurementCO2(
                uuid,
                measurement.getTime().toInstant(),
                measurement.getCo2()
        );

        influxDB.save(measurementCO2);
    }

    @Override
    public Optional<SensorMetrics> readMetrics(@NonNull String uuid) {
        Query query = newQuery(
                "SELECT ROUND(MEAN(\"co2_level\")) as mean, MAX(\"co2_level\") " +
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
