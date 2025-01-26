package dev.datageneration.simulation.Sensors;

import lombok.Value;
import lombok.experimental.NonFinal;

public class SensorMetric {
    public long ticksGenerated = 0;

    public SensorMetric() {
        ticksGenerated = 0;
    }
}
