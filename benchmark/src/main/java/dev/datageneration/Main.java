package dev.datageneration;

import dev.datageneration.aggregate.AveragedData;
import dev.datageneration.aggregate.ErrorCreator;
import dev.datageneration.aggregate.FinalData;
import dev.datageneration.processing.ProcessingGenerator;
import dev.datageneration.analyse.Analyser;
import dev.datageneration.analyse.Comparer;
import dev.datageneration.jsonHandler.JsonFileHandler;
import dev.datageneration.receiver.DataReceiver;
import dev.datageneration.simulation.BenchmarkContext;
import dev.datageneration.simulation.RandomData;
import dev.datageneration.simulation.SensorGenerator;
import dev.datageneration.sending.ThreadedSender;

import dev.datageneration.simulation.BenchmarkConfig;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    static final String tire = "src/main/java/dev/datageneration/kafka/KafkaTools/Tire.java";

    public static void main(String[] args) throws Exception {
        //Get Data from Settings file
        BenchmarkConfig config = BenchmarkConfig.fromFile();
        BenchmarkContext context = new BenchmarkContext( config );
        // set to new seed
        RandomData.seed = config.seed();

        setPaths(config);

        //Start Creation of Data
        if(config.generateSensors()){
            System.out.println("Generating sensors...");
            //Delete all files in folder
            JsonFileHandler.deleteFolder(config.getDataPath());

            //create files
            SensorGenerator.start(context);
        }

        if ( config.generateProcessing() ) {
            context.loadNecessities();
            JsonFileHandler.deleteFolder(config.getProcessingPath());
            System.out.println("Generating processing...");

            ProcessingGenerator.process(context);
        }


        if ( !config.execute() ){
            return;
        }
        System.out.println("Starting processing...");

        //Start Sending to Stream processing System and start receiver
        Thread sendThread = new Thread(() -> {
            try {
                ThreadedSender.sendThreaded(config.aggregated(), config.threadAmount(), config.stepDurationMs());
            } catch (Exception e) {
                log.warn( e.getMessage() );
            }
        });

        Thread receiveThread = new Thread(() -> {
            try {
                DataReceiver.receive(config.aggregated());
            } catch (Exception e) {
                log.warn( e.getMessage() );
            }
        });
        receiveThread.start();
        TimeUnit.SECONDS.sleep(1);
        sendThread.start();


        sendThread.join();
        receiveThread.join();
        log.info("Finished everything");
    }


    private static void setPaths( BenchmarkConfig config ) {
        //Set data for all files
        RandomData.setPeek(config.factor());
        Analyser.setAmountSensors(config.sensorAmount());
        Analyser.setThreadAmount(config.threadAmount());
        Analyser.setFolder(config.path());
        AveragedData.setFolderStore(config.path());

        FinalData.setFolderStore(config.path());
        ThreadedSender.setPathNormal(config.path());
        Comparer.setFolder(config.path());
    }

}
