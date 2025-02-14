package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignores any fields not mapped
public class Accelerometer extends Sensor {

    @JsonProperty("data.throttlepedall")
    public double throttle;



    @SneakyThrows
    public static Accelerometer from( String json ) {
        return MAPPER.readValue( json, Accelerometer.class );
    }

}
