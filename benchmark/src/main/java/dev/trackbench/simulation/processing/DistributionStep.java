package dev.trackbench.simulation.processing;

import java.util.List;

public class DistributionStep extends Step {



    @Override
    public void next( List<Value> values ) {
        toAllSteps( values );
    }

}
