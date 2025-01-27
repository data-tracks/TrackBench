package dev.datageneration.simulation;

import lombok.Value;
import org.json.JSONObject;

@Value(staticConstructor = "of")
public class ErrorRates {

    double noDataRate;
    double dropRate;


    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("noDataRate", noDataRate);
        obj.put("dropRate", dropRate);
        return obj;
    }

}
