package dev.trackbench;

import dev.trackbench.analyse.Analyser;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.display.Display;
import dev.trackbench.execution.ExecutionCoordinator;
import dev.trackbench.simulation.aggregate.AveragedData;
import dev.trackbench.simulation.aggregate.FinalData;
import dev.trackbench.analyse.OldAnalyser;
import dev.trackbench.analyse.Comparer;
import dev.trackbench.util.jsonHandler.JsonFileHandler;
import dev.trackbench.simulation.processing.ProcessingGenerator;
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

        Display display = Display.INSTANCE;

        setPaths( config );

        if ( config.generate() ) {
            Display.INSTANCE.info( "Generating sensors..." );
            context.printGeneratingTime();
            //Delete all files in folder
            JsonFileHandler.deleteFolder( config.getDataPath() );

            //create files
            SensorGenerator.start( context );
        }

        if ( config.simulate() ) {
            context.loadNecessities();
            JsonFileHandler.deleteFolder( config.getSimulationPath() );
            Display.INSTANCE.info( "Generating processing..." );

            ProcessingGenerator.process( context );
        }

        if ( config.execute() ) {
            JsonFileHandler.deleteFolder( config.getResultPath() );
            Display.INSTANCE.info( "Starting processing..." );
            ExecutionCoordinator.start(context);
            Display.INSTANCE.info( "Finished processing." );

        }

        if ( config.validate() ) {
            JsonFileHandler.deleteFolder( config.getValidationPath() );
            Display.INSTANCE.info( "Starting validation..." );
            Validator.start(context);
        }

        if( config.analyze() ) {
            Display.INSTANCE.info( "Starting analyser..." );
            Analyser.start(context);
        }

        Display.INSTANCE.info( "Finished everything" );
    }


    private static void setPaths( BenchmarkConfig config ) {
        //Set data for all files
        RandomData.setPeek( config.factor() );
        OldAnalyser.setAmountSensors( config.sensorAmount() );
        OldAnalyser.setFolder( config.path() );
        AveragedData.setFolderStore( config.path() );
        FinalData.setFolderStore( config.path() );
        Comparer.setFolder( config.path() );
    }

}
