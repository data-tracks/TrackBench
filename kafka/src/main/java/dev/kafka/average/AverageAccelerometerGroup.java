package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Accelerometer;
import dev.kafka.sensor.Sensor;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
public class AverageAccelerometerGroup extends Average {

    public double throttle;
    public double maxThrottle;
    public double minThrottle;


    public AverageAccelerometerGroup() {
        throttle = 0;
    }


    public AverageAccelerometerGroup( double throttle, long count, long tickStart, long tickEnd, long tick, long id ) {
        super( count, tickStart, tickEnd, tick, id );
        this.throttle = throttle;
        this.maxThrottle = Double.MAX_VALUE;
        this.minThrottle = Double.MAX_VALUE;
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


    public void updateAggregates( Accelerometer other ) {
        if ( maxThrottle < other.throttle ) {
            maxThrottle = other.throttle;
        }
        if ( minThrottle > other.throttle ) {
            minThrottle = other.throttle;
        }
    }


    public void next( Sensor entry ) {
        throttle += ((Accelerometer) entry).throttle;
    }

}
