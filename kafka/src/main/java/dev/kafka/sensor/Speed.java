package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.SneakyThrows;

public class Speed extends Sensor {

    @JsonProperty("speed")
    public double speed;

    @JsonProperty("wind")
    public double wind;

    @SneakyThrows
    public static Speed from( String json ) {
        return MAPPER.readValue( json, Speed.class );
    }

}
