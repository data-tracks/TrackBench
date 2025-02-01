package dev.trackbench.simulation.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.util.file.JsonTarget;
import dev.trackbench.util.Pair;
import java.util.List;
import java.util.Random;

public class NoDataError extends Error {

    public NoDataError( Sensor sensor, long interval, float alteration, Random random, JsonTarget target, JsonTarget errorTarget ) {
        super( "No Data Error", sensor, interval, alteration, random, target, errorTarget );
    }


    @Override
    public Pair<List<JsonNode>, List<JsonNode>> produceError( long tick, List<JsonNode> previous, ObjectNode data ) {
        ObjectNode errorData = JsonNodeFactory.instance.objectNode();
        errorData.put( "type", getSensor().getTemplate().getType() );
        errorData.put( "id", getSensor().id );
        errorData.put( "Error", "No Data" );

        ObjectNode error = JsonNodeFactory.instance.objectNode();
        error.put( "tick", tick );
        error.putIfAbsent( "data", errorData );

        return new Pair<>( List.of( error ), List.of(error) );
    }

}
