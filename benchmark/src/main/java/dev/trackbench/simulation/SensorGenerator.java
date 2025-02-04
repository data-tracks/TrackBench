package dev.trackbench.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.simulation.sensor.DocSensor;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.simulation.sensor.SensorTemplate;
import dev.trackbench.util.CountRegistry;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SensorGenerator {

    /**
     * Creates Sensors and fills them with data accordingly to the given sensorArray.
     * Once the sensors are created it writes their data into csv files.
     */
    public static void start( BenchmarkContext context ) {
        // create Sensors
        RandomData.setSeed( RandomData.seed );
        createSensors( context );
    }


    /**
     * Creates sensors accordingly to the chosen amount.
     */
    public static void createSensors( BenchmarkContext context ) {
        List<Sensor> sensors = new ArrayList<>();

        CountRegistry registry = new CountRegistry( context.getConfig().ticks(), context.getConfig().updateTickVisual(), " ticks" );

        for ( int i = 0; i < context.getConfig().sensorAmount(); i++ ) {
            // pick random sensor
            int pickedSensorIndex = (int) RandomData.getRandom( 0, SensorTemplate.templates.size() );
            Sensor sensor = chooseSensor( SensorTemplate.templates.get( pickedSensorIndex ).get(), context.getConfig(), registry );
            sensors.add( sensor );
        }

        saveSensors( sensors, context.getConfig() );
        List<Sensor> sensors2 = loadSensors( context.getConfig() );
        if ( sensors2.size() != sensors.size() ) {
            throw new RuntimeException( "Sensor loading is wrong" );
        }

        context.setSensors( sensors );

        try {
            // start all benchmarks
            sensors.forEach( Sensor::start );

            // wait for all sensors to finish
            for ( Sensor sensor : sensors ) {
                sensor.join();
            }

            log.info( "###\nFinishing last batch..." );

        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        registry.done();
        log.info( "###\nDone generating..." );

        // we print the summary for debug purposes
        for ( Sensor sensor : sensors ) {
            log.info( "Sensor: {}, MetaData: {}", sensor.getTemplate().getType(), sensor.getMetric() );
        }
        log.info("Max id is {}", Sensor.getDpIdBuilder().get());


    }


    private static void saveSensors( List<Sensor> sensors, BenchmarkConfig config ) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();

        sensors.forEach( sensor -> array.add( sensor.toJson() ) );
        try {
            FileWriter writer = new FileWriter( config.getSensorPath(), false );
            writer.write( array.toPrettyString() );
            writer.flush();
            writer.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }


    public static List<Sensor> loadSensors( BenchmarkConfig config ) {
        List<Sensor> sensors = new ArrayList<>();

        try {
            JsonNode array = new ObjectMapper().readTree( config.getSensorPath() );
            if ( !array.isArray() ) {
                throw new RuntimeException( "Sensor path is not a JSON array" );
            }
            for ( JsonNode node : array ) {
                if ( !node.isObject() ) {
                    throw new RuntimeException( "Sensor is not a JSON object" );
                }
                sensors.add( Sensor.fromJson( (ObjectNode) node, config ) );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        return sensors;
    }


    public static Sensor chooseSensor( SensorTemplate template, BenchmarkConfig config, CountRegistry registry ) {
        return new DocSensor( template, config, registry );
    }

}
