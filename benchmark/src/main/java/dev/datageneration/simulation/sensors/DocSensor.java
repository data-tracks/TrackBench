package dev.datageneration.simulation.sensors;

import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.types.DataType;
import dev.datageneration.util.IterRegistry;
import java.util.Map.Entry;
import org.json.JSONObject;

public class DocSensor extends Sensor {

    public DocSensor(SensorTemplate template, BenchmarkConfig config, IterRegistry registry ) {
        super(template, config, registry);
    }


    @Override
    public void attachDataPoint( JSONObject target) {
        for ( Entry<String, DataType> nameType : getTemplate().getDataTypes().entrySet() ){
            target.put(nameType.getKey(), nameType.getValue().sample( nameType.getKey() )); // Add each sensor data point to JSON object
        }
    }
}
