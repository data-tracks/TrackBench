package dev.trackbench.util.file;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;

public class FileSource implements JsonSource {

    private final File file;
    private JsonIterator json;
    private final long readBatchSize;
    private Long count;


    public FileSource( File file, long readBatchSize ) {
        this.file = file;
        this.json = new JsonIterator( readBatchSize, file, false );
        this.readBatchSize = readBatchSize;
    }


    @Override
    public boolean hasNext() {
        return json.hasNext();
    }


    @Override
    public JsonNode next() {
        return json.next();
    }


    @Override
    public long countLines() {
        if (count == null) {
            count = json.countLines();
        }
        return count;
    }


    @Override
    public JsonSource copy() {
        FileSource source = new FileSource( file, readBatchSize );
        source.count = count;
        return source;
    }


    @Override
    public void offset( long start ) {
        json.offset( start );
    }


    @Override
    public void reset() {
        this.json = new JsonIterator( readBatchSize, file, false );
    }

}
