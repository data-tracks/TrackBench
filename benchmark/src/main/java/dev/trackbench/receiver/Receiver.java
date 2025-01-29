package dev.trackbench.receiver;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.trackbench.system.System;
import dev.trackbench.util.Clock;
import dev.trackbench.util.FileJsonTarget;
import dev.trackbench.util.ObservableThread;
import dev.trackbench.workloads.Workload;

public class Receiver extends ObservableThread {


    private final System system;
    private final Clock clock;
    private final Buffer buffer;
    private final Workload workload;


    public Receiver( Workload workload, System system, Clock clock, FileJsonTarget target ) {
        this.system = system;
        this.workload = workload;
        this.clock = clock;
        this.buffer = new Buffer( ( tick, value ) -> {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.putIfAbsent( "data", value );
            node.put( "tick", tick );
            target.attach( node );
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
        super.interrupt();
    }
}
