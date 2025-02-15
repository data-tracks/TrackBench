package dev.trackbench.configuration.workloads;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.simulation.processing.Step;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.validation.Comparator;
import java.io.File;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.kafka.common.protocol.types.Field.Str;

@Getter
public abstract class Workload {

    private final BenchmarkConfig config;
    private final String name;


    public Workload( long id, String name,  BenchmarkConfig config ) {
        this.config = config;
        this.name = name;
    }


    public abstract Optional<Step> getProcessing(String fileName);

    public List<String> getValidation( JsonSource sortedTested, JsonSource sortedTruth ) {
        Comparator comparator = new Comparator(
                sortedTruth,
                sortedTested,
                value -> value.get( "id" ).asLong() );

        return comparator.compare();
    }


}
