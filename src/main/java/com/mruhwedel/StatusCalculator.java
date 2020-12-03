package com.mruhwedel;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class StatusCalculator {
    public SensorStatus calculateCurrentStatus(Measurement measurement, List<Measurement> measurementList) {
        return SensorStatus.OK;
    }
}
