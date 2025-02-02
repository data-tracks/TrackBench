package dev.trackbench.simulation.sensor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.error.ErrorHandler;
import dev.trackbench.simulation.type.DataType;
import dev.trackbench.simulation.type.DoubleType;
import dev.trackbench.simulation.type.LongType;
import dev.trackbench.simulation.type.NumberType;
import dev.trackbench.simulation.type.StringArrayType;
import dev.trackbench.util.CountRegistry;
import dev.trackbench.util.file.FileJsonTarget;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lombok.Getter;

public abstract class Sensor extends Thread {

    private static final AtomicInteger sensorIdBuilder = new AtomicInteger();
    private static final AtomicLong dpIdBuilder = new AtomicLong();

    //Info of all possible data types, with their possible configurations.
    public static Map<String, Supplier<DataType>> dataTypes = new HashMap<>() {{
        put( "temperature tire", () -> new NumberType( 80, 110 ) );
        put( "temperature brake", () -> new NumberType( 900, 1000 ) );
        put( "temperature c", () -> new NumberType( 1, 50 ) );
        put( "temperature engine", () -> new NumberType( 500, 600 ) );
        put( "temperature fuelP", () -> new NumberType( 20, 60 ) );
        put( "pressure psi", () -> new DoubleType( 25, 30 ) );
        put( "kph", () -> new DoubleType( 80, 360 ) );
        put( "mph", () -> new DoubleType( 60, 236.121 ) );
        put( "direction", () -> new NumberType( 0, 4 ) );
        put( "brake_pressure", () -> new NumberType( 1, 10 ) );
        put( "ml/min", () -> new LongType( 3000, 4000 ) );
        put( "on/off", () -> new NumberType( 0, 1 ) );
        put( "drs-zone", () -> new NumberType( 0, 3 ) );
        put( "test", () -> new LongType( 1, 10 ) );
        put( "wear", () -> new NumberType( 1, 90 ) );
        put( "liability", () -> new NumberType( 1, 96 ) );
        put( "acceleration", () -> new DoubleType( 1, 30 ) );
        put( "wind speed", () -> new DoubleType( 1, 200 ) );
        put( "g-lateral", () -> new DoubleType( 1, 6 ) );
        put( "g-longitudinal", () -> new DoubleType( 1, 5 ) );
        put( "throttlepedall", () -> new NumberType( 1, 100 ) );
        put( "rpm", () -> new LongType( 7000, 18000 ) );
        put( "fuelFlow", () -> new NumberType( 20, 120 ) );//kg/h
        put( "oil_pressure", () -> new DoubleType( 1.5, 7 ) );
        put( "fuel_pressure", () -> new DoubleType( 3, 5 ) );
        put( "exhaust", () -> new DoubleType( 0.7, 1.2 ) );//lambda ratio
        put( "turning_degree", () -> new NumberType( 1, 180 ) );
        put( "array_of_data", StringArrayType::new );
        put( "position", () -> new NumberType( 0, 4 ) );

    }};
    // each sensor needs its own random following the seed to behave consistent
    final public Random random;

    public final int id;
    private final CountRegistry registry;
    public int counter = 0;
    @Getter
    private final SensorTemplate template;

    @Getter
    private final BenchmarkConfig config;
    @Getter
    private final SensorMeta metric = new SensorMeta();

    @Getter
    private FileJsonTarget dataTarget;
    @Getter
    private FileJsonTarget dataWithErrorTarget;
    @Getter
    private FileJsonTarget errorTarget;

    private final ErrorHandler errorHandler;


    public Sensor( SensorTemplate template, BenchmarkConfig config, CountRegistry registry ) {
        this.template = template;
        this.config = config;
        this.registry = registry;
        this.id = sensorIdBuilder.getAndIncrement();
        this.random = new Random( config.seed() + id ); // this is still determinist as this is done in the same thread

        this.dataTarget = new FileJsonTarget( config.getSensorPath( this ), config );
        this.errorTarget = new FileJsonTarget( config.getErrorPath( this ), config );
        this.dataWithErrorTarget = new FileJsonTarget( config.getDataWithErrorPath( this ), config );

        this.errorHandler = new ErrorHandler( this );
    }

    protected Sensor( int id, SensorTemplate template, BenchmarkConfig config ) {
        this.id = id;
        this.template = template;
        this.config = config;
        this.random = new Random( config.seed() + id );
        this.errorHandler = new ErrorHandler( this );
        this.registry = null;
    }



    /**
     * attaches a data object
     */
    public abstract void attachDataPoint( ObjectNode target );


    @Override
    public void run() {

        try {
            for ( long tick = 0; tick < config.ticks(); tick++ ) {
                simulateTick( tick );

                if ( tick % config.updateTickVisual() == 0 ) {
                    registry.update( id, tick );
                }
            }
            errorHandler.getErrors().forEach(metric::addError);
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
        data.put( "sensorId", id );
        data.put( "type", template.getType() );
        attachDataPoint( data );

        // Wrap each JSON object with a number prefix
        ObjectNode dataWrapper = JsonNodeFactory.instance.objectNode();
        dataWrapper.putIfAbsent( "data", data );
        dataWrapper.put( "tick", tick );
        dataWrapper.put("id", dpIdBuilder.incrementAndGet() );

        dataTarget.attach( dataWrapper );

        handlePotentialError( tick, dataWrapper );

        counter = 0;
        metric.ticksGenerated++;
    }


    private void handlePotentialError( long tick, ObjectNode data ) throws IOException {
        if ( !errorHandler.handleError( tick, data ) ) {
            // we attach the normal object as we did not produce error
            dataWithErrorTarget.attach( data );
        }
    }


    public JsonNode toJson() {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put( "id", id );
        result.putIfAbsent( "template", template.toJson() );
        return result;
    }

    public static Sensor fromJson( ObjectNode node, BenchmarkConfig config ) {
        int id = node.get( "id" ).asInt();
        SensorTemplate template = SensorTemplate.fromJson(node.get( "template" ));
        return new DocSensor( id, template, config );
    }

}
