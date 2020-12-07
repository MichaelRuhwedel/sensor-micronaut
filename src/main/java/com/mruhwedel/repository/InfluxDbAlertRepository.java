package com.mruhwedel.repository;

import com.mruhwedel.domain.Alert;
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
class InfluxDbAlertRepository implements AlertRepository {
    private final InfluxDBMapper influxDBMapper;

    private static Alert measurementToDomain(AlertMeasurement a) {
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
                .query(queryForLatestOngoing(uuid), AlertMeasurement.class).stream()
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
    private AlertMeasurement domainToMeasurement(String uuid, Alert alert) {
        return new AlertMeasurement(
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
                .query(queryAll, AlertMeasurement.class)
                .stream().map(InfluxDbAlertRepository::measurementToDomain)
                .collect(Collectors.toList());
    }

}
