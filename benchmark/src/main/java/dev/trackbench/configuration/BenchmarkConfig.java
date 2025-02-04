package dev.trackbench.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import dev.trackbench.Main;
import dev.trackbench.simulation.sensor.Sensor;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.trackbench.util.file.FileUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;

public record BenchmarkConfig(
        int seed,
        File path,
        boolean generate,
        boolean simulate,
        boolean execute,
        boolean validate,
        boolean analyze,
        boolean aggregated,
        long sensorBatchSize,
        long readBatchSize,
        float maxErrorAlteration,
        int sensorAmount,
        long ticks,
        long receivers,
        long stepDurationNs,
        long updateTickVisual
) {

    public static final String DEFAULT_SETTINGS_FILE = "settings.properties";

    public static final String DATA_PATH = "data";
    public static final String SIMULATION_PATH = "simulation";
    public static final String VALIDATION_PATH = "validation";
    public static final String SENSORS_PATH = "sensors";
    public static final String ERRORS_DATA_PATH = "errors";
    public static final String DATA_WITH_ERRORS_PATH = "data_and_errors";
    public static final String RESULT_PATH = "result";
    public static final long executionMaxMin = 2;
    public static final String ARRIVED_TICK_KEY = "arrived";


    public static BenchmarkConfig fromFile() {
        //Get Data from Settings file
        Configurations configs = new Configurations();
        PropertiesConfiguration props;
        try {
            props = configs.properties( Main.class.getClassLoader().getResource( DEFAULT_SETTINGS_FILE ) );
        } catch ( ConfigurationException e ) {
            throw new RuntimeException( e );
        }

        return new BenchmarkConfig(
                props.getInt( "seed" ),
                new File( props.getString( "path" ) ),
                props.getBoolean( "generate" ),
                props.getBoolean( "simulate" ),
                props.getBoolean( "execute" ),
                props.getBoolean( "validate" ),
                props.getBoolean( "analyze" ),
                props.getBoolean( "aggregatedData" ),
                getNumber( props, "sensorBatchSize" ),
                getNumber( props, "readBatchSize" ),
                props.getFloat( "maxErrorAlteration" ),
                props.getInt( "sensorAmount" ),
                getNumber( props, "ticks" ),
                getNumber( props, "receivers" ),
                props.getInt( "stepDurationNs" ),
                getNumber( props, "updateTickVisual" )
        );
    }


    private static long getNumber( PropertiesConfiguration props, String key ) {
        return Long.parseLong( props.getString( key ).replace( "_", "" ) );
    }


    public long factor() {
        return 100 / stepDurationNs * 5;
    }


    @NotNull
    public File getFileAndMkDirs( String... parentDirs ) {
        File file = new File( path.toString(), String.join( "/", parentDirs ) );
        boolean mkdirs = file.mkdirs();
        return file;
    }


    @NotNull
    private File getSensorJson( File path, Sensor sensor ) {
        return FileUtils.getJson( path, "%d_%s".formatted( sensor.id, sensor.getTemplate().getType() ) );
    }


    public File getDataPath() {
        return new File( path.toString(), DATA_PATH );
    }


    public File getResultFile(String workflow) {
        return getFileAndMkDirs( RESULT_PATH, workflow );
    }


    public File getResultFile( String workflow, long num ) {
        File file = getResultFile(workflow);

        return FileUtils.getJson( file, String.valueOf( num ) );
    }


    public File getSensorPath( Sensor sensor ) {
        File parent = getFileAndMkDirs( DATA_PATH, DATA_PATH );
        return getSensorJson( parent, sensor );
    }


    public File getErrorPath( Sensor sensor ) {
        File parent = getFileAndMkDirs( DATA_PATH, ERRORS_DATA_PATH );
        return getSensorJson( parent, sensor );
    }


    public File getDataWithErrorPath() {
        return getFileAndMkDirs( DATA_PATH, DATA_WITH_ERRORS_PATH );
    }


    public File getDataWithErrorPath( Sensor sensor ) {
        File parent = getDataWithErrorPath();
        return getSensorJson( parent, sensor );
    }


    public File getSimulationPath() {
        return getFileAndMkDirs( SIMULATION_PATH );
    }


    public long calculateErrorInterval( Sensor sensor, double rate ) {
        if ( rate == 0 ) {
            return ticks;
        }

        long ticksGenerated = ticks / sensor.getTemplate().getTickLength();

        long errorsTotal = (long) (ticksGenerated * rate);

        return ticks / errorsTotal;
    }


    public List<File> getFilesInFolder( File dir ) {
        if ( dir.isFile() ) {
            throw new IllegalArgumentException( "Path '" + path + "' exists but is not a folder." );
        }
        return Arrays.stream( Objects.requireNonNull( dir.listFiles() ) ).map( file -> new File( dir, file.getName() ) ).collect( Collectors.toList() );
    }

    public File getResultPath() {
        return getFileAndMkDirs( RESULT_PATH );
    }


    public List<File> getResultFiles() {
        File folder = getResultPath();
        return getFilesInFolder( folder );
    }


    public File getSensorPath() {
        File parent = getFileAndMkDirs( DATA_PATH );
        return FileUtils.getJson( parent, SENSORS_PATH );
    }


    public File getSimulationFile( String workflow, String name ) {
        File parent = getSimulationPath( workflow );
        return FileUtils.getJson( parent, name );
    }


    public File getSimulationPath( String workflow ) {
        return getFileAndMkDirs( SIMULATION_PATH, workflow.toLowerCase() );
    }


    public File getValidationFile( String name ) {
        return getFileAndMkDirs( VALIDATION_PATH, name );
    }

    public File getValidationPath() {
        return getFileAndMkDirs( VALIDATION_PATH );
    }

    public static long getArrivedTick(JsonNode node) {
        return node.get( ARRIVED_TICK_KEY ).asLong();
    }
}
