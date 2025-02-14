package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Brake;
import dev.kafka.sensor.Sensor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

@Getter
@NoArgsConstructor
public class AverageBrakeGroup extends Average {

    public int temp;
    public int pressure;
    public int wear;
    public int minTemp;
    public int maxTemp;
    public int minPressure;
    public int maxPressure;


    public AverageBrakeGroup( int temp, int pressure, long count, long tickStart, long tickEnd, long id, int wear ) {
        super( count, tickStart, tickEnd, id );
        this.temp = temp;
        this.pressure = pressure;
        this.wear = wear;
        this.minTemp = Integer.MIN_VALUE;
        this.maxTemp = Integer.MAX_VALUE;
        this.minPressure = Integer.MIN_VALUE;
        this.maxPressure = Integer.MAX_VALUE;
    }


    public double[] getAverage() {
        double[] average = new double[]{ 0, 0, 0 };
        if ( count != 0 ) {
            if ( temp != 0 ) {
                average[0] = (double) temp / count;
            }
            if ( pressure != 0 ) {
                average[1] = (double) pressure / count;
            }
            if ( wear != 0 ) {
                average[2] = (double) wear / count;
            }
        } else {
            return new double[]{ temp, pressure, wear };
        }

        return average;
    }


    @Override
    public ProducerRecord<String, String> getRecord( String topic ) {
        double[] average = getAverage();

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "averageTemp", average[0] );
        data.put( "averagePressure", average[1] );
        data.put( "averageWear", average[2] );

        return wrapRecord( "brake", topic, data );
    }


    @Override
    public void next( Sensor other ) {
        Brake entry = (Brake) other;
        count += 1;
        temp += entry.temp;
        pressure += entry.pressure;
        wear += entry.wear;
    }

}
