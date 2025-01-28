package dev.datageneration.simulation.sensors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datageneration.simulation.BenchmarkConfig;
import dev.datageneration.simulation.types.DataType;
import dev.datageneration.util.CountRegistry;
import java.util.Map.Entry;

public class DocSensor extends Sensor {

    public DocSensor(SensorTemplate template, BenchmarkConfig config, CountRegistry registry ) {
        super(template, config, registry);
    }

    protected DocSensor(int id, SensorTemplate template, BenchmarkConfig config) {
        super(id, template, config );
    }


    @Override
    public void attachDataPoint( ObjectNode target ) {
        for ( Entry<String, DataType> nameType : getTemplate().getHeaderTypes().entrySet() ){
            target.putIfAbsent(nameType.getKey(), nameType.getValue().sample( nameType.getKey() )); // Add each sensor data point to JSON object
        }
    }
}
