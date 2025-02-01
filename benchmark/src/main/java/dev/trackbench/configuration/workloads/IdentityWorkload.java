package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;

public class IdentityWorkload extends Workload {

    public IdentityWorkload( BenchmarkConfig config ) {
        super( "Identity", config );
    }


    @Override
    public Step getProcessing(String fileName) {
        return new DistributionStep().after( new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) );
    }

}
