package dev.trackbench.workloads;

import dev.trackbench.BenchmarkConfig;
import dev.trackbench.simulation.processing.Step;
import lombok.Getter;

@Getter
public abstract class Workload {

    private final BenchmarkConfig config;
    private final String name;


    public Workload( String name,  BenchmarkConfig config ) {
        this.config = config;
        this.name = name;
    }


    public abstract Step getProcessing(String fileName);


}
