package dev.trackbench.util.file;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;

public class FileSource implements JsonSource {

    private final File file;
    private JsonIterator json;
    private final long readBatchSize;


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
        return json.countLines();
    }


    @Override
    public JsonSource copy() {
        return new FileSource( file, readBatchSize );
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
