package dev.trackbench.execution.receiver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.display.Display;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;
import dev.trackbench.util.file.FileJsonTarget;
import dev.trackbench.util.ObservableThread;
import dev.trackbench.configuration.workloads.Workload;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Receiver extends ObservableThread {


    private final System system;
    private final Clock clock;
    private final Buffer buffer;
    private final Workload workload;
    private final FileJsonTarget target;


    public Receiver( Workload workload, System system, Clock clock, FileJsonTarget target ) {
        this.system = system;
        this.workload = workload;
        this.clock = clock;
        this.target = target;
        this.buffer = new Buffer( ( tick, value ) -> {
            ((ObjectNode) value).put(BenchmarkConfig.ARRIVED_TICK_KEY, tick);
            target.attach( value );
        } );
    }


    @Override
    public void run() {
        this.buffer.start();
        system.getReceiver( workload, running, ready, clock, buffer ).run();
    }


    @Override
    public void interrupt() {
        this.buffer.interrupt();
        if (!this.buffer.buffer.isEmpty()) {
            Display.INSTANCE.info("Buffer size is {}", this.buffer.buffer.size());
        }
        try {
            target.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.interrupt();
    }
}
