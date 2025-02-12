package dev.trackbench.simulation.processing;

import java.util.List;
import java.util.function.Function;

public class Project extends Step {

    private final Function<Value, Value> project;


    public Project( Function<Value, Value> project ) {
        this.project = project;
    }


    @Override
    public void next( List<Value> values ) {
        values.forEach( value -> toAllSteps( List.of( project.apply( value ) ) ) );
    }

}
