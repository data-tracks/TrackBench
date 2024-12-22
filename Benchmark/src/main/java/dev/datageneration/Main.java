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
import java.io.FileReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {

    //change path to where settings.txt if stored
    static final String path = "src/main/resources";
    static final File file = new File(path + "/settings.txt");
    static boolean aggregated;
    public static void main(String[] args) throws Exception {

        //Get Data from Settings file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int counter = 0;
        int amount = 0;
        String[] input = new String[7];
        while ((st = br.readLine()) != null){
            if(counter >= 3 && counter <= 7) {
                input[amount] = st.split(":")[1];
                System.out.println(input[amount]);
                amount++;
            }
            if(counter == 9 || counter == 10) {
                input[amount] = st.split(":")[1];
                System.out.println(input[amount]);
                amount++;
            }
            counter++;
        }
        //Set the loaded data
        if(input[0].equals("true")){
            aggregated = true;
        } else {
            aggregated = false;
        }
        int threadAmount = Integer.parseInt(input[1]);
        int[] sensorArray = new int[Integer.parseInt(input[2])];
        Arrays.fill(sensorArray, Integer.parseInt(input[3]));
        long durationTimeStep = Integer.parseInt(input[4]); //milliseconds
        final File folderStorage = new File(input[5]);
        final File folderSensorData = new File(input[6]);
        System.out.println("Factor: " + (int)(100/durationTimeStep * 5));

        //Set data for all files
        RandomData.setPeek((int)(100/durationTimeStep * 5));
        DataReceiver.setAmountSensors(sensorArray.length);
        Analyser.setFolder(folderStorage);
        AveragedData.setFolderData(folderSensorData);
        AveragedData.setFolderStore(folderStorage);
        ErrorCreator.setFolderData(folderSensorData);
        FinalData.setFolderStore(folderStorage);
        WindowedData.setFolderData(folderSensorData);
        WindowedData.setFolderStore(folderStorage);
        ThreadedSender.setPathNormal(folderStorage);
        Comparer.setFolder(folderStorage);
        DataGenerator.setFolderData(folderSensorData);
        DataGenerator.setFolderStore(folderStorage);
        SensorGenerator.setFolder(folderSensorData);
        JsonFileHandler.setFolderAggregated(folderStorage);
        JsonFileHandler.setFolderSensors(folderSensorData);

        //Delete all files in folder
        JsonFileHandler.deleteAllJsonFiles();

        //Start Creation of Data
        SensorGenerator.creator(sensorArray);
        ErrorCreator.dataWithErrors(); //create some data loss and null entries.
        DataGenerator.dataGenerator();
        WindowedData.createWindowedData(); //creates warnings if some data is not in a wished range
        AveragedData.aggregatedData(durationTimeStep); //get average over a time interval
        FinalData.createFinalData();


        //Start Sending to Stream processing System and start receiver
        Thread sendThread = new Thread(() -> {
            try {
                ThreadedSender.sendThreaded(aggregated, threadAmount, durationTimeStep);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread receiveThread = new Thread(() -> {
            try {
                DataReceiver.receive(aggregated);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();
        TimeUnit.SECONDS.sleep(1);
        sendThread.start();


        sendThread.join();
        receiveThread.join();
        System.out.println("Finished everything");
    }
}
