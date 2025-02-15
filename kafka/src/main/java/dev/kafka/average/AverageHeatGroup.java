package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Heat;
import dev.kafka.sensor.Sensor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
@NoArgsConstructor
public class AverageHeatGroup extends Average {

    public double temp;
    public double minTemp;
    public double maxTemp;


    public AverageHeatGroup( double temp, int count, int tickStart, int tickEnd, int id, long tick ) {
        super( count, tickStart, tickEnd, id, tick );
        this.temp = temp;
        this.minTemp = Double.MAX_VALUE;
        this.maxTemp = Double.MIN_VALUE;
    }


    public double getAverage() {
        double average;
        if ( count != 0 ) {
            average = temp / count;
        } else {
            return temp;
        }
        return average;
    }


    @Override
    public ProducerRecord<String, String> getRecord( String topic ) {
        double average = getAverage();

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "averageTemp", average );

        return wrapRecord( "heat", topic, data );
    }


    @Override
    public void next( Sensor sensor ) {
        Heat entry = (Heat) sensor;
        temp += entry.temp;
    }

}
