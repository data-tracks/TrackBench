package dev.datageneration.util;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import dev.datageneration.simulation.BenchmarkConfig;
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
    final BenchmarkConfig config;

    long counter = 0;
    List<String> batch = new ArrayList<>();


    public FileJsonTarget( File file, BenchmarkConfig config ) {
        this.file = file;
        this.config = config;
        try {
            this.writer = new BufferedWriter( new FileWriter( file, true ) );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void attach( JsonNode value ) {
        if ( counter < config.sensorBatchSize() ) {
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
