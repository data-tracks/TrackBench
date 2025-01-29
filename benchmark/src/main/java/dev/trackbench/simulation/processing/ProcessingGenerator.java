package dev.trackbench.simulation.processing;

import dev.trackbench.BenchmarkContext;
import dev.trackbench.util.SimpleCountRegistry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ProcessingGenerator {

    static List<JSONObject> data = new ArrayList<>();  // Store JSONObjects instead of String arrays
    static List<JSONObject> windowedData = new ArrayList<>();  // Store JSONObjects instead of String arrays

    public static void process( BenchmarkContext context) {
        List<File> files = context.getConfig().getSensorFiles( context.getConfig().getDataWithErrorPath() );
        List<ProcessingHandler> processingHandlers = new ArrayList<>();

        SimpleCountRegistry registry = new SimpleCountRegistry( files.size(), " file(s)" );

        log.info( "found {}", files.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")));

        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                ProcessingHandler creator = new ProcessingHandler(context, file, registry);
                processingHandlers.add(creator);
            }
        }
        processingHandlers.forEach(Thread::start);

        try {
            for ( ProcessingHandler processingHandler : processingHandlers ) {
                processingHandler.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getWindowedData() {
        while (!data.isEmpty()) {
            JSONObject dataEntry = data.getFirst();
            JSONObject d = dataEntry.getJSONObject("data");
            getWarnings(d.getString("type"), dataEntry);
        }
    }

    private static void getWarnings(String type, JSONObject jsonObject) {
        String warning;
        if(jsonObject.getJSONObject("data").has("Error")) {
            windowedData.add(jsonObject);
            data.remove(jsonObject);
            return;
        }
        switch (type) {
            case "tire":
                if(jsonObject.getJSONObject("data").getInt("temperature tire") > 110) {
                    warning = "position:" + jsonObject.getJSONObject("data").getNumber("position").intValue() + " is to hot.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getInt("wear") > 90) {
                    warning = "position:" + jsonObject.getJSONObject("data").getNumber("position").intValue() + " is worn down.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getDouble("pressure psi") > 30) {
                    warning = "position:" + jsonObject.getJSONObject("data").getNumber("position").intValue() + " to high pressure.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            case "heat":
                if(jsonObject.getJSONObject("data").getNumber("temperature c").intValue() > 50) {
                    warning = " to hot temperature.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            case "engine":
                if(jsonObject.getJSONObject("data").getNumber("oil_pressure").doubleValue() > 7) {
                    warning = " oil pressure to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                if(jsonObject.getJSONObject("data").getNumber("temperature engine").intValue() > 600) {
                    warning = " is overheating.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getNumber("fuel_pressure").doubleValue() > 5) {
                    warning = " fuel pressure to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getNumber("rpm").longValue() > 18000) {
                    warning = " rpm to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                if(jsonObject.getJSONObject("data").getNumber("exhaust").doubleValue() > 1.2) {
                    warning = " exhaust fumes not good.";
                    createErrorObject(jsonObject, type, warning);
                }
                if(jsonObject.getJSONObject("data").getNumber("fuelFlow").intValue() > 120) {
                    warning = " fuelFlow to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            case "fuelPump":
                if(jsonObject.getJSONObject("data").getNumber("ml/min").longValue() > 4000) {
                    warning = " fuel flow is to low.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getNumber("temperature fuelP").intValue() > 60) {
                    warning = " fuel-pump temperature is to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            case "brake":
                if(jsonObject.getJSONObject("data").getNumber("temperature brake").intValue() > 1000) {
                    warning = " is overheating.";
                    createErrorObject(jsonObject, type, warning);
                }
                if(jsonObject.getJSONObject("data").getNumber("wear").intValue() > 90) {
                    warning = " is worn down.";
                    createErrorObject(jsonObject, type, warning);
                }
                if(jsonObject.getJSONObject("data").getNumber("brake_pressure").intValue() > 10) {
                    warning = " brake pressure to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            case "gForce":
                if(jsonObject.getJSONObject("data").getNumber("g-lateral").intValue() > 6) {
                    warning = " g-force lateral is high.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getNumber("g-longitudinal").intValue() > 5) {
                    warning = " g-force longitudinal is high.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;
            case "accelerometer":
                if(jsonObject.getJSONObject("data").getNumber("throttlepedall").intValue() > 100) {
                    warning = " throttlepedall is high.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            case "speed":
                if(jsonObject.getJSONObject("data").getNumber("kph").intValue() > 360) {
                    warning = " kph is high.";
                    createErrorObject(jsonObject, type, warning);
                }
                if (jsonObject.getJSONObject("data").getNumber("wind speed").intValue() > 200) {
                    warning = " wind speed is to high.";
                    createErrorObject(jsonObject, type, warning);
                }
                data.remove(jsonObject);
                break;

            default:
                data.remove(jsonObject);
                break;
        }
    }

    private static void createErrorObject(JSONObject jsonObject, String type, String warning) {
        JSONObject error = new JSONObject();
        error.put("data", jsonObject.getJSONObject("data"));
        error.put("WarningMessage", type + " id:" + jsonObject.getJSONObject("data").getNumber("id") + " " + warning);
        error.put("tick", jsonObject.getInt("tick"));
        windowedData.add(error);
    }
}
