package dev.trackbench.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class JsonIterator {

    private final long readBatchSize;
    private final ObjectMapper mapper = new ObjectMapper();
    @Getter
    private final File file;
    List<JsonNode> cache = new ArrayList<>();
    private final BufferedReader reader;
    @Getter
    private final long lines;

    @Getter
    private long counter = 0;


    public JsonIterator( long readBatchSize, File target ) {
        this.file = target;
        this.readBatchSize = readBatchSize;
        try {
            this.lines = FileUtils.countLines( target );
            this.reader = new BufferedReader( new FileReader( target ) );
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        }
    }



    @Nullable
    public JsonNode next() {
        if ( hasNext() ) {
            counter++;
            return cache.removeFirst();
        } else {
            return null;
        }
    }


    public boolean hasNext() {
        if ( !cache.isEmpty() ) {
            return true;
        }

        try {
            for ( long i = 0; i < readBatchSize; i++ ) {
                String line = reader.readLine();
                if ( line == null ) {
                    break;
                }
                cache.add( mapper.readTree( line ) );
            }
            return !cache.isEmpty();
        } catch ( Exception e ) {
            return false;
        }
    }

}
