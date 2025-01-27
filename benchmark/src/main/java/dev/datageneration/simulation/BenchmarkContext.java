package dev.datageneration.simulation;

import dev.datageneration.simulation.sensors.Sensor;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.NonFinal;

@Getter
public class BenchmarkContext {

    @NonFinal
    @Setter
    private List<Sensor> sensors = new ArrayList<>();

    final BenchmarkConfig config;


    public BenchmarkContext( BenchmarkConfig config ) {
        this.config = config;
    }

}
