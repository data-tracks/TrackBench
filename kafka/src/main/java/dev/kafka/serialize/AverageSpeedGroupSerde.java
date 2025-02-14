package dev.kafka.serialize;

import dev.kafka.average.AverageSpeedGroup;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageSpeedGroupSerde implements Serde<AverageSpeedGroup> {

    @Override
    public Serializer<AverageSpeedGroup> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageSpeedGroup> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageSpeedGroup> {

        @Override
        public byte[] serialize(String topic, AverageSpeedGroup data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 6 + Integer.BYTES * 4);
            buffer.putDouble(data.speed);
            buffer.putDouble(data.wind);
            buffer.putInt(data.count);
            buffer.putInt(data.tickStart);
            buffer.putInt(data.tickEnd);
            buffer.putInt(data.id);
            buffer.putDouble(data.minSpeed);
            buffer.putDouble(data.maxSpeed);
            buffer.putDouble(data.minWind);
            buffer.putDouble(data.maxWind);
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

    public static class AverageDeserializer implements Deserializer<AverageSpeedGroup> {

        @Override
        public AverageSpeedGroup deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            double speed = buffer.getDouble();
            double wind = buffer.getDouble();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            double minSpeed = buffer.getDouble();
            double maxSpeed = buffer.getDouble();
            double minWind = buffer.getDouble();
            double maxWind = buffer.getDouble();
            return new AverageSpeedGroup(speed, wind, count, tickS, tickE, id);
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
