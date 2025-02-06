package dev.trackbench.util;

import dev.trackbench.display.LoadingBar;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleCountRegistry {

    LoadingBar loadingBar;

    long total;
    AtomicLong count = new AtomicLong(0);


    public SimpleCountRegistry( long total, String units ) {
        this.total = total;
        this.loadingBar = new LoadingBar( total, units );
    }


    public void finish() {
        synchronized ( this ) {
            this.loadingBar.next( count.incrementAndGet() );
        }
    }
    public void done() {
        this.loadingBar.done();
    }
}
