package dev.trackbench.simulation.sensor;

import static dev.trackbench.simulation.sensor.Sensor.dataTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.trackbench.simulation.error.ErrorRates;
import dev.trackbench.simulation.type.DataType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class SensorTemplate {

    public static List<Supplier<SensorTemplate>> templates = List.of(
            () -> SensorTemplate.of( "heat", "0", ErrorRates.of( 0.001, 0.001 ), "temperature c" ),//heat sensor
            () -> SensorTemplate.of( "heat", "0", ErrorRates.of( 0.001, 0.001 ), "temperature c" ),//heat sensor,
            () -> SensorTemplate.of( "tire", "0", ErrorRates.of( 0.001, 0.001 ), "temperature tire", "pressure psi", "wear", "liability", "position" ),//front_left_tyre
            () -> SensorTemplate.of( "tire", "0", ErrorRates.of( 0.001, 0.001 ), "temperature tire", "pressure psi", "wear", "liability", "position" ),//front_right_tyre
            () -> SensorTemplate.of( "tire", "0", ErrorRates.of( 0.001, 0.001 ), "temperature tire", "pressure psi", "wear", "liability", "position" ),//rear_left_tyre
            () -> SensorTemplate.of( "tire", "0", ErrorRates.of( 0.001, 0.001 ), "temperature tire", "pressure psi", "wear", "liability", "position" ),//rear_right_tyre
            () -> SensorTemplate.of( "speed", "0", ErrorRates.of( 0.001, 0.001 ), "kph", "mph", "acceleration", "wind speed" ),//speed_sensor
            () -> SensorTemplate.of( "gForce", "0", ErrorRates.of( 0.001, 0.001 ), "g-lateral", "g-longitudinal" ),//g_sensor
            () -> SensorTemplate.of( "fuelPump", "0", ErrorRates.of( 0.001, 0.001 ), "temperature fuelP", "ml/min" ),//fuel_pump_sensor
            () -> SensorTemplate.of( "DRS", "0", ErrorRates.of( 0.001, 0.001 ), "on/off", "drs-zone" ),//drs_sensor
            () -> SensorTemplate.of( "brake", "0", ErrorRates.of( 0.001, 0.001 ), "temperature brake", "brake_pressure", "wear" ),//front_left_brake
            () -> SensorTemplate.of( "brake", "0", ErrorRates.of( 0.001, 0.001 ), "temperature brake", "brake_pressure", "wear" ),//front_right_brake
            () -> SensorTemplate.of( "brake", "0", ErrorRates.of( 0.001, 0.001 ), "temperature brake", "brake_pressure", "wear" ),//rear_left_brake
            () -> SensorTemplate.of( "brake", "0", ErrorRates.of( 0.001, 0.001 ), "temperature brake", "brake_pressure", "wear" ),//rear_right_brake
            () -> SensorTemplate.of( "accelerometer", "0", ErrorRates.of( 0.001, 0.001 ), "throttlepedall" ),
            () -> SensorTemplate.of( "engine", "0", ErrorRates.of( 0.001, 0.001 ), "temperature engine", "rpm", "fuelFlow", "oil_pressure", "fuel_pressure", "exhaust" ),
            () -> SensorTemplate.of( "blackbox", "0", ErrorRates.of( 0.001, 0.001 ), "array_of_data" ),
            () -> SensorTemplate.of( "steering", "0", ErrorRates.of( 0.001, 0.001 ), "direction", "turning_degree" ) );

    public static Map<String, Integer> tickLength = new HashMap<>() {{
        put( "tire", 1 );                //tick --> 10 entries per timeunit
        put( "heat", 1 );
        put( "speed", 1 );
        put( "gForce", 1 );
        put( "fuelPump", 1 );
        put( "DRS", 1 );
        put( "brake", 1 );
        put( "steering", 1 );
        put( "accelerometer", 1 );
        put( "engine", 1 );
        put( "blackbox", 5 );           // every 5th tick an entry
    }};

    static long idCounter = 0;

    long id = idCounter++;
    String type;
    String group;
    List<String> headers;
    @Getter
    Map<String, DataType> headerTypes;

    ErrorRates errorRates;


    public SensorTemplate( String type, String group, ErrorRates errorRates, List<String> headers ) {
        this.type = type;
        this.group = group;
        this.headers = headers;
        this.errorRates = errorRates;
        this.headerTypes = headers.stream().collect( Collectors.toMap( header -> header, header -> dataTypes.get( header ).get() ) );
    }


    public SensorTemplate( String type, ErrorRates errorRates, String group, String... headers ) {
        this( type, group, errorRates, List.of( headers ) );
    }


    public static @NotNull SensorTemplate of( String type, String group, ErrorRates errorRates, List<String> headers ) {
        return new SensorTemplate( type, group, errorRates, headers );
    }


    public static @NotNull SensorTemplate of( String type, String group, ErrorRates errorRates, String... headers ) {
        return new SensorTemplate( type, errorRates, group, headers );
    }


    public int getTickLength() {
        return tickLength.get( getType() );
    }


    public JsonNode toJson() {
        ObjectNode obj = JsonNodeFactory.instance.objectNode();
        obj.put( "id", id );
        obj.put( "type", type );
        obj.putIfAbsent( "headers", JsonNodeFactory.instance.arrayNode().addAll( headers.stream().map( TextNode::valueOf ).toList() ) );
        obj.putIfAbsent( "errorRates", errorRates.toJson() );
        return obj;
    }


    public static SensorTemplate fromJson( JsonNode template ) {
        String type = template.path( "type" ).asText();
        long id = template.path( "id" ).asLong();
        String group = template.path( "group" ).asText();
        List<String> headers = parseHeaders( template.get( "headers" ) );
        ErrorRates errorRates = ErrorRates.fromJson( (ObjectNode) template.get( "errorRates" ) );

        return new SensorTemplate( type, group, errorRates, headers );
    }


    private static List<String> parseHeaders( JsonNode template ) {
        List<String> hs = new ArrayList<>();

        for ( JsonNode node : template ) {
            hs.add( node.asText() );
        }

        return hs;
    }


}
