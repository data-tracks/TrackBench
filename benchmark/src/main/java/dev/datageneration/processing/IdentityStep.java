package dev.datageneration.processing;

import java.util.List;

public class IdentityStep extends Step {

    public IdentityStep( List<Step> steps ) {
        super( steps );
    }


    @Override
    public void next( List<Value> values ) {
        toAllSteps( values );
    }

}
