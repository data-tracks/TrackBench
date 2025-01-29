package dev.trackbench.util;

import dev.trackbench.simulation.processing.Step;
import dev.trackbench.simulation.processing.Value;
import java.io.IOException;
import java.util.List;

public class FileStep extends Step {

    private final JsonTarget target;


    public FileStep( JsonTarget target ) {
        this.target = target;
    }


    @Override
    public void close() {
        try {
            target.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void next( List<Value> values ) {
        for ( Value value : values ) {
            target.attach( value.getNode() );
        }
        toAllSteps( values );
    }

}
