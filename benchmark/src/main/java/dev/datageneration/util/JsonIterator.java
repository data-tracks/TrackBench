package dev.datageneration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datageneration.simulation.BenchmarkConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class JsonIterator {

    private final BenchmarkConfig benchmarkConfig;
    private final ObjectMapper mapper = new ObjectMapper();
    List<JsonNode> cache = new ArrayList<>();
    private final BufferedReader reader;
    @Getter
    private long counter = 0;


    public JsonIterator( BenchmarkConfig benchmarkConfig, File target ) {
        this.benchmarkConfig = benchmarkConfig;
        try {
            countLines( target );
            this.reader = new BufferedReader( new FileReader( target ) );
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        }
    }


    private static void countLines( File target ) throws FileNotFoundException {

        try ( RandomAccessFile file = new RandomAccessFile( target, "r" ); FileChannel channel = file.getChannel() ) {

            MappedByteBuffer buffer = channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
            int lineCount = 0;

            for ( int i = 0; i < buffer.limit(); i++ ) {
                if ( buffer.get( i ) == '\n' ) {
                    lineCount++;
                }
            }

            log.info( "File {} has {} lines", target.getName(), lineCount );
        } catch ( IOException e ) {
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
            for ( long i = 0; i < benchmarkConfig.readBatchSize(); i++ ) {
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
