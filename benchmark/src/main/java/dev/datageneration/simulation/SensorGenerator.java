package dev.datageneration.simulation;

import dev.datageneration.simulation.sensors.DocSensor;
import dev.datageneration.simulation.sensors.Sensor;
import dev.datageneration.simulation.sensors.SensorTemplate;
import dev.datageneration.util.IterRegistry;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

@Slf4j
public class SensorGenerator {

    /**
     * Creates Sensors and fills them with data accordingly to the given sensorArray.
     * Once the sensors are created it writes their data into csv files.
     */
    public static void start(BenchmarkConfig config) {
        // create Sensors
        RandomData.setSeed(RandomData.seed);
        createSensors(config);
    }

    /**
     * Creates sensors accordingly to the chosen amount.
     */
    public static void createSensors( BenchmarkConfig config ) {
        List<Sensor> sensors = new ArrayList<>();

        IterRegistry registry = new IterRegistry( config.ticks(), config.updateTickVisual() );

        for ( int i = 0; i < config.sensorAmount(); i++ ) {
            // pick random sensor
            int pickedSensorIndex = (int) RandomData.getRandom( 0, SensorTemplate.templates.size() );
            Sensor sensor = chooseSensor( SensorTemplate.templates.get( pickedSensorIndex ), config, registry );
            sensors.add( sensor );
        }

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

        saveSensors(sensors, config);
    }

    private static void saveSensors(List<Sensor> sensors, BenchmarkConfig config) {
        JSONArray array = new JSONArray();
        sensors.forEach( sensor -> array.put(sensor.getTemplate()));
        //writer = new FileWriter(config.getSensorPath());
    }

    public static Sensor chooseSensor( SensorTemplate template, BenchmarkConfig config, IterRegistry registry ) {
        return new DocSensor( template, config, registry );
    }
}
