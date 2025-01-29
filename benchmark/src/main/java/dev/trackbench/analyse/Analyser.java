package dev.trackbench.analyse;

import dev.trackbench.BenchmarkContext;
import dev.trackbench.jsonHandler.JsonFileHandler;
import dev.trackbench.util.Pair;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static dev.trackbench.analyse.Comparer.*;
import static dev.trackbench.jsonHandler.JsonFileHandler.writeFile;
import static dev.trackbench.jsonHandler.JsonFileHandler.writeJsonFile;

@Slf4j
public class Analyser {
    static List<JSONObject> dataSent = new LinkedList<>();
    static List<JSONObject> averagedData = new LinkedList<>();
    @Setter
    static File folder;
    static File folderResults = new File( "src/main/resources");
    static final String file1 = "ALL_DATA.json";
    static final String file2 = "averagedData.json";//"finalData.json";
    static int amountErrors = 0;
    static int amountWarnings;
    static int amountPeeks;
    @Setter
    static int threadAmount;
    @Setter
    static int amountSensors;
    static String message;
    static double percentageCorrectErrors;
    static double percentageCorrectWarnings;
    static double percentageCorrectWindows;

    public static void analyser(boolean aggregated, List<JSONObject> dataReceived, long startTime,
                                long lastReceivedTime, int throughput) throws IOException {
        if (aggregated) { //aggregated Data
            JsonFileHandler.readJsonFile(folder, file2, averagedData);
            log.info("Aggregated data received");


            List<JSONObject> onlyWindows = getOnlyWindows(dataReceived);

            dataReceived.removeAll(onlyWindows);

            List<JSONObject> onlyMaxWindows = getOnlyMaxWindows(onlyWindows);

            writeJsonFile(folder, "maxWindowsFromSystem", onlyMaxWindows);
            writeJsonFile(folder, "windowsFomSystem", onlyWindows);
            writeJsonFile(folder, "error_warnings_system", dataReceived);

            //send data to the comparer to validate the peeks
            String message = comparing(onlyMaxWindows, dataReceived, aggregated);

            List<String> peekStore = Comparer.getPeeksStore();
            StringBuilder peekAnalyse = new StringBuilder();
            for (String s : peekStore) {
                String[] info = s.split(":");
                for (int j = 0; j < info.length; j++) {
                    peekAnalyse.append(info[j]).append(" ");
                    if(j == 2 && info[0].equals("received")) {
                        amountPeeks += Integer.parseInt(info[j]);
                    }
                }
                peekAnalyse.append("\n");
            }
            log.info(peekAnalyse.toString());

            //Get Completeness of the peeks in the windows
            int amountWrongPeeks = getFalseWindow();
            int amountRightPeeks = getRightWindow();
            percentageCorrectWindows = 100 / (double) amountPeeks * amountRightPeeks;

            analyseErrorAndWarnings(dataReceived);

            //Get completeness of the errors and warnings
            int errorMissing = getErrors().size();
            int warningMissing = getWarnings().size();
            percentageCorrectErrors =  100 - (100 / (double) amountErrors * errorMissing);
            percentageCorrectWarnings = 100 - (100 / (double) amountWarnings * warningMissing);

        } else {
            JsonFileHandler.readJsonFile(folder, file1, dataSent);
            log.info("Normal data received");
        }

        // End time
        long elapsedTime = lastReceivedTime - startTime;
        double elapsedTimeInSeconds = elapsedTime / 1000.0;
        String time = String.format("%.5f", elapsedTimeInSeconds);
        double timeInSeconds = Double.parseDouble(time);

        // Get the Current Date and Time
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(currentDate);

        // Information after Receiving Data
        String output =
                "\n_________________________________________________________________________________________________\n" +
                formattedDate +
                "\nData received in " + time + " seconds" +
                "\nAmount Warnings in data: " + amountWarnings +
                "\nAmount Errors in data: " + amountErrors +
                "\nAmount of data received: " + throughput +
                "\nAmount of Sensors used: " + amountSensors +
                "\nAmount of Threads used: " + threadAmount +
                "\n\n" + message +
                "\n.................................................." +
                "\nPerformance metrics:"+
                "\nData latency: " + String.format("%.8f", (timeInSeconds / throughput)) + " seconds for one datapoint" +
                "\nData throughput: " + String.format("%.3f", (throughput / elapsedTimeInSeconds)) + " per second" +
                "\nThread efficiency: " + String.format("%.3f", (double) amountSensors / threadAmount) +
                "\n.................................................." +
                "\nData quality metrics:" +
                "\nPeak completeness: " + percentageCorrectWindows +
                "\nWarning completeness: " + percentageCorrectWarnings +
                "\nError completeness: " + percentageCorrectErrors +
                "\n_________________________________________________________________________________________________";
        log.info(output);

        writeFile(folderResults, "analysis", output);

    }

    private static void analyseErrorAndWarnings(List<JSONObject> receivedData) {
        for (JSONObject json : receivedData) {
            if (json.has("WarningMessage")) {
                amountWarnings++;
            }
            if (json.getJSONObject("data").has("Error")) {
                amountErrors++;
            }
        }
    }


    private static List<JSONObject> getOnlyMaxWindows(List<JSONObject> dataReceived) {
        dataReceived.sort(Comparator.comparingInt((JSONObject obj) -> obj.getJSONObject("data").getInt("id"))
                .thenComparingInt(obj -> obj.getInt("startTime")));

        List<JSONObject> onlyMaxWindows = new LinkedList<>();
        int id;
        int start;
        JSONObject json;
        for(int i = 0; i < dataReceived.size(); i++) {
//            log.info(dataReceived.get(i).getJSONObject("data").getInt("id") + " " + dataReceived.get(i).getInt("startTime") + " " + dataReceived.get(i).getInt("endTime"));
            if(i == dataReceived.size() - 1) {
                onlyMaxWindows.add(dataReceived.get(i));
                break;
            }
            id = dataReceived.get(i).getJSONObject("data").getInt("id");
            start = dataReceived.get(i).getInt("startTime");
            json = dataReceived.get(i+1);
            if(json.getInt("startTime") != start && id >= 0) {
                onlyMaxWindows.add(dataReceived.get(i));
            }
        }
        onlyMaxWindows.sort(Comparator.comparingInt((JSONObject obj) -> obj.getInt("startTime")));
        return onlyMaxWindows;
    }

    private static List<JSONObject> getOnlyWindows(List<JSONObject> dataReceived) {
        List<JSONObject> onlyWindows = new LinkedList<>();
        for(JSONObject data : dataReceived) {
            if(data.has("startTime")) {
                onlyWindows.add(data);
            }
        }
        return onlyWindows;
    }

    public static void start(BenchmarkContext context) {
        DelayCollector delayCollector = new DelayCollector(context);
        for (Pair<String, String> pair : delayCollector.start()) {
            log.info("{}: {}", pair.left(), pair.right());
        }
    }
}
