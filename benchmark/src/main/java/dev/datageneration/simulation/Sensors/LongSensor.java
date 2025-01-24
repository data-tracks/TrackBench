package dev.datageneration.simulation.Sensors;

import dev.datageneration.simulation.types.DataType;
import dev.datageneration.util.Pair;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
public class LongSensor extends Sensor {

    List<Pair<String, DataType>> nameType;


    public LongSensor(String type, int id, String[] dataInfo) {
        super(type, id);

        List<Pair<String, DataType>> nameType = new ArrayList<>();

        for (String info : dataInfo) {
            nameType.add(new Pair<>(info, dataTypes.get(info)));
        }

        this.nameType = List.copyOf(nameType);
    }

    @Override
    public List<String> getHeader() {
        return Pair.lefts(nameType);
    }

    @Override
    public void attachDataPoint(JSONObject target) {
        //create JSON object
        for (String element : getHeader()) {
            target.put(element, Double.valueOf(dataTypes.get(element).sample(element)));
        }
    }
}
