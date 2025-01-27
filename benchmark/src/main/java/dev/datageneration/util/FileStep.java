package dev.datageneration.util;

import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import java.io.IOException;
import java.util.List;

public class FileStep extends Step {

    private final JsonTarget target;


    public FileStep( List<Step> steps, JsonTarget target ) {
        super( steps );
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
            target.attach( value.node() );
        }
        toAllSteps( values );
    }

}
