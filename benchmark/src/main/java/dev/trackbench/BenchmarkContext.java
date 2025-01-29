package dev.trackbench;

import dev.trackbench.simulation.SensorGenerator;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BenchmarkContext {

    @NonFinal
    @Setter
    private List<Sensor> sensors = new ArrayList<>();

    private System system;

    final BenchmarkConfig config;
    @Setter
    private Clock clock;


    public BenchmarkContext( BenchmarkConfig config, System system ) {
        this.config = config;
        this.system = system;
    }


    public void loadNecessities() {
        if(!sensors.isEmpty()) {
            log.info("Sensors already loaded");
            return;
        }
        log.info("Loading sensors...");
        sensors.addAll( SensorGenerator.loadSensors( config ) );
    }

}
