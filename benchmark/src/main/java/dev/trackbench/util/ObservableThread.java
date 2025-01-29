package dev.trackbench.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class ObservableThread extends Thread {
    public final AtomicBoolean ready = new AtomicBoolean(false);
    public final AtomicBoolean running = new AtomicBoolean(true);


    public ObservableThread( Runnable runnable ) {
        super( runnable );
    }

    public ObservableThread() {
        super();
    }

}
