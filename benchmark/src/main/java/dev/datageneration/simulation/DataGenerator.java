package dev.datageneration.simulation;

import dev.datageneration.jsonHandler.JsonFileHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import static dev.datageneration.jsonHandler.JsonFileHandler.writeJsonFile;

@Slf4j
public class DataGenerator {

    /**
     * Reads the data from the generated JSON files and stores them in a list (allData).
     * Always sorts the list by "prefix" and writes the sorted data back to a JSON file called ALL_DATA.json.
     */
    public static void dataGenerator( BenchmarkConfig config ) throws Exception {
        List<File> filenames = config.getSensorFiles( BenchmarkConfig.DATA_WITH_ERRORS_PATH );

        log.info( "found {}", filenames );
    }
}