package dev.datageneration.processing;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.BenchmarkContext;
import dev.datageneration.simulation.sensors.Sensor;
import dev.datageneration.util.JsonIterator;

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


    public ProcessingHandler( BenchmarkContext context, File target ) {
        this.config = context.getConfig();
        this.context = context;
        this.target = target;
        List<String> names = Arrays.stream( target.getName().split( "_" ) ).toList();
        int id = Integer.parseInt( names.getFirst() );
        Sensor sensor = context.getSensors().get( id );
        this.initialStep = sensor.getProcessing();
        /*
        // testing
        if ( sensor.getTemplate().getHeaderTypes().getFirst() instanceof DoubleType || sensor.getTemplate().getHeaderTypes().getFirst() instanceof IntType ) {
            this.windows = List.of( new Pair<>( new SlidingWindow( AvgAggregator::new, new SingleExtractor( "data." + sensor.getTemplate().getHeaders().getFirst() ), 1000 ), new FileJsonTarget( config.getSingleWindowPath( sensor ), config ) ) );
        }else {
            this.windows = List.of();
        }*/

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

    }

}
