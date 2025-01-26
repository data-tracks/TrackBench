package dev.datageneration.simulation;

import dev.datageneration.simulation.Sensors.DocSensor;
import dev.datageneration.simulation.Sensors.Sensor;
import dev.datageneration.simulation.Sensors.SensorTemplate;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RandomData {

    public static List<SensorTemplate> sensorTemplates = List.of(
            SensorTemplate.of( "heat", "temperature c" ),//heat sensor
            SensorTemplate.of( "heat", "temperature c" ),//heat sensor,
            SensorTemplate.of( "tire", "temperature tire", "pressure psi", "wear", "liability", "position" ),//front_left_tyre
            SensorTemplate.of( "tire", "temperature tire", "pressure psi", "wear", "liability", "position" ),//front_right_tyre
            SensorTemplate.of( "tire", "temperature tire", "pressure psi", "wear", "liability", "position" ),//rear_left_tyre
            SensorTemplate.of( "tire", "temperature tire", "pressure psi", "wear", "liability", "position" ),//rear_right_tyre
            SensorTemplate.of( "speed", "kph", "mph", "acceleration", "wind speed" ),//speed_sensor
            SensorTemplate.of( "gForce", "g-lateral", "g-longitudinal" ),//g_sensor
            SensorTemplate.of( "fuelPump", "temperature fuelP", "ml/min" ),//fuel_pump_sensor
            SensorTemplate.of( "DRS", "on/off", "drs-zone" ),//drs_sensor
            SensorTemplate.of( "brake", "temperature brake", "brake_pressure", "wear" ),//front_left_brake
            SensorTemplate.of( "brake", "temperature brake", "brake_pressure", "wear" ),//front_right_brake
            SensorTemplate.of( "brake", "temperature brake", "brake_pressure", "wear" ),//rear_left_brake
            SensorTemplate.of( "brake", "temperature brake", "brake_pressure", "wear" ),//rear_right_brake
            SensorTemplate.of( "accelerometer", "throttlepedall" ),
            SensorTemplate.of( "engine", "temperature engine", "rpm", "fuelFlow", "oil_pressure", "fuel_pressure", "exhaust" ),
            SensorTemplate.of( "blackbox", "array_of_data" ),
            SensorTemplate.of( "steering", "direction", "turning_degree" ) );


    public static Random random = new Random();
    public static long seed = 795673489;
    @Setter
    public static double peek;


    public static void setSeed( long s ) {
        random.setSeed( s );
    }


    /**
     * Creates a random number(double) between the given min and max values.
     *
     * @param min value
     * @param max value
     * @return random number between min and max
     */
    public static double getRandom( double min, double max ) {
        return random.nextDouble() * (max - min) + min;
    }


    public static Map<String, double[]> probabilities = new HashMap<>() {{
        put( "temperature tire", new double[]{ 0.999, 0.001 } ); //TODO: adjust the probabilities
        put( "temperature c", new double[]{ 0.999, 0.001 } );
        put( "pressure psi", new double[]{ 0.999, 0.001 } );
        put( "liability", new double[]{ 0.999, 0.001 } );
        put( "kmp/h", new double[]{ 0.999, 0.001 } );
        put( "mp/h", new double[]{ 0.999, 0.001 } );
        put( "acceleration", new double[]{ 0.999, 0.001 } );
        put( "wind speed", new double[]{ 0.999, 0.001 } );
        put( "g-lateral", new double[]{ 0.999, 0.001 } );
        put( "g-longitudinal", new double[]{ 0.999, 0.001 } );
        put( "temperature fuelP", new double[]{ 0.999, 0.001 } );
        put( "ml/min", new double[]{ 0.999, 0.001 } );
        put( "temperature brake", new double[]{ 0.999, 0.001 } );
        put( "brake_pressure", new double[]{ 0.999, 0.001 } );
        put( "wear", new double[]{ 0.999, 0.001 } );
        put( "temperature engine", new double[]{ 0.999, 0.001 } );
        put( "rpm", new double[]{ 0.999, 0.001 } );
        put( "fuelFlow", new double[]{ 0.999, 0.001 } );
        put( "oil_pressure", new double[]{ 0.999, 0.001 } );
        put( "fuel_pressure", new double[]{ 0.999, 0.001 } );
        put( "exhaust", new double[]{ 0.999, 0.001 } );
        put( "throttlepedall", new double[]{ 0.999, 0.001 } );
    }};


    public static double getRandomWithProbability( double min, double max, String name ) {
        double range = max - min;
        if ( probabilities.containsKey( name ) ) {
            double[] prob = probabilities.get( name );
            double boundary = range * prob[0];
            double[] highProb = new double[]{ min, boundary };
            double[] lowProb = new double[]{ boundary, max };
            if ( random.nextDouble() <= prob[0] ) {
                return random.nextDouble() * (highProb[1] - highProb[0]) + highProb[0];
            } else {
                return peek * (random.nextDouble() * (lowProb[1] - lowProb[0]) + lowProb[0]);
            }
        } else {
            return getRandom( min, max );
        }
    }


    /**
     * The real creator of the sensors.
     * Creates sensors accordingly to the chosen amount.
     *
     * @param sensorAmount amount of sensors
     * @return list of sensors
     */
    public static List<Sensor> createSensors( long ticks, int sensorAmount ) {
        List<Sensor> sensors = new ArrayList<>();

        for ( int i = 0; i < sensorAmount; i++ ) {
            int pickedSensorIndex = (int) getRandom(0, RandomData.sensorTemplates.size());
            sensors.add( chooseSensor( RandomData.sensorTemplates.get( pickedSensorIndex ) ) );
        }

        long tick = 0;
        for ( long i = 0; i < ticks; i++ ) {
            for ( Sensor sensor : sensors ) {
                sensor.simulateTick(tick);
            }
        }

        // we print the summary for debug purposes
        for ( Sensor sensor : sensors ) {
            log.info( "Sensor:{} generated {} ticks, one every {} tick(s)", sensor.getTemplate().getType(), sensor.getMetric().ticksGenerated, sensor.getTemplate().getTickLength() );
        }

        return sensors;
    }


    public static List<String> listFilesForFolder( final File folder ) {
        List<String> filenames = new ArrayList<>();
        for ( final File fileEntry : Objects.requireNonNull( folder.listFiles() ) ) {
            if ( fileEntry.isDirectory() ) {
                listFilesForFolder( fileEntry );
            } else {
                if ( fileEntry.getName().contains( ".json" ) ) {
                    filenames.add( fileEntry.getName() );
                }
            }
        }
        return filenames;
    }


    public static Sensor chooseSensor( SensorTemplate template ) {
        return new DocSensor( template );
    }

}
