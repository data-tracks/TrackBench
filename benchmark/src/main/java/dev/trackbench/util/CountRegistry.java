package dev.trackbench.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CountRegistry {
    LoadingBar loadingBar;
    Map<Long, Long> counts = new ConcurrentHashMap<>();

    long total;
    long stepSize;

    long last = 0;

    public CountRegistry(long total, long stepSize, String units) {
        this.total = total;
        this.stepSize = stepSize;
        this.loadingBar = new LoadingBar(total, units);
    }

    public void update(long id, long count) {
        counts.put(id, count);

        long next = last + stepSize;
        if ( counts.values().stream().allMatch( v -> v > next ) ) {
            synchronized ( this ){
                this.last = next;
                this.loadingBar.next( next );
            }
        }
    }

}
