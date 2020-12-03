package com.mruhwedel;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
class Measurements {
    private int co2;
    private ZonedDateTime time;
}
