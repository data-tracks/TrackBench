package dev.trackbench.util.file;


import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirSource implements JsonSource {

    private final File directory;
    private final List<File> files;
    private final List<JsonIterator> iters;
    private final List<Long> counts = new ArrayList<>();
    private final long readBatchSize;
    private Long count = null;


    public DirSource( File directory, long readBatchSize ) {
        this.directory = directory;
        this.files = List.of( Objects.requireNonNull( directory.listFiles() ) );
        this.iters = new ArrayList<>( files.stream().map( f -> new JsonIterator( readBatchSize, f, false ) ).toList() );
        this.readBatchSize = readBatchSize;
    }


    @Override
    public boolean hasNext() {
        if ( iters.isEmpty() ) {
            return false;
        }

        if ( !iters.getFirst().hasNext() ) {
            iters.removeFirst();
            return hasNext();
        }

        return true;
    }


    @Override
    public JsonNode next() {
        if ( !hasNext() ) {
            return null;
        }
        return iters.getFirst().next();
    }


    @Override
    public long countLines() {
        if ( count == null ) {
            for ( File file : files ) {
                counts.add( FileUtils.countLines( file, false ) );
            }

            count = counts.stream().reduce( 0L, Long::sum );
        }
        return count;
    }


    @Override
    public JsonSource copy() {
        DirSource dir = new DirSource( directory, readBatchSize );
        dir.count = count;
        return dir;
    }


    @Override
    public void offset( long offset ) {
        long currentOffset = offset;

        long toRemove = 0;

        for ( long count : counts ) {
            currentOffset -= count;
            if ( currentOffset < 0 ) {
                iters.getFirst().offset( currentOffset );
                break;
            } else {
                toRemove++;
            }
        }

        for ( long i = 0; i < toRemove; i++ ) {
            iters.removeFirst();
            counts.removeFirst();
        }
    }


    @Override
    public void reset() {
        this.iters.clear();
        this.iters.addAll( files.stream().map( f -> new JsonIterator( readBatchSize, f, false ) ).toList() );
    }


    @Override
    public String toString() {
        return String.format( "directory: %s", directory  );
    }

}
