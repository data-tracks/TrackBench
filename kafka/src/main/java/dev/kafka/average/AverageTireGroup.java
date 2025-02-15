package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Sensor;
import dev.kafka.sensor.Tire;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
@NoArgsConstructor
public class AverageTireGroup extends Average {

    public double temp;
    public double pressure;

    public int position;
    public int wear;
    public double minTemp;
    public double maxTemp;
    public double minPressure;
    public double maxPressure;


    public AverageTireGroup( double temp, double pressure, int count, int tickStart, int tickEnd, int id, int position, int wear, long tick ) {
        super( count, tickStart, tickEnd, id, tick );
        this.temp = temp;
        this.pressure = pressure;
        this.position = position;
        this.wear = wear;
        this.minTemp = Double.MAX_VALUE;
        this.maxTemp = Double.MIN_VALUE;
        this.minPressure = Double.MAX_VALUE;
        this.maxPressure = Double.MIN_VALUE;
    }


    public double[] getAverage() {
        double[] average = new double[3];
        if ( count != 0 ) {
            average[0] = temp / count;
            average[1] = pressure / count;
            average[2] = (double) wear / count;
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
        data.put( "position", position );

        return wrapRecord( "tire", topic, data );
    }


    @Override
    public void next( Sensor sensor ) {
        Tire entry = (Tire) sensor;
        temp += entry.temp;
        pressure += entry.pressure;
        wear += entry.wear;
    }

}

