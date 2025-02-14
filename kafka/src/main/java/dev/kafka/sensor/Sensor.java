package dev.kafka.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sensor {

    @JsonProperty("id")
    public long id;
    @JsonProperty("tick")
    public long tick;
    @JsonProperty("data.type")
    public String type;

    @JsonProperty("data.Error")
    public boolean error;


}
