package dev.trackbench.simulation.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.simulation.sensor.Sensor;
import dev.trackbench.util.JsonTarget;
import dev.trackbench.util.Pair;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class Error {

    public final String name;
    private final long interval;
    private final long alteration;
    private final JsonTarget errorTarget;
    private final Sensor sensor;

    @Getter
    private long totalCounter = 0;

    private long dynamicInterval;
    private long counter;

    private final Random random;
    public final JsonTarget target;


    public Error( String name, Sensor sensor, long interval, float alteration, Random random, JsonTarget target, JsonTarget errorTarget ) {
        this.name = name;
        this.interval = interval;
        this.alteration = (long) (alteration * interval);
        this.random = random;
        this.target = target;
        this.errorTarget = errorTarget;
        this.sensor = sensor;
    }


    public boolean injectError( long tick, List<JsonNode> previous, ObjectNode data ) {
        if ( isError() ) {
            totalCounter++;

            attachError( tick, previous, data );

            counter = 0;
            this.dynamicInterval = calcNextError();
            return true;
        }
        // no error
        counter++;
        return false;
    }


    protected void attachError( long tick, List<JsonNode> previous, ObjectNode data ) {

        Pair<List<JsonNode>, List<JsonNode>> errorsValues = produceError( tick, previous, data );

        errorTarget.attach( errorsValues.left() );

        target.attach( errorsValues.right() );
    }


    public abstract Pair<List<JsonNode>, List<JsonNode>> produceError( long tick, List<JsonNode> previous, ObjectNode data );


    private boolean isError() {
        return counter >= dynamicInterval;
    }


    private long calcNextError() {
        // max shift of error in more or less interval between two errors
        return interval + (int) (random.nextFloat( -alteration, alteration ));
    }

}
