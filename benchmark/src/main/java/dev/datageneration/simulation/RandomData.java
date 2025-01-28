package dev.datageneration.simulation;

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
    public static Random random = new Random();
    public static long seed = 795673489;
    @Setter
    public static double peek;


    public static void setSeed( long s ) {
        seed = s;
        random = new Random(s);
    }

    public static void resetRandom() {
        random = new Random(seed);
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

    public static double getRandomWithProbability(double min, double max, String name, double current) {
        double range = max - min;

        if (probabilities.containsKey(name)) {
            double[] prob = probabilities.get(name);
            double boundary = range * prob[0];

            double biasFactor = random.nextDouble() <= prob[0] ? 0.7 : 0.3; // Stronger bias for high probability
            double rangeMin = (random.nextDouble() <= prob[0]) ? min : boundary;
            double rangeMax = (random.nextDouble() <= prob[0]) ? boundary : max;

            return getBiasedRandom(rangeMin, rangeMax, current, biasFactor);
        } else {
            return getRandom(min, max);
        }
    }

    private static double getBiasedRandom(double rangeMin, double rangeMax, double current, double biasFactor) {
        double randomValue = rangeMin + random.nextDouble() * (rangeMax - rangeMin);
        return (1 - biasFactor) * randomValue + biasFactor * current;
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


}
