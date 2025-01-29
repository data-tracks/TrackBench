package dev.trackbench.system;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.receiver.Buffer;
import dev.trackbench.util.Clock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface System {

    void prepare();

    Consumer<JsonNode> getSender();

    Runnable getReceiver( AtomicBoolean running, AtomicBoolean ready, Clock clock, Buffer dataConsumer );

}
