package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Brake extends Sensor {


    public int temp;

    public int pressure;

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
    public static Brake from( String json ) {
        return MAPPER.readValue( json, Brake.class );
    }

}
