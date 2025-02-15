package dev.kafka.sensor;

import static dev.kafka.util.Connection.MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FuelPump extends Sensor {

    @JsonProperty("temperature fuelP")
    public double temp;
    @JsonProperty("ml/min")
    public double flowRate;


    @JsonProperty("data")
    void setData( Map<String, String> data ) {
        super.setData( data );
        if ( data.containsKey( "temperature fuelP" ) ) {
            temp = Integer.parseInt( data.get( "temperature fuelP" ) );
        }
        if ( data.containsKey( "ml/min" ) ) {
            flowRate = Double.parseDouble( data.get( "ml/min" ) );
        }

    }


    @SneakyThrows
    public static FuelPump from( String json ) {
        return MAPPER.readValue( json, FuelPump.class );
    }

}
