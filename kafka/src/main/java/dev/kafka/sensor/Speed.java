package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Speed extends Sensor {

    @JsonProperty("speed")
    public double speed;

    @JsonProperty("wind")
    public double wind;


    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        super.setData( data );
        if ( data.containsKey( "speed" ) ) {
            speed = Double.parseDouble( data.get( "speed" ) );
        }
        if ( data.containsKey( "wind" ) ) {
            wind = Double.parseDouble( data.get( "wind" ) );
        }
    }


    @SneakyThrows
    public static Speed from( String json ) {
        return MAPPER.readValue( json, Speed.class );
    }

}
