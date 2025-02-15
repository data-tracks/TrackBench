package dev.kafka.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Sensor {

    @JsonProperty("id")
    public long id;
    @JsonProperty("tick")
    public long tick;

    public String type;

    public boolean error;

    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        if ( data.containsKey( "type" ) ) {
            this.type = data.get( "type" );
        }
        if ( data.containsKey( "error" ) || data.containsKey( "Error" ) ) {
            this.error = true;
        }
    }


}
