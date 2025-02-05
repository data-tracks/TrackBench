package dev.trackbench.util;

import dev.trackbench.display.LoadingBar;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CountRegistry {
    private final boolean synchronize;
    LoadingBar loadingBar;
    Map<Long, Long> counts = new ConcurrentHashMap<>();

    long total;
    long stepSize;

    @Getter
    long last = 0;


    /**
     *
     * @param total how much is the max
     * @param stepSize after how many steps should the graphic be updated
     * @param units what units are the entries of
     * @param synchronize if multiple threads are working on it,
     * which need to finish each step separately before progress can be updated or if they work together
     */
    public CountRegistry(long total, long stepSize, String units, boolean synchronize) {
        this.total = total;
        this.stepSize = stepSize;
        this.loadingBar = new LoadingBar(total, units);
        this.synchronize = synchronize;
    }

    public CountRegistry(long total, long stepSize, String units) {
        this(total, stepSize, units, true);
    }

    public void update(long id, long count) {
        counts.put(id, count);

        long next = last + stepSize;

        if ( counts.values().stream().allMatch( v -> v > next ) || !synchronize ) {
            synchronized ( this ){
                this.last = next;
                this.loadingBar.next( next );
            }
        }
    }
    public void done(){
        this.loadingBar.done();
    }

}
