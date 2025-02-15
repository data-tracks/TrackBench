package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Engine extends Sensor {


    public int temp;
    public double rpm;
    public int fuelFlow;
    public double oilPressure;
    public double fuelPressure;
    public double exhaust;

    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        super.setData( data );
        if ( data.containsKey( "temperature engine" ) ) {
            temp = Integer.parseInt( data.get( "temperature engine" ) );
        }
        if ( data.containsKey( "rpm" ) ) {
            rpm = Double.parseDouble( data.get( "rpm" ) );
        }
        if ( data.containsKey( "fuelFlow" ) ) {
            fuelFlow = Integer.parseInt( data.get( "fuelFlow" ) );
        }
        if ( data.containsKey( "oil_pressure" ) ) {
            oilPressure = Double.parseDouble( data.get( "oil_pressure" ) );
        }

        if ( data.containsKey( "fuelPressure" ) ) {
            fuelPressure = Double.parseDouble( data.get( "fuelPressure" ) );
        }
        if ( data.containsKey( "exhaust" ) ) {
            exhaust = Double.parseDouble( data.get( "exhaust" ) );
        }
    }


    @SneakyThrows
    public static Engine from( String json ) {
        return MAPPER.readValue( json, Engine.class );
    }

}
