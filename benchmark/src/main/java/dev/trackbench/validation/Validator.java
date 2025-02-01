package dev.trackbench.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.util.file.JsonSource;
import java.io.File;
import java.util.Map.Entry;

public class Validator {

    public static void start( BenchmarkContext context ) {

        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            //Sorter sorter = new Sorter( context.getConfig().getResultFile( entry.getValue().getName() ), context.getConfig().readBatchSize(), v -> 0L );
            //sorter.sort();
            File simulated = context.getConfig().getSimulationPath( entry.getValue().getName() );
            File tested = context.getConfig().getResultFile( entry.getValue().getName() );

            Checker checker = new Checker( simulated, tested );
            checker.check();

            Comparator comparator = new Comparator(
                    JsonSource.of( simulated, context.getConfig().readBatchSize() ),
                    j -> j.get( "tick" ).asLong(),
                    JsonSource.of( tested, context.getConfig().readBatchSize() ),
                    j -> j.get( "data" ).get( "tick" ).asLong(),
                    (a,b) -> "Nothing");

            comparator.compare();

        }

    }

}
