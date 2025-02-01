package dev.trackbench.simulation.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.util.file.JsonTarget;
import dev.trackbench.util.Pair;
import java.util.List;
import java.util.Random;

public class DropError extends Error{

    public DropError( Sensor sensor, long interval, float alteration, Random random, JsonTarget target, JsonTarget errorTarget ) {
        super( "Drop Error", sensor, interval, alteration, random, target, errorTarget );
    }


    @Override
    public Pair<List<JsonNode>, List<JsonNode>> produceError( long tick, List<JsonNode> previous, ObjectNode data ) {
        return new Pair<>( List.of(), List.of() );
    }

}
