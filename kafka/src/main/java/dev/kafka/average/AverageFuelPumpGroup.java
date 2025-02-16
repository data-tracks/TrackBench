package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.FuelPump;
import dev.kafka.sensor.Sensor;
import dev.kafka.util.SerdeUtil.SerdeValues;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
@NoArgsConstructor
public class AverageFuelPumpGroup extends Average {

    public double temp;
    public double flowRate;
    public double maxTemp;
    public double minTemp;
    public double maxFlow;
    public double minFlow;


    public AverageFuelPumpGroup(
            double temp,
            double flowRate,
            SerdeValues values ) {
        super( values );
        this.temp = temp;
        this.flowRate = flowRate;
        this.maxTemp = Double.MAX_VALUE;
        this.minTemp = Double.MIN_VALUE;
        this.maxFlow = Double.MAX_VALUE;
        this.minFlow = Double.MIN_VALUE;
    }


    public double getPressure() {
        return flowRate;
    }


    public double[] getAverage() {
        double[] average = new double[2];
        if ( count != 0 ) {
            average[0] = temp / count;
            average[1] = flowRate / count;
        } else {
            return new double[]{ temp, flowRate };
        }
        return average;
    }


    @Override
    public ProducerRecord<String, String> getRecord( String topic ) {
        double[] average = getAverage();

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "averageTemp", average[0] );
        data.put( "averageFlowRate", average[1] );

        return wrapRecord( "fuelPump", topic, data );
    }


    @Override
    public void next( Sensor sensor ) {
        FuelPump entry = (FuelPump) sensor;
        temp += entry.temp;
        flowRate += entry.flowRate;
    }

}
