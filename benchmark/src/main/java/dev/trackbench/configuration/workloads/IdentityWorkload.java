package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Filter;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;
import java.util.Optional;

public class IdentityWorkload extends Workload {

    public IdentityWorkload( long id, BenchmarkConfig config ) {
        super( id, "Identity", config );
    }


    @Override
    public Optional<Step> getProcessing( String fileName ) {
        return Optional.ofNullable( new DistributionStep().after( new Filter( v -> v.getNode().has( "data" ) && !v.getNode().get( "data" ).has( "Error" ) ).after( new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) ) ) );
    }

}
