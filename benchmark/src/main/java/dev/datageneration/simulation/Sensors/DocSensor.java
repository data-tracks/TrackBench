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
        for ( Entry<String, DataType> nameType :template.getDataTypes().entrySet() ){
            target.put(nameType.getKey(), nameType.getValue().sample( nameType.getKey() )); // Add each sensor data point to JSON object
        }
    }
}
