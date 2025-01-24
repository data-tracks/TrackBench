package dev.datageneration.simulation.Sensors;

import dev.datageneration.simulation.types.DataType;
import java.util.Map.Entry;
import org.json.JSONObject;

public class DocSensor extends Sensor {

    public DocSensor(SensorTemplate template) {
        super(template);
    }


    @Override
    public void attachDataPoint( JSONObject target) {
        JSONObject sensorDataObject = new JSONObject();

        for ( Entry<String, DataType> nameType :template.getDataTypes().entrySet() ){
            sensorDataObject.put(nameType.getKey(), Double.valueOf(nameType.getValue().sample( nameType.getKey() ))); // Add each sensor data point to JSON object
        }
    }
}
