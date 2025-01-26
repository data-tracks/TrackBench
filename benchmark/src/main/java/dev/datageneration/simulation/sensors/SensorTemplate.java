package dev.datageneration.simulation.sensors;

import static dev.datageneration.simulation.sensors.Sensor.dataTypes;

import dev.datageneration.simulation.ErrorRates;
import dev.datageneration.simulation.types.DataType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class SensorTemplate {

    public static Map<String, Integer> tickLength = new HashMap<>(){{
        put( "tire", 1 );                //tick --> 10 entries per timeunit
        put( "heat", 1);
        put( "speed", 1);
        put( "gForce", 1);
        put( "fuelPump", 1);
        put( "DRS", 1);
        put( "brake", 1);
        put( "steering", 1);
        put( "accelerometer", 1);
        put( "engine", 1);
        put( "blackbox", 5);           // every 5th tick an entry
    }};

    static long idCounter = 0;

    long id = idCounter++;
    String type;
    List<String> headers;

    ErrorRates errorRates;

    public SensorTemplate( String type, ErrorRates errorRates, List<String> headers ) {
        this.type = type;
        this.headers = headers;
        this.errorRates = errorRates;
    }

    public SensorTemplate(String type, ErrorRates errorRates, String... headers) {
        this(type, errorRates, List.of(headers));
    }


    public static @NotNull SensorTemplate of( String type, ErrorRates errorRates, List<String> headers ) {
        return new SensorTemplate(type, errorRates, headers);
    }

    public static @NotNull SensorTemplate of( String type, ErrorRates errorRates, String... headers ) {
        return new SensorTemplate( type, errorRates, headers);
    }

    public Map<String, DataType> getDataTypes() {
        return headers.stream().collect( Collectors.toMap( header -> header, header -> dataTypes.get( header ) ) );
    }

    public int getTickLength() {
        return tickLength.get( getType() );
    }

}
