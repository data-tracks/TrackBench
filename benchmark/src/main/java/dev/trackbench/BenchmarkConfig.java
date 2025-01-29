package dev.trackbench;

import dev.trackbench.simulation.sensor.Sensor;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    public static final String PROCESSING_PATH =  "processing";
    public static final String SENSORS_PATH = "sensors";
    public static final String ERRORS_DATA_PATH = "errors";
    public static final String DATA_WITH_ERRORS_PATH = "data_and_errors";
    public static final String RESULT_PATH = "result";
    public static final long executionMaxMin = 5;


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
    private File getFileAndMkDirs(String... parentDirs) {
        File file = new File( path.toString(), String.join( "/", parentDirs ) );
        boolean mkdirs = file.mkdirs();
        return file;
    }

    @NotNull
    private File getSensorJson( File path, Sensor sensor ) {
        return getJson( path, "%d_%s.json".formatted( sensor.id, sensor.getTemplate().getType() ) );
    }

    @NotNull
    private File getJson( File path, String name ) {
        return new File( path, "%s.json".formatted( name ) );
    }


    public File getDataPath(){
        return new File( path.toString(), DATA_PATH );
    }

    public File getResultPath( long num ) {
        File file = getFileAndMkDirs( RESULT_PATH );
        return getJson( file, String.valueOf( num ) );
    }

    public File getSensorPath( Sensor sensor ) {
        File parent = getFileAndMkDirs(DATA_PATH, DATA_PATH);
        return getSensorJson( parent, sensor );
    }


    public File getErrorPath( Sensor sensor ) {
        File parent = getFileAndMkDirs(DATA_PATH, ERRORS_DATA_PATH);
        return getSensorJson( parent, sensor );
    }

    public File getDataWithErrorPath() {
        return getFileAndMkDirs(DATA_PATH, DATA_WITH_ERRORS_PATH);
    }


    public File getDataWithErrorPath( Sensor sensor ) {
        File parent = getDataWithErrorPath();
        return getSensorJson( parent, sensor );
    }


    public File getProcessingPath() {
        return getFileAndMkDirs(PROCESSING_PATH);
    }



    public File getSingleProcessingPath( Sensor sensor, String name ) {


        File parent = getProcessingPath();
        File path = new File( "%s/%d_%s".formatted( parent.getAbsolutePath(), sensor.id, sensor.getTemplate().getType() ) );
        boolean success = path.mkdirs();

        return getJson( path, name.replace( "/", "-" ) );
    }


    public long calculateErrorInterval( Sensor sensor, double rate ) {
        if ( rate == 0 ) {
            return ticks;
        }

        long ticksGenerated = ticks / sensor.getTemplate().getTickLength();

        long errorsTotal = (long) (ticksGenerated * rate);

        return ticks / errorsTotal;
    }


    public List<File> getSensorFiles(File dir) {
        if ( dir.isFile() ) {
            throw new IllegalArgumentException( "Sensor file path '" + path + "' exists but is not a file" );
        }
        return Arrays.stream( Objects.requireNonNull( dir.listFiles() ) ).map( file -> new File( dir, file.getName() ) ).collect( Collectors.toList() );
    }


    public File getSensorPath() {
        File parent = getFileAndMkDirs( DATA_PATH );
        return getJson( parent, SENSORS_PATH  );
    }




}
