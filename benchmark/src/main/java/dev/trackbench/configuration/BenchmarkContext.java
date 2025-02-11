package dev.trackbench.configuration;

import dev.trackbench.configuration.workloads.ErrorWorkload;
import dev.trackbench.configuration.workloads.WindowGroupWorkload;
import dev.trackbench.configuration.workloads.WindowWorkload;
import dev.trackbench.display.Display;
import dev.trackbench.simulation.SensorGenerator;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.simulation.window.SlidingWindow;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;
import dev.trackbench.util.TimeUtils;
import dev.trackbench.configuration.workloads.IdentityWorkload;
import dev.trackbench.configuration.workloads.Workload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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

    private final Map<Integer, Workload> workloads = new HashMap<>();


    public BenchmarkContext( BenchmarkConfig config, System system ) {
        this.config = config;
        this.system = system;

        workloads.put( 0, new IdentityWorkload( config ) );
        workloads.put( 1, new ErrorWorkload( config ) );
        workloads.put( 2, new WindowWorkload( config ) );
        workloads.put( 3, new WindowGroupWorkload( "tire", config ) );
        workloads.put( 4, new WindowGroupWorkload( "break", config ) );
    }


    public void loadNecessities() {
        if(!sensors.isEmpty()) {
            Display.INSTANCE.info("Sensors already loaded");
            return;
        }
        Display.INSTANCE.info("Loading sensors...");
        sensors.addAll( SensorGenerator.loadSensors( config ) );
    }

    public void printProcessingTime(){
        printTime("The processing");
    }

    public void printGeneratingTime() {
        printTime("Generating data which");
    }

    public void printTime(String prefix) {
        Display.INSTANCE.info( "{} will take approx. {} to execute on the target system...", prefix, tickToTime(config.ticks()));
        Display.INSTANCE.info( "Ticks {} and {}ns per tick...", config.ticks(), config.stepDurationNs() );
    }

    @NotNull
    public String tickToTime(long tick) {
        return TimeUtils.formatNanoseconds(config.stepDurationNs() * tick);
    }


    public Workload getWorkload(int n) {
        return workloads.get( n );
    }

}
