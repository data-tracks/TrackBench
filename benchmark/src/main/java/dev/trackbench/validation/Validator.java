package dev.trackbench.validation;

import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.display.Display;
import dev.trackbench.display.DisplayUtils;
import dev.trackbench.util.file.JsonSource;
import dev.trackbench.validation.chunksort.ChunkSorter;
import java.io.File;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Validator {

    public static void start( BenchmarkContext context ) {

        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            Display.INSTANCE.line();
            Display.INSTANCE.info( "Analysis Workload: " + entry.getValue().getName() );
            Display.INSTANCE.line();
            JsonSource simulated = JsonSource.of( context.getConfig().getSimulationPath( entry.getValue().getName() ), context.getConfig().readBatchSize() );
            JsonSource tested = JsonSource.of( context.getConfig().getResultFile( entry.getValue().getName() ), context.getConfig().readBatchSize() );

            SimpleChecker simpleChecker = new SimpleChecker( simulated, tested );
            simpleChecker.check();

            ChunkSorter sorter = new ChunkSorter(
                    tested.copy(),
                    context.getConfig().getValidationFile( "sortedTested" ),
                    value -> value.get( "id" ).asLong() );
            File sortedTested = sorter.sort();

            sorter = new ChunkSorter(
                    simulated.copy(),
                    context.getConfig().getValidationFile( "sortedTruth" ),
                    value -> value.get( "id" ).asLong() );
            File sortedTruth = sorter.sort();

            Comparator comparator = new Comparator(
                    JsonSource.of( sortedTruth, context.getConfig().readBatchSize() ),
                    JsonSource.of( sortedTested, context.getConfig().readBatchSize() ),
                    value -> value.get( "id" ).asLong() );

            comparator.compare();

        }

    }

}
