package com.mruhwedel;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import lombok.*;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.impl.InfluxDBMapper;

import javax.inject.Singleton;

@Factory
@RequiredArgsConstructor
class InfluxDbFactory {

    private final DatabaseConfig databaseConfig;

    @Singleton
    InfluxDB createDb() {
        return InfluxDBFactory
                .connect(
                        databaseConfig.getUrl(),
                        databaseConfig.getUsername(),
                        databaseConfig.getPassword()
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
