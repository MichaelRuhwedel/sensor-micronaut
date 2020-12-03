package com.mruhwedel;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class StatusCalculator {
    public SensorStatus calculateCurrentStatus(List<Measurements> measurementsList) {
        return SensorStatus.OK;
    }
}
