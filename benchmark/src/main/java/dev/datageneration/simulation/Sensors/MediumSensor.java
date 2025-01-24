package dev.datageneration.simulation.Sensors;

import dev.datageneration.simulation.types.DataType;
import org.json.JSONObject;


public class MediumSensor extends Sensor {

    String di1;
    String di2;
    String di3;
    DataType dType1;
    DataType dType2;
    DataType dType3;


    public MediumSensor(String type, int id, String[] dataInfo) {
        super(type, id);
        this.di1 = dataInfo[0];
        this.di2 = dataInfo[1];
        this.di3 = dataInfo[2];
        this.dType1 = dataTypes.get(di1);
        this.dType2 = dataTypes.get(di2);
        this.dType3 = dataTypes.get(di3);
    }

    @Override
    public String[] getHeader() {
        return new String[] {di1, di2, di3};
    }

    @Override
    public void attachDataPoint(JSONObject target) { //TODO: change to add json here.
        String[] data =new String[] {dType1.sample(di1), dType2.sample(di2), dType3.sample(di3)};

        //create JSON object
        String[] header = getHeader();

        int f = getTickValue();
        if(counter == f) {
            tick ++;
            counter = 0;
        }
        JSONObject sensorDataObject = new JSONObject();
        for (int j = 0; j < header.length; j++) {
            sensorDataObject.put(header[j], Double.valueOf(data[j])); // Add each sensor data point to JSON object
        }
        sensorDataObject.put("id", id);
        sensorDataObject.put("type", getType());
        // Wrap each JSON object with a number prefix
        JSONObject freqObject = new JSONObject();
        freqObject.put("data", sensorDataObject);
        freqObject.put("tick", tick);
        counter ++;

        // Add the prefixed object to the JSON array
        dataPoints.put(freqObject);
    }
}
