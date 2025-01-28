package dev.datageneration.processing;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.BenchmarkContext;
import dev.datageneration.simulation.sensors.Sensor;
import dev.datageneration.util.CountRegistry;
import dev.datageneration.util.JsonIterator;

import dev.datageneration.util.SimpleCountRegistry;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessingHandler extends Thread {

    private final BenchmarkContext context;

    private final BenchmarkConfig config;

    private final File target;

    private final Step initialStep;
    private final SimpleCountRegistry registry;


    public ProcessingHandler( BenchmarkContext context, File target, SimpleCountRegistry registry ) {
        this.config = context.getConfig();
        this.context = context;
        this.target = target;
        this.registry = registry;
        List<String> names = Arrays.stream( target.getName().split( "_" ) ).toList();
        int id = Integer.parseInt( names.getFirst() );
        Sensor sensor = context.getSensors().get( id );
        this.initialStep = sensor.getProcessing();

    }


    @Override
    public void run() {
        JsonIterator iterator = new JsonIterator( config, target );

        while ( iterator.hasNext() ) {
            JsonNode node = iterator.next();
            JsonNode tick = Objects.requireNonNull( node ).get( "tick" );
            this.initialStep.next( new Value( tick.asLong(), node ) );
        }
        this.initialStep.close();
        this.registry.finish();
    }

}
