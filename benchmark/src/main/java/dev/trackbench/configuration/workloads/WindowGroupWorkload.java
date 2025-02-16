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
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.file.FileStep;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class WindowGroupWorkload extends Workload {

    private final List<SensorTemplate> templates;


    public WindowGroupWorkload( long id, List<SensorTemplate> templates, BenchmarkConfig config ) {
        super( id, "WindowGroup", config );
        this.templates = templates;
    }


    @Override
    public Optional<Step> getProcessing( String fileName ) {
        List<Step> projects = templates.stream().map( t -> Pair.of( t, t.pickHeader( List.of( LongType.class, DoubleType.class, NumberType.class ) ) ) ).filter( p -> p.getValue().isPresent() ).map( p ->
                new Filter( v -> {
                    JsonNode data = v.getNode();
                    return data.has( "data" ) && data.get( "data" ).has( "type" ) && data.get( "data" ).get( "type" ).asText().equals( p.getKey().getType() ) && !data.get( "data" ).has( "Error" );
                } ).after( new Project( p.getValue().orElseThrow().extractor() ).after(
                        new SlidingWindow( AvgAggregator::new, 1_000 ).after(
                                new Project( p.getValue().orElseThrow().setter() )
                                        .after( new FileStep( new FileJsonTarget( getConfig().getSimulationFile( this.getName(), fileName ), getConfig() ) ) ) ) ) ) ).toList();

        Step process = new DistributionStep();

        projects.forEach( process::after );
        return Optional.of( process );
    }

}
