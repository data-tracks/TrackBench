package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Sensor;
import dev.kafka.sensor.Speed;
import dev.kafka.util.SerdeUtil.SerdeValues;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
@NoArgsConstructor
public class AverageSpeed extends Average {

    public double speed;
    public double wind;


    public AverageSpeed( double speed, double wind, SerdeValues values ) {
        super( values );
        this.speed = speed;
        this.wind = wind;
    }


    public double getTemp() {
        return speed;
    }


    public double getPressure() {
        return wind;
    }


    public double[] getAverage() {
        double[] average = new double[2];
        if ( count != 0 ) {
            average[0] = speed / count;
            average[1] = wind / count;
        } else {
            return new double[]{ speed, wind };
        }

        return average;
    }


    @Override
    public ProducerRecord<String, String> getRecord( String topic ) {
        double[] average = getAverage();

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "averageSpeed kph", average[0] );
        data.put( "averageSpeed mph", (average[0] / 1.609344) );
        data.put( "averageWindSpeed", average[1] );

        return wrapRecord( "speed", topic, data );
    }


    @Override
    public void next( Sensor sensor ) {
        Speed entry = (Speed) sensor;
        speed += entry.speed;
        wind += entry.wind;
    }

}
