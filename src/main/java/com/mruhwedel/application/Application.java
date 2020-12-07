package com.mruhwedel.application;

import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;

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
