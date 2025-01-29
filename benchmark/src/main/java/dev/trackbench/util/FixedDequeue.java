package dev.trackbench.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class FixedDequeue<D> {

    private final Deque<D> queue;
    private final int maxSize;


    public FixedDequeue( int maxSize ) {
        this.maxSize = maxSize;
        this.queue = new ArrayDeque<>( maxSize );
    }


    public void add( D data ) {
        if ( queue.size() >= maxSize ) {
            queue.removeFirst();
        }
        this.queue.add( data );
    }


    public boolean isEmpty() {
        return queue.isEmpty();
    }


    public D getLast() {
        return queue.getLast();
    }

}
