package dev.trackbench.simulation.window;

import dev.trackbench.simulation.processing.Step;
import dev.trackbench.simulation.processing.Value;
import java.util.List;

public abstract class Window extends Step {


    public void next( List<Value> values ){
        values.forEach( this::next );
    }


}
