package dev.datageneration.processing;

import java.util.ArrayList;
import java.util.List;

public abstract class Step {

    public final List<Step> steps = new ArrayList<>();


    public Step after(Step step) {
        steps.add(step);
        return this;
    }

    public void close() {
        this.steps.forEach( Step::close );
    }

    public abstract void next( List<Value> values );


    public void next( Value value ) {
        next( List.of( value ) );
    }


    protected void toAllSteps( List<Value> values ) {
        this.steps.forEach( step -> step.next( values ) );
    }

}
