package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.SneakyThrows;

public class Heat extends Sensor {

    @JsonProperty("temp")
    public double temp;


    @SneakyThrows
    public static Heat from( String json ) {
        return MAPPER.readValue( json, Heat.class );
    }

}
