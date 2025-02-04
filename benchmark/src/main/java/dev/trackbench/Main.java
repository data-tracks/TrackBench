package dev.trackbench;

import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.execution.ExecutionCoordinator;
import dev.trackbench.simulation.aggregate.AveragedData;
import dev.trackbench.simulation.aggregate.FinalData;
import dev.trackbench.analyse.Analyser;
import dev.trackbench.analyse.Comparer;
import dev.trackbench.util.jsonHandler.JsonFileHandler;
import dev.trackbench.simulation.processing.ProcessingGenerator;
import dev.trackbench.execution.sending.SendCoordinator;
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
            log.info( "Generating sensors..." );
            context.printGeneratingTime();
            //Delete all files in folder
            JsonFileHandler.deleteFolder( config.getDataPath() );

            //create files
            SensorGenerator.start( context );
        }

        if ( config.simulate() ) {
            context.loadNecessities();
            JsonFileHandler.deleteFolder( config.getSimulationPath() );
            System.out.println( "Generating processing..." );

            ProcessingGenerator.process( context );
        }

        if ( config.execute() ) {
            JsonFileHandler.deleteFolder( config.getResultPath() );
            System.out.println( "Starting processing..." );
            ExecutionCoordinator.start(context);
            System.out.println( "Finished processing." );

        }

        if ( config.validate() ) {
            JsonFileHandler.deleteFolder( config.getValidationPath() );
            System.out.println( "Starting validation..." );
            Validator.start(context);
        }

        if( config.analyze() ) {
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
        Comparer.setFolder( config.path() );
    }

}
