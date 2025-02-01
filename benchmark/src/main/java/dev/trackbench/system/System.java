package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.execution.receiver.Buffer;
import dev.trackbench.util.Clock;
import dev.trackbench.configuration.workloads.Workload;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface System {

    void prepare();

    Consumer<JsonNode> getSender();

    Runnable getReceiver( Workload workload, AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer );

}
