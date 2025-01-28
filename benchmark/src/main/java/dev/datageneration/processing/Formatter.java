package dev.datageneration.processing;

import java.util.List;
import java.util.function.Function;

public class Formatter extends Step {

    Function<Value, List<Value>> transform;

    public Formatter(Function<Value, List<Value>> transform) {
        this.transform = transform;
    }

    @Override
    public void next( List<Value> values ) {
        toAllSteps( values.stream().flatMap( v -> transform.apply( v ).stream() ).toList() );
    }

}
