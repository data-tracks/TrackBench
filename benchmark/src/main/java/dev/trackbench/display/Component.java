package dev.trackbench.display;

public interface Component {

    void render();

    default void start(Runnable runnable) {
        render();
        runnable.run();
    }
}
