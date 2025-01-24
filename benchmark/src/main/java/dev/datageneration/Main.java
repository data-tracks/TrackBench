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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    //change path to where settings.txt if stored
    static final String path = "src/main/resources";
    static final String SETTINGS_PATH = "setting.properties";
    static final String tire = "src/main/java/dev/datageneration/kafka/KafkaTools/Tire.java";
    static boolean aggregated;
    static boolean createData;
    public static void main(String[] args) throws Exception {

        //Get Data from Settings file
        Properties props = new Properties();

        try ( InputStream in = Main.class.getResourceAsStream(SETTINGS_PATH)) {
            if (in == null) {
                throw new RuntimeException("Could not find " + SETTINGS_PATH);
            }
            props.load(in);
        }

        //Set the loaded data
        createData = Boolean.getBoolean(props.getProperty( "createData" ));
        aggregated = Boolean.getBoolean(props.getProperty( "aggregatedData" ));
        int threadAmount = Integer.parseInt(props.getProperty( "threadAmount" ));
        int sensorAmount = Integer.parseInt(props.getProperty( "sensorAmount" ));
        long stepDurationMs = Integer.parseInt(props.getProperty( "stepDurationMs" ));
        final File path = new File(props.getProperty( "pathAggregated" ));
        final File pathSensorData = new File(props.getProperty( "pathSensor" ));
        log.info( "Factor: {}", (int) (100 / stepDurationMs * 5) );

        //Set data for all files
        RandomData.setPeek((int)(100/stepDurationMs * 5));
        Analyser.setAmountSensors(sensorAmount);
        Analyser.setThreadAmount(threadAmount);
        Analyser.setFolder(path);
        AveragedData.setFolderData(pathSensorData);
        AveragedData.setFolderStore(path);
        ErrorCreator.setFolderData(pathSensorData);
        FinalData.setFolderStore(path);
        WindowedData.setFolderData(pathSensorData);
        WindowedData.setFolderStore(path);
        ThreadedSender.setPathNormal(path);
        Comparer.setFolder(path);
        DataGenerator.setFolderData(pathSensorData);
        DataGenerator.setFolderStore(path);
        SensorGenerator.setFolder(pathSensorData);
        JsonFileHandler.setFolderAggregated(path);
        JsonFileHandler.setFolderSensors(pathSensorData);



        //Start Creation of Data
        if(createData){
            //Delete all files in folder
            JsonFileHandler.deleteAllJsonFiles();

            //create files
            SensorGenerator.creator(sensorAmount);
            ErrorCreator.dataWithErrors(); //create some data loss and null entries.
            DataGenerator.dataGenerator();
            WindowedData.createWindowedData(); //creates warnings if some data is not in a wished range
            AveragedData.aggregatedData(stepDurationMs); //get average over a time interval
            FinalData.createFinalData();
        }

        //Start Sending to Stream processing System and start receiver
        Thread sendThread = new Thread(() -> {
            try {
                ThreadedSender.sendThreaded(aggregated, threadAmount, stepDurationMs);
            } catch (Exception e) {
                log.warn( e.getMessage() );
            }
        });

        Thread receiveThread = new Thread(() -> {
            try {
                DataReceiver.receive(aggregated);
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
}
