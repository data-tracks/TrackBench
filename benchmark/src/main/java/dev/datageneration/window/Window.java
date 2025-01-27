package dev.datageneration.window;

import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import java.util.List;

public abstract class Window extends Step {

    public Window( List<Step> steps ) {
        super( steps );
    }


    public void next( List<Value> values ){
        values.forEach( this::next );
    }


}
