package dev.trackbench.util.file;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;

public interface JsonSource {


    static JsonSource of( File path, long readBatchSize ) {

        if ( !path.exists() ) {
            throw new IllegalArgumentException( "Path does not exist: " + path );
        }
        if ( path.isDirectory() ) {
            return new DirSource(path, readBatchSize);
        }else {
            return new FileSource(path, readBatchSize);
        }

    }

    boolean hasNext();

    JsonNode next();

    long countLines();

    JsonSource copy();

    void offset( long start );

    void reset();



}
