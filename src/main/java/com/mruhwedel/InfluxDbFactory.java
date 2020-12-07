package com.mruhwedel;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import lombok.*;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.impl.InfluxDBMapper;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Factory
@RequiredArgsConstructor
class InfluxDbFactory {

    private final DatabaseConfig databaseConfig;

    @Singleton
    InfluxDB createDb() {
        ConnectionPool connectionPool = new ConnectionPool(
                40,
                5, TimeUnit.SECONDS
        );
        return InfluxDBFactory
                .connect(
                        databaseConfig.getUrl(),
                        databaseConfig.getUsername(),
                        databaseConfig.getPassword(),
                        new OkHttpClient
                                .Builder()
                                .connectionPool(connectionPool)
                )
                .setDatabase(databaseConfig.getDatabase());
    }

    @Factory
    @RequiredArgsConstructor
    static class MapperFactory {
        private final InfluxDB influxDB;

        @Singleton
        InfluxDBMapper influxDBMapper() {
            return new InfluxDBMapper(influxDB);
        }
    }

    @Data
    @ConfigurationProperties("influxdb")
    static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private String database;
    }

}
