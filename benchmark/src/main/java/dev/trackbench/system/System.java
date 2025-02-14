package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.configuration.workloads.Workload;
import dev.trackbench.execution.receiver.Buffer;
import dev.trackbench.util.Clock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface System {

    static System from( String system ) {
        return switch ( system.toLowerCase() ) {
            case "kafka" -> new KafkaSystem();
            case "dummy" -> new DummySystem();
            default -> throw new IllegalStateException( "Unexpected value: " + system.toLowerCase() );
        };
    }

    void prepare();

    Consumer<JsonNode> getSender();

    Runnable getReceiver( Workload workload, AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer );

}
