package dev.trackbench.display;

import java.util.function.Consumer;
import lombok.Getter;

public abstract class Component {

    @Getter
    private Consumer<String> consumer;

    abstract void render();

    void start(Runnable runnable, Consumer<String> msgConsumer) {
        this.consumer = msgConsumer;
        render();
        runnable.run();
    }
}
