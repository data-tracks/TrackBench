package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.FuelPump;
import dev.kafka.sensor.Sensor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
@NoArgsConstructor
public class AverageFuelPump extends Average {

    public double temp;
    public double flowRate;


    public AverageFuelPump( double temp, double flowRate, long count, long tickStart, long tickEnd, long id ) {
        super( count, tickStart, tickEnd, id );
        this.temp = temp;
        this.flowRate = flowRate;
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
