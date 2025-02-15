package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.aggregate.AvgAggregator;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.simulation.window.SlidingWindow;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;
import java.util.Optional;

public class WindowWorkload extends Workload {

    public WindowWorkload( long id, BenchmarkConfig config ) {
        super( id, "Window", config );
    }


    @Override
    public Optional<Step> getProcessing( String fileName ) {
        return Optional.ofNullable(
                new DistributionStep().after(
                        new SlidingWindow( AvgAggregator::new, 1_000 ).after(
                                new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) ) ) );
    }

}
