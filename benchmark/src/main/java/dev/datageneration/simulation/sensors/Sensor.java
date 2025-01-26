package dev.datageneration.simulation.sensors;

import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.types.DataType;
import dev.datageneration.simulation.types.DoubleType;
import dev.datageneration.simulation.types.IntType;
import dev.datageneration.simulation.types.LongType;
import dev.datageneration.simulation.types.StringArrayType;
import dev.datageneration.util.FileJsonTarget;
import dev.datageneration.util.IterRegistry;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import org.json.JSONObject;

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

    private FileJsonTarget dataTarget;
    private FileJsonTarget dataWithErrorTarget;
    private FileJsonTarget errorTarget;

    private final long errorInterval;
    private final long maxErrorAlteration;
    private long dynamicErrorInterval;
    private long errorCounter = 0;


    public Sensor( SensorTemplate template, BenchmarkConfig config, IterRegistry registry ) {
        this.random = new Random( config.seed() );
        this.template = template;
        this.config = config;
        this.registry = registry;
        this.id = idBuilder++;
        this.errorInterval = config.calculateErrorInterval( this );
        this.maxErrorAlteration = (long) (this.errorInterval * config.maxErrorAlteration());
    }


    /**
     * attaches a data object
     */
    public abstract void attachDataPoint( JSONObject target );


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
        JSONObject data = new JSONObject();
        data.put( "id", id );
        data.put( "type", template.getType() );
        attachDataPoint( data );

        // Wrap each JSON object with a number prefix
        JSONObject freqObject = new JSONObject();
        freqObject.put( "data", data );
        freqObject.put( "tick", tick );

        dataTarget.attach( freqObject );

        if ( isError() ) {
            // we attach an error to both files
            attachError( tick );
            errorCounter = 0;
            recalculateError();
        } else {
            // we attach the normal object
            dataWithErrorTarget.attach( freqObject );
            errorCounter++;
        }

        counter = 0;
        metric.ticksGenerated++;
    }


    private void recalculateError() {
        // max shift of error in more or less interval between two errors
        this.dynamicErrorInterval = this.errorInterval + (((int) (random.nextFloat() * maxErrorAlteration * 2)) - maxErrorAlteration);
    }


    private boolean isError() {
        return errorCounter >= dynamicErrorInterval;
    }


    private void attachError( long tick ) throws IOException {
        metric.errorsGenerated++;

        JSONObject errorData = new JSONObject();
        errorData.put( "type", template.getType() );
        errorData.put( "id", id );
        errorData.put( "Error", "No Data" );

        JSONObject error = new JSONObject();
        error.put( "tick", tick );
        error.put( "data", errorData );

        dataWithErrorTarget.attach( error );
        errorTarget.attach( error );
    }

}
