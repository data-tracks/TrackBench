package dev.datageneration.simulation;

import dev.datageneration.Main;
import dev.datageneration.simulation.sensors.Sensor;
import java.io.File;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public record BenchmarkConfig(
        int seed,
        boolean generate,
        boolean execute,
        boolean aggregated,
        long sensorBatchSize,
        float maxErrorAlteration,
        int threadAmount,
        int sensorAmount,
        long ticks,
        long stepDurationMs,
        File path,
        File pathSensorData,
        long updateTickVisual
) {

    public static final String DEFAULT_SETTINGS_FILE = "settings.properties";

    public static final String DATA_PATH = "data";
    public static final String ERRORS_DATA_PATH = "errors";
    public static final String DATA_WITH_ERRORS_PATH = "data_and_errors";


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
                props.getBoolean( "generate" ),
                props.getBoolean( "execute" ),
                props.getBoolean( "aggregatedData" ),
                getNumber( props, "sensorBatchSize" ),
                props.getFloat( "maxErrorAlteration" ),
                props.getInt( "threadAmount" ),
                props.getInt( "sensorAmount" ),
                getNumber( props, "ticks" ),
                props.getInt( "stepDurationMs" ),
                new File( props.getString( "pathAggregated" ) ),
                new File( props.getString( "pathSensor" ) ),
                getNumber( props, "updateTickVisual" )
        );
    }


    private static long getNumber( PropertiesConfiguration props, String key ) {
        return Long.parseLong( props.getString( key ).replace( "_", "" ) );
    }


    public long factor() {
        return 100 / stepDurationMs * 5;
    }


    public File getDataPath( Sensor sensor ) {
        boolean success = new File( pathSensorData.toString(), DATA_PATH ).mkdirs();
        return new File( "%s/%s/%d_%s.json".formatted( pathSensorData.toString(), DATA_PATH, sensor.id, sensor.getTemplate().getType() ) );
    }


    public File getErrorPath( Sensor sensor ) {
        boolean success = new File( pathSensorData.toString(), ERRORS_DATA_PATH ).mkdirs();
        return new File( "%s/%s/%d_%s.json".formatted( pathSensorData.toString(), ERRORS_DATA_PATH, sensor.id, sensor.getTemplate().getType() ) );
    }


    public File getDataWithErrorPath( Sensor sensor ) {
        boolean success = new File( pathSensorData.toString(), DATA_WITH_ERRORS_PATH ).mkdirs();
        return new File( "%s/%s/%d_%s.json".formatted( pathSensorData.toString(), DATA_WITH_ERRORS_PATH, sensor.id, sensor.getTemplate().getType() ) );
    }


    public long calculateErrorInterval( Sensor sensor ) {
        if ( sensor.getTemplate().getErrorRate() == 0 ) {
            return ticks;
        }

        long ticksGenerated = ticks / sensor.getTemplate().getTickLength();

        long errorsTotal = (long) (ticksGenerated * sensor.getTemplate().getErrorRate());

        return ticks / errorsTotal;
    }


}
