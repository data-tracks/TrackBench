package dev.trackbench.simulation.sensor;


import dev.trackbench.simulation.error.Error;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SensorMeta {

    public long ticksGenerated = 0;

    private Map<String, Long> meta = new HashMap<>();


    @Override
    public String toString() {
        return "{%s}".formatted( meta.entrySet().stream().map( v -> v.getKey() + ":" + v.getValue() ).collect( Collectors.joining(", ")) );
    }


    public void addError( Error e ) {
        meta.put( e.name, e.getTotalCounter() );
    }

}
