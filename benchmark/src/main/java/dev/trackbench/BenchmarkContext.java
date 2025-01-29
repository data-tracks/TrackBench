package dev.trackbench;

import dev.trackbench.simulation.SensorGenerator;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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

    public void printProcessingTime(){
        printTime("The processing");
    }

    public void printGeneratingTime() {
        printTime("Generating data which");
    }

    public void printTime(String prefix) {
        log.info( "{} will take approx. {}...", prefix, tickToTime(config.ticks()));
        log.info( "Ticks {} and {}ns per tick...", config.ticks(), config.stepDurationNs() );
    }

    @NotNull
    public String tickToTime(long tick) {
        return formatNanoseconds(config.stepDurationNs() * tick);
    }


    public static String formatNanoseconds( long nanoseconds ) {
        Duration duration = Duration.ofNanos( nanoseconds );

        if ( duration.toDays() > 0 ) {
            return duration.toDays() + " days";
        } else if ( duration.toHours() > 0 ) {
            return duration.toHours() + " hours";
        } else if ( duration.toMinutes() > 0 ) {
            return duration.toMinutes() + " minutes";
        } else if ( duration.getSeconds() > 0 ) {
            return duration.getSeconds() + " seconds";
        } else if ( duration.toMillis() > 0 ) {
            return duration.toMillis() + " milliseconds";
        } else if ( duration.toNanos() >= 1000 ) {
            return duration.toNanos() / 1000 + " microseconds";
        } else {
            return nanoseconds + " nanoseconds";
        }
    }

}
