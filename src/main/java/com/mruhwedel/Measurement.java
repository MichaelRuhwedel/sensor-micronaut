package com.mruhwedel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor // used by micronaut
@AllArgsConstructor // convenience
class Measurement {
    private int co2;
    private ZonedDateTime time;
}
