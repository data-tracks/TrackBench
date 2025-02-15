package dev.trackbench.configuration.workloads;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.aggregate.AvgAggregator;
import dev.trackbench.simulation.processing.DistributionStep;
import dev.trackbench.simulation.processing.Filter;
import dev.trackbench.simulation.processing.Project;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.simulation.sensor.SensorTemplate;
import dev.trackbench.simulation.type.DoubleType;
import dev.trackbench.simulation.type.LongType;
import dev.trackbench.simulation.type.NumberType;
import dev.trackbench.simulation.window.SlidingWindow;
import dev.trackbench.util.ValueHandler;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;
import java.util.List;
import java.util.Optional;
import org.apache.commons.text.WordUtils;

public class WindowGroupWorkload extends Workload {

    private final SensorTemplate template;


    public WindowGroupWorkload( long id, SensorTemplate template, BenchmarkConfig config ) {
        super( id, "WindowGroup" + WordUtils.capitalize( template.getType() ), config );
        this.template = template;
    }


    @Override
    public Optional<Step> getProcessing( String fileName ) {
        Optional<ValueHandler> project = template.pickHeader( List.of( LongType.class, DoubleType.class, NumberType.class ) );
        if ( project.isEmpty() ) {
            return Optional.empty();
        }

        return Optional.of( new DistributionStep().after(
                new Filter( v -> {
                    JsonNode data = v.getNode();
                    return data.has( "data" ) && data.get( "data" ).has( "type" ) && data.get( "data" ).get( "type" ).asText().equals( template.getType() ) && !data.get( "data" ).has( "Error" );
                } ).after(
                        new Project( project.orElseThrow().extractor() ).after(
                                new SlidingWindow( AvgAggregator::new, 1_000 ).after(
                                        new Project( project.orElseThrow().setter() ).after(
                                                new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) ) ) ) ) ) );
    }

}
