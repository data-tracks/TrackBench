package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.SneakyThrows;

public class Brake extends Sensor {

    @JsonProperty("temp")
    public int temp;
    @JsonProperty("pressure")
    public int pressure;
    @JsonProperty("wear")
    public int wear;


    @SneakyThrows
    public static Brake from( String json ) {
        return MAPPER.readValue( json, Brake.class );
    }

}
