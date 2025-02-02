package dev.trackbench.validation;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.validation.chunksort.ChunkSorter;

import java.io.File;
import java.util.Map.Entry;

public class Validator {

    public static void start( BenchmarkContext context ) {

        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            File simulated = context.getConfig().getSimulationPath( entry.getValue().getName() );
            File tested = context.getConfig().getResultFile( entry.getValue().getName() );

            SimpleChecker simpleChecker = new SimpleChecker( simulated, tested );
            simpleChecker.check();

            ChunkSorter sorter = new ChunkSorter(JsonSource.of(simulated, context.getConfig().readBatchSize()), context.getConfig().getValidationFile("sorted"));
            sorter.chunk();

            /*Comparator comparator = new Comparator(
                    JsonSource.of( simulated, context.getConfig().readBatchSize() ),
                    j -> j.get( "tick" ).asLong(),
                    JsonSource.of( tested, context.getConfig().readBatchSize() ),
                    j -> j.get( "data" ).get( "tick" ).asLong(),
                    (a,b) -> "Nothing");

            comparator.compare();*/

        }

    }

}
