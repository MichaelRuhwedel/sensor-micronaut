package com.mruhwedel;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.impl.InfluxDBMapper;

import javax.inject.Singleton;

@Factory
public class InfluxDbClientFactory {

    @Value("${influxdb.url}")
    private String influxUrl;

    @Value("${influxdb.username}")
    private String username;

    @Value("${influxdb.password}")
    private String password;

    @Value("${influxdb.database}")
    private String database;

    @Singleton
    InfluxDBMapper influxDBClient() {
        return new InfluxDBMapper(createDb());
    }

    private InfluxDB createDb() {
        return InfluxDBFactory
                .connect(influxUrl, username, password)
                .setDatabase(database);
    }
}
