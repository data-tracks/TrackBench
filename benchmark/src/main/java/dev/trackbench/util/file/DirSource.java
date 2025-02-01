package dev.trackbench.util.file;


import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirSource implements JsonSource {

    private final File directory;
    private final List<File> files;
    private final List<JsonIterator> iters;
    private final long readBatchSize;


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
        return files.stream().map( ( File target ) -> FileUtils.countLines( target, false ) ).reduce( 0L, Long::sum );
    }


    @Override
    public JsonSource copy() {
        return new DirSource( directory, readBatchSize );
    }


    @Override
    public void offset( long offset ) {
        long currentOffset = offset;

        long toRemove = 0;
        for ( JsonIterator iter : iters ) {
            currentOffset -= iter.countLines();
            if ( currentOffset < 0 ) {
                iters.getFirst().offset( currentOffset );
                break;
            } else {
                toRemove++;
            }
        }

        for ( long i = 0; i < toRemove; i++ ) {
            iters.removeFirst();
        }
    }


    @Override
    public void reset() {
        this.iters.clear();
        this.iters.addAll( files.stream().map( f -> new JsonIterator( readBatchSize, f, false ) ).toList() );
    }

}
