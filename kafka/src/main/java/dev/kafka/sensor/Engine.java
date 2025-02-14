package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.SneakyThrows;

public class Engine extends Sensor {

    @JsonProperty("data.temperature engine")
    public int temp;
    @JsonProperty("data.rpm")
    public long rpm;
    @JsonProperty("data.fuelFlow")
    public int fuelFlow;
    @JsonProperty("data.oil_pressure")
    public double oilPressure;
    @JsonProperty("data.fuel_pressure")
    public double fuelPressure;
    @JsonProperty("data.exhaust")
    public double exhaust;


    @SneakyThrows
    public static Engine from( String json ) {
        return MAPPER.readValue( json, Engine.class );
    }

}
