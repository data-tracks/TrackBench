package dev.datageneration;

import dev.datageneration.aggregate.AveragedData;
import dev.datageneration.aggregate.ErrorCreator;
import dev.datageneration.aggregate.FinalData;
import dev.datageneration.aggregate.WindowedData;
import dev.datageneration.analyse.Analyser;
import dev.datageneration.analyse.Comparer;
import dev.datageneration.jsonHandler.JsonFileHandler;
import dev.datageneration.receiver.DataReceiver;
import dev.datageneration.simulation.DataGenerator;
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
        // set to new seed
        RandomData.seed = config.seed();

        setPaths(config);

        //Start Creation of Data
        if(config.generate()){
            //Delete all files in folder
            JsonFileHandler.deleteAllJsonFiles();

            //create files
            SensorGenerator.start(config);
            //ErrorCreator.dataWithErrors(); //create some data loss and null entries.
            DataGenerator.dataGenerator(config);
            /*WindowedData.createWindowedData(); //creates warnings if some data is not in a wished range
            AveragedData.aggregatedData(config.stepDurationMs()); //get average over a time interval
            FinalData.createFinalData();*/
        }

        if ( !config.execute() ){
            return;
        }

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
        AveragedData.setFolderData(config.pathSensorData());
        AveragedData.setFolderStore(config.path());
        ErrorCreator.setFolderData(config.pathSensorData());
        FinalData.setFolderStore(config.path());
        WindowedData.setFolderData(config.pathSensorData());
        WindowedData.setFolderStore(config.path());
        ThreadedSender.setPathNormal(config.path());
        Comparer.setFolder(config.path());
        DataGenerator.setFolderStore(config.path());
        SensorGenerator.setFolder(config.pathSensorData());
        JsonFileHandler.setFolderAggregated(config.path());
        JsonFileHandler.setFolderSensors(config.pathSensorData());
    }

}
