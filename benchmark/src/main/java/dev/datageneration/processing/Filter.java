package dev.datageneration.processing;

import java.util.List;
import java.util.function.Function;

public class Filter extends Step {

    Function<Value, Boolean> filter;


    public Filter( List<Step> steps, Function<Value, Boolean> filter ) {
        super( steps );
        this.filter = filter;
    }


    @Override
    public void next( List<Value> value ) {
        List<Value> values = value.stream().filter( v -> filter.apply( v ) ).toList();
        toAllSteps( values );
    }

}
