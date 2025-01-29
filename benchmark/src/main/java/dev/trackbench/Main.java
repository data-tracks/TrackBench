package dev.trackbench;

import dev.trackbench.processing.ProcessingCoordinator;
import dev.trackbench.simulation.aggregate.AveragedData;
import dev.trackbench.simulation.aggregate.FinalData;
import dev.trackbench.analyse.Analyser;
import dev.trackbench.analyse.Comparer;
import dev.trackbench.jsonHandler.JsonFileHandler;
import dev.trackbench.simulation.processing.ProcessingGenerator;
import dev.trackbench.sending.SendCoordinator;
import dev.trackbench.simulation.RandomData;
import dev.trackbench.simulation.SensorGenerator;
import dev.trackbench.system.DummySystem;
import dev.trackbench.validation.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    static final String tire = "src/main/java/dev/datageneration/kafka/KafkaTools/Tire.java";


    public static void main( String[] args ) throws Exception {
        //Get Data from Settings file
        BenchmarkConfig config = BenchmarkConfig.fromFile();
        BenchmarkContext context = new BenchmarkContext( config, new DummySystem() );
        // set to new seed
        RandomData.seed = config.seed();

        setPaths( config );

        if ( config.generate() ) {
            System.out.println( "Generating sensors..." );
            context.printGeneratingTime();
            //Delete all files in folder
            JsonFileHandler.deleteFolder( config.getDataPath() );

            //create files
            SensorGenerator.start( context );
        }

        if ( config.simulate() ) {
            context.loadNecessities();
            JsonFileHandler.deleteFolder( config.getProcessingPath() );
            System.out.println( "Generating processing..." );

            ProcessingGenerator.process( context );
        }

        if ( config.execute() ) {
            System.out.println( "Starting processing..." );
            ProcessingCoordinator.start(context);
            System.out.println( "Finished processing." );

            Validator.start(context);


            System.out.println( "Starting analyser..." );
            Analyser.start(context);
        }
        log.info( "Finished everything" );
    }


    private static void setPaths( BenchmarkConfig config ) {
        //Set data for all files
        RandomData.setPeek( config.factor() );
        Analyser.setAmountSensors( config.sensorAmount() );
        Analyser.setFolder( config.path() );
        AveragedData.setFolderStore( config.path() );
        FinalData.setFolderStore( config.path() );
        SendCoordinator.setPathNormal( config.path() );
        Comparer.setFolder( config.path() );
    }

}
