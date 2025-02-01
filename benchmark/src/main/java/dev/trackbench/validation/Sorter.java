package dev.trackbench.validation;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.util.file.FileUtils;
import dev.trackbench.util.file.JsonIterator;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

public class Sorter {

    private final File file;
    private final long readBatchSize;

    private final Function<JsonNode, Long> comparator;


    public Sorter( File file, long readBatchSize, Function<JsonNode, Long> comparator ) {
        this.file = file;
        this.readBatchSize = readBatchSize;
        this.comparator = comparator;
    }


    public void sort() {
        if ( file.isFile() ) {
            sortFile( file );
        } else {
            sortFolder();
        }
    }


    private void sortFolder() {
        File folder = FileUtils.createFolderAndMove( file, "unsorted" );

        List<File> files = FileUtils.getJsonFiles( folder );

        List<Thread> threads = new ArrayList<>();
        for ( File file : files ) {
            Thread thread = new Thread( () -> {
                sortFile( file );
            } );
            threads.add( thread );
            thread.start();
        }

        try {
            for ( Thread thread : threads ) {
                thread.join();
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }


    private void sortFile( File file ) {
        PriorityQueue<JsonNode> nodes = new PriorityQueue<>( Comparator.comparing( comparator ));

        JsonIterator iterator = new JsonIterator( readBatchSize, file, false );

        while ( iterator.hasNext() ) {
            nodes.add( iterator.next() );
        }
    }

}
