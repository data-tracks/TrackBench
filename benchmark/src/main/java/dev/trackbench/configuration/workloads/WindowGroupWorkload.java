package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.aggregate.AvgAggregator;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Filter;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.simulation.sensor.SensorTemplate;
import dev.trackbench.simulation.type.DoubleType;
import dev.trackbench.simulation.type.LongType;
import dev.trackbench.simulation.window.SlidingWindow;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;
import java.util.List;
import java.util.Optional;
import org.apache.commons.text.WordUtils;

public class WindowGroupWorkload extends Workload {

    private final SensorTemplate template;


    public WindowGroupWorkload( SensorTemplate template, BenchmarkConfig config ) {
        super( "WindowGroup" + WordUtils.capitalize( template.getType() ), config );
        this.template = template;
    }


    @Override
    public Optional<Step> getProcessing( String fileName ) {
        Optional<Step> project = template.pickHeader( List.of( LongType.class, DoubleType.class ) );
        if ( project.isEmpty() ) {
            return Optional.empty();
        }

        return Optional.of( new DistributionStep()
                .after( new Filter( v -> v.getNode().has( "data" ) && v.getNode().get( "data" ).has( "type" ) && v.getNode().get( "data" ).get( "type" ).asText().equals( template.getType() ) ) )
                .after( project.orElseThrow() )
                .after( new SlidingWindow( AvgAggregator::new, 1_000 ) )
                .after( new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) ) );
    }

}
