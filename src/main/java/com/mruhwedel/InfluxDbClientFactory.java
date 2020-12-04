package com.mruhwedel;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;

@Factory
public class InfluxDbClientFactory {

    @Value("${influxdb.url}")
    private String influxUrl;

    @Value("${influxdb.org}")
    private String org;

    @Value("${influxdb.token}")
    private char[] token;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Singleton
    InfluxDBClient influxDBClient(){
        return InfluxDBClientFactory.create(influxUrl, token, org, bucket);
    }
}
