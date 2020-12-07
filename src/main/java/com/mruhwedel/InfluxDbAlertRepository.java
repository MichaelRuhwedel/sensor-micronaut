package com.mruhwedel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBMapper;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.influxdb.dto.BoundParameterQuery.QueryBuilder.newQuery;

@Singleton
@RequiredArgsConstructor
public class InfluxDbAlertRepository implements AlertRepository {
    private final InfluxDBMapper influxDBMapper;

    private static Alert measurementToDomain(AlertCo2 a) {
        return new Alert(
                a.getStartTime().atZone(ZoneId.systemDefault()),
                a.getEndTime() == -1 ? null : Instant.ofEpochSecond(a.getEndTime()).atZone(ZoneId.systemDefault()),
                a.getMeasurement1(),
                a.getMeasurement2(),
                a.getMeasurement3()
        );
    }

    @Override
    public Optional<Alert> getLatestOngoing(@NonNull String uuid) {
        return influxDBMapper
                .query(queryForLatestOngoing(uuid), AlertCo2.class).stream()
                .findFirst()
                .map(InfluxDbAlertRepository::measurementToDomain);

    }

    private BoundParameterQuery queryForLatestOngoing(String uuid) {
        return newQuery(
                "SELECT * FROM alert_co2 " +
                        " WHERE uuid = $uuid " +
                        " AND end_time = -1 " +
                        "ORDER BY time desc " +
                        "LIMIT 1"
        ).bind("uuid", uuid)
                .create();
    }

    @Override
    public void save(String uuid, Alert alert) {
        influxDBMapper.save(domainToMeasurement(uuid, alert));
    }

    @NotNull
    private AlertCo2 domainToMeasurement(String uuid, Alert alert) {
        return new AlertCo2(
                alert.getStartTime().toInstant(),
                Optional.ofNullable(alert.getEndTime()).map(et -> et.toInstant().getEpochSecond()).orElse(-1L),
                alert.getMeasurement1(),
                alert.getMeasurement2(),
                alert.getMeasurement3(),
                uuid
        );
    }

    @Override
    public List<Alert> getAll(@NonNull String uuid) {
        Query queryAll = newQuery(
                "SELECT * FROM alert_co2 " +
                        " WHERE uuid = $uuid " +
                        " ORDER BY time desc "
        )
                .bind("uuid", uuid)
                .create();

        return influxDBMapper
                .query(queryAll, AlertCo2.class)
                .stream().map(InfluxDbAlertRepository::measurementToDomain)
                .collect(Collectors.toList());
    }
}
