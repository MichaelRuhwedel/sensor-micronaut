package com.mruhwedel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private int measurement1;
    private int measurement2;
    private int measurement3;
}
