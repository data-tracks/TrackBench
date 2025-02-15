package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Heat extends Sensor {

    public double temp;


    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        super.setData( data );
        if ( data.containsKey( "temperature c" ) ) {
            temp = Double.parseDouble( data.get( "temperature c" ) );
        }
    }


    @SneakyThrows
    public static Heat from( String json ) {
        return MAPPER.readValue( json, Heat.class );
    }


}
