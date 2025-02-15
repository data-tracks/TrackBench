package dev.kafka.streams;


import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import dev.kafka.average.Average;
import dev.kafka.average.AverageAccelerometer;
import dev.kafka.average.AverageAccelerometerGroup;
import dev.kafka.average.AverageBrake;
import dev.kafka.average.AverageBrakeGroup;
import dev.kafka.average.AverageEngine;
import dev.kafka.average.AverageEngineGroup;
import dev.kafka.average.AverageFuelPump;
import dev.kafka.average.AverageFuelPumpGroup;
import dev.kafka.average.AverageHeat;
import dev.kafka.average.AverageHeatGroup;
import dev.kafka.average.AverageSpeed;
import dev.kafka.average.AverageSpeedGroup;
import dev.kafka.average.AverageTire;
import dev.kafka.average.AverageTireGroup;
import dev.kafka.sensor.Accelerometer;
import dev.kafka.sensor.Brake;
import dev.kafka.sensor.Engine;
import dev.kafka.sensor.FuelPump;
import dev.kafka.sensor.Heat;
import dev.kafka.sensor.Sensor;
import dev.kafka.sensor.Speed;
import dev.kafka.sensor.Tire;
import dev.kafka.serialize.AverageAccelerometerGroupSerde;
import dev.kafka.serialize.AverageAccelerometerSerde;
import dev.kafka.serialize.AverageBrakeGroupSerde;
import dev.kafka.serialize.AverageBrakeSerde;
import dev.kafka.serialize.AverageEngineGroupSerde;
import dev.kafka.serialize.AverageEngineSerde;
import dev.kafka.serialize.AverageFuelPumpGroupSerde;
import dev.kafka.serialize.AverageFuelPumpSerde;
import dev.kafka.serialize.AverageHeatGroupSerde;
import dev.kafka.serialize.AverageHeatSerde;
import dev.kafka.serialize.AverageSpeedGroupSerde;
import dev.kafka.serialize.AverageSpeedSerde;
import dev.kafka.serialize.AverageTireGroupSerde;
import dev.kafka.serialize.AverageTireSerde;
import dev.kafka.util.Connection;
import dev.kafka.util.TrackProducer;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

@Slf4j
public class StreamSetup {

    public static final String INPUT_TOPIC = "f1";
    public static final String OUTPUT_TOPIC = "f2";

    public static final long SMALL_WINDOW_MS = 100;
    public static final long LARGE_WINDOW_MS = 5000;
    public static final String ERRORS_TOPIC = "errors";

    public static final Queue<Thread> threads = new ConcurrentLinkedQueue<>();


