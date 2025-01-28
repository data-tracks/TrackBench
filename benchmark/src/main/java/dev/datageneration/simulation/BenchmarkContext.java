package dev.datageneration.simulation;

import dev.datageneration.simulation.sensors.Sensor;
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

    final BenchmarkConfig config;


    public BenchmarkContext( BenchmarkConfig config ) {
        this.config = config;
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
