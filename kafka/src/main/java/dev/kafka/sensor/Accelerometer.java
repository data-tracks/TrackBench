package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Accelerometer extends Sensor {

    public double throttle;


    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        super.setData( data );
        if ( data.containsKey( "throttlepedall" ) ) {
            throttle = Double.parseDouble( data.get( "throttlepedall" ) );
        }
    }


    @SneakyThrows
    public static Accelerometer from( String json ) {
        return MAPPER.readValue( json, Accelerometer.class );
    }

}
