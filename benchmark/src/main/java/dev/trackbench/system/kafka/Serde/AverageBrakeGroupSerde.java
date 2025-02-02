package dev.trackbench.system.kafka.Serde;

import dev.trackbench.system.kafka.AverageClass.AverageBrake;
import dev.trackbench.system.kafka.AverageClass.AverageBrakeGroup;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageBrakeGroupSerde implements Serde<AverageBrakeGroup> {

    @Override
    public Serializer<AverageBrakeGroup> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageBrakeGroup> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageBrakeGroup> {

        @Override
        public byte[] serialize(String topic, AverageBrakeGroup data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( Integer.BYTES * 11);
            buffer.putInt(data.temp);
            buffer.putInt(data.pressure);
            buffer.putInt(data.count);
            buffer.putInt(data.tickStart);
            buffer.putInt(data.tickEnd);
            buffer.putInt(data.id);
            buffer.putInt(data.wear);
            buffer.putInt(data.minTemp);
            buffer.putInt(data.maxTemp);
            buffer.putInt(data.minPressure);
            buffer.putInt(data.maxPressure);
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

    public static class AverageDeserializer implements Deserializer<AverageBrakeGroup> {

        @Override
        public AverageBrakeGroup deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int temp = buffer.getInt();
            int pressure = buffer.getInt();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            int wear = buffer.getInt();
            return new AverageBrakeGroup(temp, pressure, count, tickS, tickE, id, wear);
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
