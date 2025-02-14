package dev.kafka.average;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.kafka.sensor.Engine;
import dev.kafka.sensor.Sensor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;

@Getter
@NoArgsConstructor
public class AverageEngineGroup extends Average {

    public int temp;
    public long rpm;
    public int fuelFlow;
    public double oilPressure;
    public double fuelPressure;
    public double exhaust;
    public long maxRPM;
    public long minRPM;
    public double minOilP;
    public double maxOilP;
    public double minExhaust;
    public double maxExhaust;
    public double minFuelP;
    public double maxFuelP;


    public AverageEngineGroup(
            int temp,
            long rpm,
            int fuelFlow,
            double oilPressure,
            double fuelPressure,
            double exhaust,
            int count,
            int tickStart,
            int tickEnd,
            int id ) {
        super( count, tickStart, tickEnd, id );
        this.temp = temp;
        this.rpm = rpm;
        this.fuelFlow = fuelFlow;
        this.oilPressure = oilPressure;
        this.fuelPressure = fuelPressure;
        this.exhaust = exhaust;
        this.maxRPM = Long.MAX_VALUE;
        this.minRPM = Long.MIN_VALUE;
        this.maxOilP = Double.MAX_VALUE;
        this.minOilP = Double.MIN_VALUE;
        this.minExhaust = Double.MAX_VALUE;
        this.maxExhaust = Double.MIN_VALUE;
        this.minFuelP = Double.MAX_VALUE;
        this.maxFuelP = Double.MIN_VALUE;
    }


    public double getTemp() {
        return temp;
    }


    public double[] getAverage() {
        double[] average = new double[6];
        if ( count != 0 ) {
            if ( temp != 0 ) {
                average[0] = (double) temp / count;
            }
            if ( rpm != 0 ) {
                average[1] = (double) rpm / count;
            }
            if ( fuelFlow != 0 ) {
                average[2] = (double) fuelFlow / count;
            }
            if ( oilPressure != 0 ) {
                average[3] = oilPressure / count;
            }
            if ( fuelPressure != 0 ) {
                average[4] = fuelPressure / count;
            }
            if ( exhaust != 0 ) {
                average[5] = exhaust / count;
            }
        } else {
            return new double[]{ temp, (double) rpm, oilPressure, fuelPressure, exhaust, id };
        }
        return average;
    }


    @Override
    public ProducerRecord<String, String> getRecord( String topic ) {
        double[] average = getAverage();

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put( "averageTemp", average[0] );
        data.put( "averageRPM", average[1] );
        data.put( "averageFuelFlow", average[2] );
        data.put( "averageOilPressure", average[3] );
        data.put( "averageFuelPressure", average[4] );
        data.put( "averageExhaust", average[5] );

        return wrapRecord( "engine", topic, data );
    }


    @Override
    public void next( Sensor sensor ) {
        Engine entry = ((Engine) sensor);
        temp += entry.temp;
        rpm += entry.rpm;
        fuelFlow += entry.fuelFlow;
        oilPressure += entry.oilPressure;
        fuelPressure += entry.fuelPressure;
        exhaust += entry.exhaust;
    }

}
