package dev.trackbench.simulation;

import dev.trackbench.BenchmarkConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
public class DataGenerator {

    /**
     * Reads the data from the generated JSON files and stores them in a list (allData).
     * Always sorts the list by "prefix" and writes the sorted data back to a JSON file called ALL_DATA.json.
     */
    public static void dataGenerator( BenchmarkConfig config ) throws Exception {
        List<File> filenames = config.getFilesInFolder( config.getDataWithErrorPath() );

        log.info( "found {}", filenames );
    }
}