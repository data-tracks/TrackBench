package dev.trackbench.system.kafka.Serde;

import dev.trackbench.system.kafka.AverageClass.AverageTire;
import dev.trackbench.system.kafka.AverageClass.AverageTireGroup;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;


import java.nio.ByteBuffer;
import java.util.Map;

public class AverageTireGroupSerde implements Serde<AverageTireGroup> {

    @Override
    public Serializer<AverageTireGroup> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageTireGroup> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageTireGroup> {

        @Override
        public byte[] serialize(String topic, AverageTireGroup data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 6 + Integer.BYTES * 6);
            buffer.putDouble(data.temp);
            buffer.putDouble(data.pressure);
            buffer.putInt(data.count);
            buffer.putInt(data.tickStart);
            buffer.putInt(data.tickEnd);
            buffer.putInt(data.id);
            buffer.putInt(data.position);
            buffer.putInt(data.wear);
            buffer.putDouble(data.minTemp);
            buffer.putDouble(data.maxTemp);
            buffer.putDouble(data.minPressure);
            buffer.putDouble(data.maxPressure);
            return buffer.array();
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No configuration needed
        }

        @Override
        public void close() {
            // No resources to close
        }
    }

    public static class AverageDeserializer implements Deserializer<AverageTireGroup> {

        @Override
        public AverageTireGroup deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            double temp = buffer.getDouble();
            double pressure = buffer.getDouble();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            int position = buffer.getInt();
            int wear = buffer.getInt();
            double minTemp = buffer.getDouble();
            double maxTemp = buffer.getDouble();
            double minPressure = buffer.getDouble();
            double maxPressure = buffer.getDouble();
            return new AverageTireGroup(temp, pressure, count, tickS, tickE, id, position, wear);
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // No configuration needed
        }

        @Override
        public void close() {
            // No resources to close
        }
    }
}
