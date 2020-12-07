package com.mruhwedel.application;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDBException;

import javax.inject.Singleton;

import static io.micronaut.http.HttpStatus.SERVICE_UNAVAILABLE;

@Produces
@Singleton
@Requires(classes = {InfluxDBException.class, ExceptionHandler.class})
@Slf4j
public class InfluxDbExceptionHandler implements ExceptionHandler<InfluxDBException, HttpResponse<Void>> {
    @Override
    public HttpResponse<Void> handle(HttpRequest request, InfluxDBException exception) {
        log.error("Looks like the db is not running: {}", exception.getMessage());
        return HttpResponse.status(SERVICE_UNAVAILABLE);
    }

}