    public static void run() {
        setupStreams( "accelerometer", false, Accelerometer::from, AverageAccelerometer::new, new AverageAccelerometerSerde() );

        //setupStreams( "accelerometer", true, Accelerometer::from, AverageAccelerometerGroup::new, new AverageAccelerometerGroupSerde() );

        setupStreams( "break", false, Brake::from, AverageBrake::new, new AverageBrakeSerde() );

        //setupStreams( "break", true, Brake::from, AverageBrakeGroup::new, new AverageBrakeGroupSerde() );

        setupStreams( "engine", false, Engine::from, AverageEngine::new, new AverageEngineSerde() );

        //setupStreams( "engine", true, Engine::from, AverageEngineGroup::new, new AverageEngineGroupSerde() );

        setupStreams( "fuelPump", false, FuelPump::from, AverageFuelPump::new, new AverageFuelPumpSerde() );

        //setupStreams( "fuelPump", true, FuelPump::from, AverageFuelPumpGroup::new, new AverageFuelPumpGroupSerde() );

        setupStreams( "heat", false, Heat::from, AverageHeat::new, new AverageHeatSerde() );

        //setupStreams( "heat", true, Heat::from, AverageHeatGroup::new, new AverageHeatGroupSerde() );

        setupStreams( "speed", false, Speed::from, AverageSpeed::new, new AverageSpeedSerde() );

        //setupStreams( "speed", true, Speed::from, AverageSpeedGroup::new, new AverageSpeedGroupSerde() );

        setupStreams( "tire", false, Tire::from, AverageTire::new, new AverageTireSerde() );

        //setupStreams( "tire", true, Tire::from, AverageTireGroup::new, new AverageTireGroupSerde() );

        threads.add( WarningStream.run() );

        filterErrorsStream();

        threads.forEach( t -> {
            try {
                t.join();
            } catch ( InterruptedException e ) {
                throw new RuntimeException( e );
            }
        } );

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            try {
                threads.forEach( Thread::interrupt );
            } finally {
                log.info( "Interrupting all threads forcefully." );
            }
        } ) );

    }


    private static void filterErrorsStream() {
        TrackProducer<String, String> producer = Connection.getProducer( "errors-app" );

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sensorStream = builder.stream( INPUT_TOPIC );

        sensorStream
                .selectKey( ( k, v ) -> extractType( v ) )
                .filter( ( k, v ) -> {
                    try {
                        JsonNode node = MAPPER.readTree( v );
                        if ( node.has( "data" ) ) {
                            return node.get( "data" ).has( "Error" );
                        }
                        return true;
                    } catch ( JsonProcessingException e ) {
                        throw new RuntimeException( e );
                    }
                } )
                .to( ERRORS_TOPIC );

        // Start the Kafka Streams application
        KafkaStreams streams = new KafkaStreams( builder.build(), producer.getProperties() );
        cleanupStream( producer, streams );
    }


    private static <Avg extends Average> void setupStreams(
            String type,
            boolean grouped,
            Function<String, Sensor> sensorFunction,
            Supplier<Avg> avgSupplier,
            Serde<Avg> serde ) {
        String id = String.format( "%s%s-app", type, grouped ? "-group" : "" );

        if ( grouped ) {
            //runGroupedWindow( Connection.getProducer( "mini-" + id ), type, Duration.ofMillis( SMALL_WINDOW_MS ), "mini-group", sensorFunction, avgSupplier, serde );
            runGroupedWindow( Connection.getProducer( "large-" + id ), type, Duration.ofMillis( LARGE_WINDOW_MS ), "window", sensorFunction, avgSupplier, serde );
        } else {
            runAsIs( Connection.getProducer( id ), type, sensorFunction ); // only do it for "ungrouped"
            //runWindow( Connection.getProducer( "mini-" + id ), type, Duration.ofMillis( SMALL_WINDOW_MS ), "mini-group", sensorFunction, avgSupplier, serde );
            runWindow( Connection.getProducer( "large-" + id ), type, Duration.ofMillis( LARGE_WINDOW_MS ), "window", sensorFunction, avgSupplier, serde );
        }

    }


    private static void runAsIs( TrackProducer<String, String> producer, String type, Function<String, Sensor> sensorFunction ) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sensorStream = builder.stream( INPUT_TOPIC );

        sensorStream
                .selectKey( ( k, v ) -> extractType( v ) )
                .filter( ( k, v ) -> type.equals( k ) && !sensorFunction.apply( v ).error )
                .to( OUTPUT_TOPIC );

        // Start the Kafka Streams application
        KafkaStreams streams = new KafkaStreams( builder.build(), producer.getProperties() );
        cleanupStream( producer, streams );
    }


    public static <Avg extends Average> void runWindow(
            TrackProducer<String, String> producer,
            String type,
            Duration windowSize,
            String outputTopic,
            Function<String, Sensor> sensorFunction,
            Supplier<Avg> avgSupplier,
            Serde<Avg> serde ) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sensorStream = builder.stream( INPUT_TOPIC );

        KTable<Windowed<String>, Avg> aggregatedStream = sensorStream
                .selectKey( ( k, v ) -> extractType( v ) )
                .filter( ( k, v ) -> type.equals( k ) && !sensorFunction.apply( v ).error )
                .groupBy( ( key, value ) -> key )
                .windowedBy(
                        TimeWindows.ofSizeWithNoGrace( windowSize ) )
                .aggregate( avgSupplier::get, ( key, value, agg ) -> {
                    agg.nextValue( sensorFunction.apply( value ) );
                    return agg;
                }, Materialized.with( Serdes.String(), serde ) );

        finishStreamSetup( producer, outputTopic, aggregatedStream, builder );
    }


    private static String extractType( String value ) {
        try {
            JsonNode node = MAPPER.readTree( value );
            return node.get( "data" ).get( "type" ).asText();
        } catch ( Exception e ) {
            return "unknown";
        }
    }


    public static <Avg extends Average> void runGroupedWindow(
            TrackProducer<String, String> producer,
            String type,
            Duration windowSize,
            String outputTopic,
            Function<String, Sensor> sensorFunction,
            Supplier<Avg> avgSupplier,
            Serde<Avg> serde ) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> sensorStream = builder.stream( INPUT_TOPIC );

        KTable<Windowed<String>, Avg> aggregatedStream = sensorStream
                .selectKey( ( k, v ) -> extractType( v ) )
                .filter( ( k, v ) -> type.equals( k ) && !sensorFunction.apply( v ).error )
                .groupByKey( Grouped.with( Serdes.String(), Serdes.String() ) )
                .windowedBy( TimeWindows.ofSizeWithNoGrace( windowSize ) )
                .aggregate( avgSupplier::get, ( key, value, agg ) -> {
                    agg.nextValue( sensorFunction.apply( value ) );
                    return agg;
                }, Materialized.with( Serdes.String(), serde ) );

        finishStreamSetup( producer, outputTopic, aggregatedStream, builder );
    }


    private static void finishStreamSetup(
            TrackProducer<String, String> producer,
            String outputTopic,
            KTable<Windowed<String>, ? extends Average> aggregatedStream,
            StreamsBuilder builder ) {
        aggregatedStream
                .toStream()
                .foreach( ( key, value ) -> {
                    ProducerRecord<String, String> record = value.getRecord( outputTopic );

                    log.info( "Message: {}", record );
                    producer.send( record, ( metadata, exception ) -> {
                        if ( exception != null ) {
                            System.err.println( "Failed to send message: " + exception.getMessage() );
                        }
                    } );
                } );

        // Start the Kafka Streams application
        KafkaStreams streams = new KafkaStreams( builder.build(), producer.getProperties() );
        cleanupStream( producer, streams );
    }


    private static void cleanupStream( TrackProducer<String, String> producer, KafkaStreams streams ) {
        streams.cleanUp();
        Thread thread = new Thread( streams::start );
        thread.start();
        threads.add( thread );

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            try {
                producer.flush();
                streams.close();
            } finally {
                log.info( "Shutting down" );
            }
        } ) );
    }

}
