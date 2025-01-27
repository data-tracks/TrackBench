package dev.datageneration.simulation;

import dev.datageneration.simulation.sensors.DocSensor;
import dev.datageneration.simulation.sensors.Sensor;
import dev.datageneration.simulation.sensors.SensorTemplate;
import dev.datageneration.util.IterRegistry;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

@Slf4j
public class SensorGenerator {

    /**
     * Creates Sensors and fills them with data accordingly to the given sensorArray.
     * Once the sensors are created it writes their data into csv files.
     */
    public static void start(BenchmarkContext context) {
        // create Sensors
        RandomData.setSeed(RandomData.seed);
        createSensors(context);
    }

    /**
     * Creates sensors accordingly to the chosen amount.
     */
    public static void createSensors( BenchmarkContext context ) {
        List<Sensor> sensors = new ArrayList<>();

        IterRegistry registry = new IterRegistry( context.getConfig().ticks(), context.getConfig().updateTickVisual() );

        for ( int i = 0; i < context.getConfig().sensorAmount(); i++ ) {
            // pick random sensor
            int pickedSensorIndex = (int) RandomData.getRandom( 0, SensorTemplate.templates.size() );
            Sensor sensor = chooseSensor( SensorTemplate.templates.get( pickedSensorIndex ), context.getConfig(), registry );
            sensors.add( sensor );
        }

        saveSensors(sensors, context.getConfig());
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

        log.info( "###\nDone generating..." );

        // we print the summary for debug purposes
        for ( Sensor sensor : sensors ) {
            log.info( "Sensor: {}, MetaData: {}", sensor.getTemplate().getType(), sensor.getMetric() );
        }


    }

    private static void saveSensors(List<Sensor> sensors, BenchmarkConfig config) {
        JSONArray array = new JSONArray();

        sensors.forEach( sensor -> array.put(sensor.getTemplate().toJson()));
        try {
            FileWriter writer = new FileWriter( config.getSensorPath() );
            writer.write( array.toString(4) );
            writer.flush();
            writer.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public static Sensor chooseSensor( SensorTemplate template, BenchmarkConfig config, IterRegistry registry ) {
        return new DocSensor( template, config, registry );
    }
}
