package dev.trackbench;

import dev.trackbench.analyse.Analyser;
import dev.trackbench.analyse.Comparer;
import dev.trackbench.analyse.OldAnalyser;
import dev.trackbench.configuration.BenchmarkConfig;
import dev.trackbench.configuration.BenchmarkContext;
import dev.trackbench.display.Display;
import dev.trackbench.execution.ExecutionCoordinator;
import dev.trackbench.simulation.RandomData;
import dev.trackbench.simulation.SensorGenerator;
import dev.trackbench.simulation.aggregate.AveragedData;
import dev.trackbench.simulation.aggregate.FinalData;
import dev.trackbench.simulation.processing.ProcessingGenerator;
import dev.trackbench.util.file.FileUtils;
import dev.trackbench.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "TrackBench", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "A heterogeneous, highly-configurable and highly-parallel stream processing benchmark.")
@Slf4j
public class Main implements Runnable {

    @Option(names = { "-p", "--prefix" }, description = "The output file which contains the summary of the evaluation.")
    private static String prefix;


    public static void main( String[] args ) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        //Get Data from Settings file
        BenchmarkConfig config = BenchmarkConfig.fromFile();
        BenchmarkContext context = new BenchmarkContext( config );
        config.setPrefix(prefix);

        // set to new seed
        RandomData.seed = config.seed();

        setPaths( config );

        if ( config.generate() ) {
            Display.INSTANCE.preInfo( "Generating sensors..." );
            context.printGeneratingTime();
            //Delete all files in folder
            FileUtils.deleteFolder( config.getDataPath() );

            //create files
            SensorGenerator.start( context );
        }

        if ( config.simulate() ) {
            context.loadNecessities();
            FileUtils.deleteFolder( config.getSimulationPath() );
            Display.INSTANCE.preInfo( "Generating processing..." );

            ProcessingGenerator.process( context );
        }

        if ( config.execute() ) {
            FileUtils.deleteFolder( config.getResultPath() );
            Display.INSTANCE.preInfo( "Starting processing..." );
            ExecutionCoordinator.start( context );
            Display.INSTANCE.preInfo( "Finished processing." );

        }

        if ( config.validate() ) {
            FileUtils.deleteFolder( config.getValidationPath() );
            Display.INSTANCE.preInfo( "Starting validation..." );
            Validator.start( context );
        }

        if ( config.analyze() ) {
            Display.INSTANCE.preInfo( "Starting analyser..." );
            Analyser.start( context );
        }

        cleanUp();

        try {
            Thread.sleep( 1000 );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        Display.INSTANCE.setFile( null );
        Display.INSTANCE.nextLine();
        Display.INSTANCE.doubleLine();
        Display.INSTANCE.preInfo( "🎉 All Processes Finished Successfully 🎉" );
        Display.INSTANCE.doubleLine();
    }


    private static void cleanUp() {
        FileUtils.close();
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
