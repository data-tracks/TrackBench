package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.SneakyThrows;

public class FuelPump extends Sensor {

    @JsonProperty("temperature fuelP")
    public double temp;
    @JsonProperty("ml/min")
    public double flowRate;


    @SneakyThrows
    public static FuelPump from( String json ) {
        return MAPPER.readValue( json, FuelPump.class );
    }

}
