package dev.datageneration.simulation;

import dev.datageneration.simulation.sensors.Sensor;

import java.io.File;
import java.util.List;

public class SensorGenerator {
    static File folder;

    public static void setFolder(File folder) {
        if ( !folder.exists() ) {
            boolean success = folder.mkdirs();
            if ( !success ) {
                throw new RuntimeException("Unable to create folder " + folder.getAbsolutePath());
            }
        }
        SensorGenerator.folder = folder;
    }

    /**
     * Creates Sensors and fills them with data accordingly to the given sensorArray.
     * Once the sensors are created it writes their data into csv files.
     */
    public static void creator( BenchmarkConfig config) {
        // create Sensors
        RandomData.setSeed(RandomData.seed);
        List<Sensor> sensors = RandomData.createSensors(config);
    }
}
