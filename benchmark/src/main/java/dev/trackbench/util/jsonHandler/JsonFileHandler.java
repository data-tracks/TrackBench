package dev.trackbench.util.jsonHandler;

import dev.trackbench.display.Display;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.Objects;

@Slf4j
public class JsonFileHandler {

    public static void readJsonFile(File folder, String fileName, List<JSONObject> allData) throws IOException {
        FileReader fReader = new FileReader(new File(folder, fileName));
        BufferedReader bReader = new BufferedReader(fReader);

        // Read the entire content of the JSON file
        StringBuilder jsonContent = new StringBuilder();
        String line;
        while ((line = bReader.readLine()) != null) {
            jsonContent.append(line);
        }
        bReader.close();

        // Parse the JSON content
        JSONArray jsonArray = new JSONArray(jsonContent.toString());

        // Process each entry in the JSON array
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            allData.add(jsonObject);  // Store each JSONObject in the allData list
        }
    }

    public static void writeJsonFile(File folder, String fName, List<JSONObject> allData) throws IOException {
        File outputFile = new File(folder + "/" + fName + ".json");

        try (FileWriter outputfile = new FileWriter(outputFile)) {
            JSONArray outputArray = new JSONArray(allData);  // Convert the list of JSONObjects back to JSONArray
            outputfile.write(outputArray.toString(4));  // Indented output for readability
            Display.INSTANCE.info("Data successfully written to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing the output file: " + e.getMessage());
            throw e;
        }
    }

    public static void writeFile(File folder, String fName, String data) throws IOException {
        boolean append = false;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.getName().equals(fName + ".txt")) {
                append = true;
                break;
            }
        }
        File outputFile = new File(folder + "/" + fName + ".txt");

        try (FileWriter outputfile = new FileWriter(outputFile, append)) {
            outputfile.write(data);  // Indented output for readability
            Display.INSTANCE.info("File successfully written to: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing the output file: " + e.getMessage());
            throw e;
        }
    }

}
