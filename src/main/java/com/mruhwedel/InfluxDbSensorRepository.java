package com.mruhwedel;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.influxdb.client.domain.WritePrecision.S;

@Slf4j
@Singleton
@RequiredArgsConstructor // if there's only one, it'll be used for injection
public class InfluxDbSensorRepository implements SensorRepository {

    private static final WritePrecision WRITE_PRECISION = S; // the specs say that's all we need
    public static final String MEASUREMENT_NAME = "co2";
    public static final int LIMIT_PREVIOUS = 2;

    @SuppressWarnings("unused") // @injected
    private final InfluxDBClient influxDBClient;

    @Override
    public Optional<SensorStatus> readStatus(@NonNull String uuid) {
        String query = createQuery(uuid, 1);

        Optional<SensorStatus> status = influxDBClient.getQueryApi()
                .query(query).stream()
                .flatMap(t -> t.getRecords().stream())
                .findFirst()
                .map(fr -> (String) fr.getValueByKey("status"))
                .map(SensorStatus::valueOf);

        log.info("Read {}", status);
        return status;
    }

    @NotNull
    private String createQuery(String uuid, int limit) {
        return String.format(
                "data \n" +
                        "  |> sort(columns: [\"_time\"]) \n" +
                        "  |> filter(fn: (r) => r.uuid == %s) \n" +
                        "  |> limit(n: %d)",
                uuid, limit
        );
    }

    @Override
    public @NonNull List<QualifiedMeasurement> fetchTwoPreviousMeasurements(@NonNull String uuid) {
        String query = createQuery(uuid, LIMIT_PREVIOUS);

        List<QualifiedMeasurement> status = influxDBClient.getQueryApi()
                .query(query).stream()
                .flatMap(t -> t.getRecords().stream())
                .filter(Objects::nonNull)
                .limit(LIMIT_PREVIOUS) // redundant, just in case the query would return more
                .map(fr -> new QualifiedMeasurement(
                        new Measurement(
                                (int) fr.getValue(),
                                ZonedDateTime.ofInstant(fr.getTime(), ZoneId.systemDefault())
                        ),
                        SensorStatus.valueOf((String) fr.getValueByKey("status"))
                ))
                .collect(Collectors.toList());
        log.info("{}: {} of up to {} measurements in repository",
                uuid, status.size(), LIMIT_PREVIOUS);

        return status;


    }

    @Override
    public void record(@NonNull String uuid, QualifiedMeasurement qualifiedMeasurement) {
        log.info("recording");
        Instant time = qualifiedMeasurement.getMeasurement().getTime().toInstant();

        Point co2Point = Point
                .measurement(MEASUREMENT_NAME)
                .addField("uuid", uuid)
                .addField("status", qualifiedMeasurement.getSensorStatus().name())
                .addField("value", qualifiedMeasurement.getMeasurement().getCo2())
                .time(time, WRITE_PRECISION);

        influxDBClient.getWriteApi().writePoint(co2Point);

    }

}
