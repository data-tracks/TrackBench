package dev.trackbench.util.file;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.configuration.BenchmarkConfig;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileJsonTarget implements JsonTarget {

    final File file;
    final Writer writer;
    final long sensorBatchSize;

    long counter = 0;
    List<String> batch = new ArrayList<>();


    public FileJsonTarget( File file, long sensorBatchSize ) {
        this.file = file;
        this.sensorBatchSize = sensorBatchSize;
        try {
            this.writer = new BufferedWriter( new FileWriter( file, true ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public FileJsonTarget( File file, BenchmarkConfig config ) {
        this(file, config.sensorBatchSize());
    }


    @Override
    public void attach( JsonNode value ) {
        if ( counter < sensorBatchSize ) {
            batch.add( value.toString() );
            counter++;
            return;
        }
        try {
            writeBatch();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        batch.clear();
        counter = 0;
    }


    private void writeBatch() throws IOException {
        StringBuilder builder = new StringBuilder();

        for ( String entry : batch ) {
            builder.append( entry ).append( "\n" );
        }
        writer.append( builder.toString() );
    }


    @Override
    public void close() throws IOException {
        writeBatch();
        writer.flush();
        writer.close();
    }


}
