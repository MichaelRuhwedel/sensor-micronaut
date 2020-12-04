package com.mruhwedel;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.Micronaut;

import javax.inject.Singleton;

@Factory
public class Application {



    public static void main(String[] args) {
        Micronaut
                .build(args)
                .mainClass(Application.class)
                .defaultEnvironments("dev")
                .start();
    }

}
