package dev.trackbench.util.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    private final boolean debug;
    List<JsonNode> cache = new ArrayList<>();
    private final BufferedReader reader;
    @Getter
    private Long lines = null;

    @Getter
    private long counter = 0;


    public JsonIterator( long readBatchSize, File target, boolean debug ) {
        this.file = target;
        this.readBatchSize = readBatchSize;
        this.debug = debug;
        try {
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
                JsonNode node = mapper.readTree( line );
                if ( node == null ) {
                    log.warn( "error parsing line: {}", line );
                }
                cache.add(node);
            }
            return !cache.isEmpty();
        } catch ( Exception e ) {
            return false;
        }
    }


    public long countLines() {
        if ( lines == null ) {
            this.lines = FileUtils.countLines( file, debug );
        }
        return lines;
    }


    public void offset( long offset ) {
        try {
            for ( long i = 0; i < offset; i++ ) {

                reader.readLine();
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

}
