package dev.trackbench.simulation.processing;

import java.util.List;
import java.util.function.Function;

public class Filter extends Step {

    Function<Value, Boolean> filter;


    public Filter(Function<Value, Boolean> filter ) {
        this.filter = filter;
    }


    @Override
    public void next( List<Value> value ) {
        List<Value> values = value.stream().filter( v -> filter.apply( v ) ).toList();
        toAllSteps( values );
    }

}
