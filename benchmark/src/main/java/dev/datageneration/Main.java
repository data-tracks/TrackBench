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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

@Slf4j
public class Main {

    //change path to where settings.txt if stored
    static final String path = "src/main/resources";
    static final String SETTINGS_PATH = "settings.properties";
    static final String tire = "src/main/java/dev/datageneration/kafka/KafkaTools/Tire.java";
    public static void main(String[] args) throws Exception {
        //Get Data from Settings file
        Configurations configs = new Configurations();
        PropertiesConfiguration props = configs.properties( Main.class.getClassLoader().getResource( SETTINGS_PATH ) );


        //Set the loaded data
        boolean generate = props.getBoolean( "generate" );
        boolean execute = props.getBoolean( "execute" );
        boolean aggregated = props.getBoolean( "aggregatedData" );
        int threadAmount = props.getInt( "threadAmount" );
        int sensorAmount = props.getInt( "sensorAmount" );
        long ticks = Long.parseLong(props.getString( "ticks" ).replace( "_", "" ));
        long stepDurationMs = props.getInt( "stepDurationMs" );
        final File path = new File(props.getString( "pathAggregated" ));
        final File pathSensorData = new File(props.getString( "pathSensor" ));
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
        if(generate){
            //Delete all files in folder
            JsonFileHandler.deleteAllJsonFiles();

            //create files
            SensorGenerator.creator(ticks, sensorAmount);
            ErrorCreator.dataWithErrors(); //create some data loss and null entries.
            DataGenerator.dataGenerator();
            WindowedData.createWindowedData(); //creates warnings if some data is not in a wished range
            AveragedData.aggregatedData(stepDurationMs); //get average over a time interval
            FinalData.createFinalData();
        }

        if ( !execute ){
            return;
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
