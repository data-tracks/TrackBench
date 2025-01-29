package dev.trackbench.validation;

import dev.trackbench.BenchmarkContext;
import dev.trackbench.workloads.Workload;
import java.util.Map.Entry;

public class Validator {

    public static void start( BenchmarkContext context ) {
        for ( Entry<Integer, Workload> entry : context.getWorkloads().entrySet() ) {
            //Sorter sorter = new Sorter( context.getConfig().getResultFile( entry.getValue().getName() ), context.getConfig().readBatchSize(), v -> 0L );
            //sorter.sort();

            Checker checker = new Checker( context.getConfig().getSimulationPath( entry.getValue().getName() ), context.getConfig().getResultFile( entry.getValue().getName() ) );
            checker.check();
        }

    }

}
