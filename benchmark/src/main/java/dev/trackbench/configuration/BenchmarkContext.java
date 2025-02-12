package dev.trackbench.configuration;

import dev.trackbench.configuration.workloads.ErrorWorkload;
import dev.trackbench.configuration.workloads.IdentityWorkload;
import dev.trackbench.configuration.workloads.WindowGroupWorkload;
import dev.trackbench.configuration.workloads.WindowWorkload;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.display.Display;
import dev.trackbench.simulation.SensorGenerator;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.simulation.sensor.SensorTemplate;
import dev.trackbench.simulation.type.DoubleType;
import dev.trackbench.simulation.type.LongType;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;
import dev.trackbench.util.TimeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Getter
public class BenchmarkContext {

    @NonFinal
    private List<Sensor> sensors = new ArrayList<>();

    private final System system;

    final BenchmarkConfig config;
    @Setter
    private Clock clock;

    private final Map<Integer, Workload> workloads = new HashMap<>();


    public BenchmarkContext( BenchmarkConfig config, System system ) {
        this.config = config;
        this.system = system;

        //workloads.put( 0, new IdentityWorkload( config ) );
        //workloads.put( 1, new ErrorWorkload( config ) );
        //workloads.put( 2, new WindowWorkload( config ) );
        setSensors( new ArrayList<>() );

    }


    public void setSensors( List<Sensor> sensors ) {
        this.sensors = sensors;
        List<SensorTemplate> distinct = sensors.stream().map( Sensor::getTemplate ).filter( t -> t.pickHeader( List.of( LongType.class, DoubleType.class ) ).isPresent() ).filter( distinctByKey( template -> template.getType().toLowerCase() ) ).toList();
        for ( SensorTemplate template : distinct ) {
            workloads.put( workloads.size(), new WindowGroupWorkload( template, config ) );
        }
    }


    public static <T> Predicate<T> distinctByKey( Function<? super T, Object> keyExtractor ) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent( keyExtractor.apply( t ), true ) == null;
    }


    public void loadNecessities() {
        if ( !sensors.isEmpty() ) {
            Display.INSTANCE.info( "✅ Sensors already loaded " );
            return;
        }
        Display.INSTANCE.info( "Loading sensors..." );
        sensors.addAll( SensorGenerator.loadSensors( config ) );
    }


    public void printProcessingTime() {
        printTime();
    }


    public void printGeneratingTime() {
        printTime();
    }


    public void printTime() {
        Display.INSTANCE.info( "• Execution Time: {}", tickToTime( config.ticks() ) );
        Display.INSTANCE.info( "• Ticks: {} ({}ns per tick)", config.ticks(), config.stepDurationNs() );
        Display.INSTANCE.nextLine();
    }


    @NotNull
    public String tickToTime( long tick ) {
        return TimeUtils.formatNanoseconds( config.stepDurationNs() * tick );
    }


    public Workload getWorkload( int n ) {
        return workloads.get( n );
    }

}
