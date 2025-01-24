package dev.datageneration.simulation.Sensors;

import static dev.datageneration.simulation.Sensors.Sensor.dataTypes;

import dev.datageneration.simulation.types.DataType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class SensorTemplate {

    static long idCounter = 0;

    long id = idCounter++;
    String type;
    List<String> headers;

    public SensorTemplate( String type, List<String> headers) {
        this.type = type;
        this.headers = headers;
    }

    public SensorTemplate(String type, String... headers) {
        this(type, List.of(headers));
    }


    public static @NotNull SensorTemplate of( String type, List<String> headers ) {
        return new SensorTemplate(type, headers);
    }

    public static @NotNull SensorTemplate of( String type, String... headers ) {
        return new SensorTemplate( type, headers);
    }

    public Map<String, DataType> getDataTypes() {
        return headers.stream().collect( Collectors.toMap( header -> header, header -> dataTypes.get( header ) ) );
    }

}
