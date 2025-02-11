package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.aggregate.AvgAggregator;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Filter;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.simulation.window.SlidingWindow;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;

public class WindowGroupWorkload extends Workload {

    private final String filter;


    public WindowGroupWorkload( String filter, BenchmarkConfig config ) {
        super( "WindowGroup" + filter, config );
        this.filter = filter;
    }


    @Override
    public Step getProcessing( String fileName ) {
        return new DistributionStep()
                .after( new Filter( v -> v.getNode().has( "data" ) && v.getNode().get( "data" ).has( "type" ) && v.getNode().get( "data" ).get( "type" ).asText().equals( filter ) ) )
                .after( new SlidingWindow( AvgAggregator::new, 1_000 ) )
                .after( new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) );
    }

}
