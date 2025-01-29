package dev.trackbench.workloads;

import dev.trackbench.BenchmarkConfig;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Filter;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.util.FileJsonTarget;
import dev.trackbench.util.FileStep;

public class ErrorWorkload extends Workload {

    public ErrorWorkload( BenchmarkConfig config ) {
        super( "Error", config );
    }


    @Override
    public Step getProcessing(String filename) {
        String path = "/data/Error";
        return new DistributionStep().after(
                new Filter( v -> !v.getNode().at( path ).isMissingNode() ).after(
                        new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), filename ), getConfig() ) )
                )
        );
    }

}
