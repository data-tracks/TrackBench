package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Accelerometer;
import dev.kafka.sensor.Sensor;
import dev.kafka.util.SerdeUtil.SerdeValues;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
public class AverageAccelerometer extends Average {

    public double throttle;


    public AverageAccelerometer() {
    }


    public AverageAccelerometer( double throttle, SerdeValues values ) {
        super( values );
        this.throttle = throttle;
    }


    public double getTemp() {
        return throttle;
    }


    public double getAverage() {
        double average;
        if ( count != 0 ) {
            average = throttle / count;
        } else {
            return throttle;
        }
        return average;
    }


    @Override
    public ProducerRecord<String, String> getRecord( String topic ) {
        double average = getAverage();

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "averageThrottlepedall", average );

        return wrapRecord( "accelerometer", topic, data );
    }


    @Override
    public void next( Sensor entry ) {
        throttle += ((Accelerometer) entry).throttle;
    }

}
