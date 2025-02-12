package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Filter;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;
import java.util.Optional;

public class ErrorWorkload extends Workload {

    public ErrorWorkload( BenchmarkConfig config ) {
        super( "Error", config );
    }


    @Override
    public Optional<Step> getProcessing(String filename) {
        String path = "/data/Error";
        return Optional.ofNullable( new DistributionStep().after(
                new Filter( v -> !v.getNode().at( path ).isMissingNode() ).after(
                        new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), filename ), getConfig() ) )
                )
        ) );
    }

}
