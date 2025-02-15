package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tire extends Sensor {

    public double temp;
    public double pressure;
    public int wear;


    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        super.setData( data );
        if ( data.containsKey( "temp" ) ) {
            temp = Integer.parseInt( data.get( "temp" ) );
        }
        if ( data.containsKey( "pressure" ) ) {
            pressure = Integer.parseInt( data.get( "pressure" ) );
        }
        if ( data.containsKey( "wear" ) ) {
            wear = Integer.parseInt( data.get( "wear" ) );
        }
    }


    @SneakyThrows
    public static Tire from( String json ) {
        return MAPPER.readValue( json, Tire.class );
    }

}
