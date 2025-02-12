package dev.trackbench.simulation.processing;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.util.file.JsonIterator;
import dev.trackbench.util.SimpleCountRegistry;
import dev.trackbench.configuration.workloads.Workload;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessingHandler extends Thread {

    private final BenchmarkContext context;

    private final BenchmarkConfig config;

    private final List<File> sources;

    private final SimpleCountRegistry registry;
    private final Workload workload;
    private final String targetName;


    public ProcessingHandler(
            BenchmarkContext context,
            File source,
            String targetName,
            Workload workload,
            SimpleCountRegistry registry) {
        this.config = context.getConfig();
        this.context = context;
        this.sources = source.isFile() ? List.of(source) : Arrays.asList(Objects.requireNonNull(source.listFiles()));
        this.registry = registry;
        this.workload = workload;
        this.targetName = targetName;
    }


    @Override
    public void run() {
        List<JsonIterator> iterators = sources.stream().map(s -> new JsonIterator(config.readBatchSize(), s, false)).toList();

        Optional<Step> initialStep = workload.getProcessing(targetName);

         if ( initialStep.isEmpty() ) {
             return;
         }

        for (JsonIterator iterator : iterators) {
            while (iterator.hasNext()) {
                JsonNode node = iterator.next();
                JsonNode tick = Objects.requireNonNull(node).get("tick");
                initialStep.orElseThrow().next(new Value(tick.asLong(), node));
            }
        }

        initialStep.orElseThrow().close();
        this.registry.finish();
    }

}
