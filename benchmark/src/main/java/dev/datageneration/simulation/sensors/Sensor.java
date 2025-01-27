package dev.datageneration.simulation.sensors;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datageneration.aggregate.SingleExtractor;
import dev.datageneration.processing.Formatter;
import dev.datageneration.processing.IdentityStep;
import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.ErrorHandler;
import dev.datageneration.simulation.types.DataType;
import dev.datageneration.simulation.types.DataType.NumericType;
import dev.datageneration.simulation.types.DoubleType;
import dev.datageneration.simulation.types.IntType;
import dev.datageneration.simulation.types.LongType;
import dev.datageneration.simulation.types.StringArrayType;
import dev.datageneration.util.FileJsonTarget;
import dev.datageneration.util.FileStep;
import dev.datageneration.util.IterRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import lombok.Getter;

public abstract class Sensor extends Thread {

    private static int idBuilder = 0;

    //Info of all possible data types, with their possible configurations.
    public static Map<String, DataType> dataTypes = new HashMap<>() {{
        put( "temperature tire", new IntType( 80, 110 ) );
        put( "temperature brake", new IntType( 900, 1000 ) );
        put( "temperature c", new IntType( 1, 50 ) );
        put( "temperature engine", new IntType( 500, 600 ) );
        put( "temperature fuelP", new IntType( 20, 60 ) );
        put( "pressure psi", new DoubleType( 25, 30 ) );
        put( "kph", new DoubleType( 80, 360 ) );
        put( "mph", new DoubleType( 60, 236.121 ) );
        put( "direction", new IntType( 0, 4 ) );
        put( "brake_pressure", new IntType( 1, 10 ) );
        put( "ml/min", new LongType( 3000, 4000 ) );
        put( "on/off", new IntType( 0, 1 ) );
        put( "drs-zone", new IntType( 0, 3 ) );
        put( "test", new LongType( 1, 10 ) );
        put( "wear", new IntType( 1, 90 ) );
        put( "liability", new IntType( 1, 96 ) );
        put( "acceleration", new DoubleType( 1, 30 ) );
        put( "wind speed", new DoubleType( 1, 200 ) );
        put( "g-lateral", new DoubleType( 1, 6 ) );
        put( "g-longitudinal", new DoubleType( 1, 5 ) );
        put( "throttlepedall", new IntType( 1, 100 ) );
        put( "rpm", new LongType( 7000, 18000 ) );
        put( "fuelFlow", new IntType( 20, 120 ) );//kg/h
        put( "oil_pressure", new DoubleType( 1.5, 7 ) );
        put( "fuel_pressure", new DoubleType( 3, 5 ) );
        put( "exhaust", new DoubleType( 0.7, 1.2 ) );//lambda ratio
        put( "turning_degree", new IntType( 1, 180 ) );
        put( "array_of_data", new StringArrayType() );
        put( "position", new IntType( 0, 4 ) );

    }};
    // each sensor needs its own random following the seed to behave consistent
    final public Random random;

    public final int id;
    private final IterRegistry registry;
    public int counter = 0;
    @Getter
    private final SensorTemplate template;
    @Getter
    private final BenchmarkConfig config;
    @Getter
    private final SensorMetric metric = new SensorMetric();

    @Getter
    private FileJsonTarget dataTarget;
    @Getter
    private FileJsonTarget dataWithErrorTarget;
    @Getter
    private FileJsonTarget errorTarget;

    private final ErrorHandler errorHandler;


    public Sensor( SensorTemplate template, BenchmarkConfig config, IterRegistry registry ) {
        this.template = template;
        this.config = config;
        this.registry = registry;
        this.id = idBuilder++;
        this.random = new Random( config.seed() + id ); // this is still determinist as this is done in the same thread
        this.errorHandler = new ErrorHandler( this );
    }


    /**
     * attaches a data object
     */
    public abstract void attachDataPoint( ObjectNode target );


    @Override
    public void run() {
        this.dataTarget = new FileJsonTarget( config.getDataPath( this ), config );
        this.errorTarget = new FileJsonTarget( config.getErrorPath( this ), config );
        this.dataWithErrorTarget = new FileJsonTarget( config.getDataWithErrorPath( this ), config );
        try {
            for ( long tick = 0; tick < config.ticks(); tick++ ) {
                simulateTick( tick );

                if ( tick % config.updateTickVisual() == 0 ) {
                    registry.update( id, tick );
                }
            }
            // emptying last batch
            dataTarget.close();
            dataWithErrorTarget.close();
            errorTarget.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        registry.update( id, config.ticks() );
    }


    public void simulateTick( long tick ) throws IOException {
        counter++;
        if ( counter < this.template.getTickLength() ) {
            return;
        }
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "id", id );
        data.put( "type", template.getType() );
        attachDataPoint( data );

        // Wrap each JSON object with a number prefix
        ObjectNode dataWrapper = JsonNodeFactory.instance.objectNode();
        dataWrapper.putIfAbsent( "data", data );
        dataWrapper.put( "tick", tick );

        dataTarget.attach( dataWrapper );

        handlePotentialError( tick, dataWrapper );

        counter = 0;
        metric.ticksGenerated++;
    }


    private void handlePotentialError( long tick, ObjectNode data ) throws IOException {
        if ( !errorHandler.handleError( tick ) ) {
            // we attach the normal object as we did not produce error
            dataWithErrorTarget.attach( data );
        }
    }

    public Step getProcessing() {
        List<Step> steps = new ArrayList<>();


        for ( Entry<String, DataType> nameType : template.getDataTypes().entrySet() ) {
            FileStep file = new FileStep( List.of(), new FileJsonTarget( config.getSingleWindowPath( this, nameType.getKey() ), config ) );
            if ( nameType.getValue() instanceof NumericType ) {
                Step step;
                if ( nameType.getValue() instanceof DoubleType ) {
                    step = new Formatter( List.of( file ), v -> List.of( new Value( v.tick(), v.node() ) ) );
                } else if ( nameType.getValue() instanceof IntType ) {
                    step = new Formatter( List.of( file ), v -> List.of( new Value( v.tick(), v.node() ) ) );
                } else {
                    continue;
                }
                steps.add( new SingleExtractor( List.of( step ), "data." + nameType.getKey() ) );
            }

        }

        return new IdentityStep( steps );
    }

}
