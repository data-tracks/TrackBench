package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import lombok.SneakyThrows;

public class Tire extends Sensor {

    public double temp;
    public double pressure;
    public int wear;
    public int position;


    @SneakyThrows
    public static Tire from( String json ) {
        return MAPPER.readValue( json, Tire.class );
    }

}
