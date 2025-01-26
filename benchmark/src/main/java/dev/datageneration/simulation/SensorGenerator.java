package dev.datageneration.simulation;

import dev.datageneration.simulation.Sensors.Sensor;
import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorGenerator {
    static List<Sensor> sensors = new ArrayList<>();
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
     * @param sensorAmount int[]
     */
    public static void creator(long ticks, int sensorAmount) {
        // create Sensors
        RandomData.setSeed(RandomData.seed);
        sensors = RandomData.createSensors(ticks, sensorAmount);

        // write data to json file for each sensor
        for (int i = 0; i < sensorAmount; i++) {
            Sensor sensor = sensors.get(i);

            File jsonFile = new File((folder.toString() + "/" + sensor.getTemplate().getId() + "_" + sensor.getTemplate().getType() + ".json"));
            try {
                // create FileWriter object with file as parameter
                FileWriter jsonOutputFile = new FileWriter(jsonFile);

                //create JSONWriter object
                JSONArray data = sensor.getDataPoints();

                jsonOutputFile.write(data.toString(4));
                jsonOutputFile.close(); // Close JSON file writer

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
