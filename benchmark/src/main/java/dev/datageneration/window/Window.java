package dev.datageneration.window;

import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import java.util.List;

public abstract class Window extends Step {


    public void next( List<Value> values ){
        values.forEach( this::next );
    }


}
