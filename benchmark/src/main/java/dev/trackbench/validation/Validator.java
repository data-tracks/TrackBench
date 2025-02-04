package dev.trackbench.validation;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.validation.chunksort.ChunkSorter;
import dev.trackbench.validation.splitter.Splitter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map.Entry;

@Slf4j
public class Validator {

    public static void start( BenchmarkContext context ) {

        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            File simulated = context.getConfig().getSimulationPath( entry.getValue().getName() );
            File tested = context.getConfig().getResultFile( entry.getValue().getName() );

            SimpleChecker simpleChecker = new SimpleChecker( simulated, tested );
            simpleChecker.check();

            ChunkSorter sorter = new ChunkSorter(
                    JsonSource.of(tested, context.getConfig().readBatchSize()),
                    context.getConfig().getValidationFile("sortedTested"),
                    value -> value.get("id").asLong());
            File sortedTested = sorter.sort();

            sorter = new ChunkSorter(
                    JsonSource.of(simulated, context.getConfig().readBatchSize()),
                    context.getConfig().getValidationFile("sortedTruth"),
                    value -> value.get("id").asLong());
            File sortedTruth = sorter.sort();

            Comparator comparator = new Comparator(
                    JsonSource.of( sortedTruth, context.getConfig().readBatchSize() ),
                    JsonSource.of( sortedTested, context.getConfig().readBatchSize() ),
                    value -> value.get("id").asLong() );

            comparator.compare();

        }

    }

}
