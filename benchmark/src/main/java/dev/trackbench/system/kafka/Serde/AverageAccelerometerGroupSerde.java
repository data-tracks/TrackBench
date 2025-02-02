package dev.trackbench.system.kafka.Serde;

import dev.trackbench.system.kafka.AverageClass.AverageAccelerometerGroup;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageAccelerometerGroupSerde implements Serde<AverageAccelerometerGroup> {

    @Override
    public Serializer<AverageAccelerometerGroup> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageAccelerometerGroup> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageAccelerometerGroup> {

        @Override
        public byte[] serialize(String topic, AverageAccelerometerGroup data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 3 + Integer.BYTES * 4);
            buffer.putDouble(data.throttle);
            buffer.putInt(data.count);
            buffer.putInt(data.tickStart);
            buffer.putInt(data.tickEnd);
            buffer.putInt(data.id);
            buffer.putDouble(data.maxThrottle);
            buffer.putDouble(data.minThrottle);
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

    public static class AverageDeserializer implements Deserializer<AverageAccelerometerGroup> {

        @Override
        public AverageAccelerometerGroup deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            double throttle = buffer.getDouble();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            double minThrottle = buffer.getDouble();
            double maxThrottle = buffer.getDouble();
            return new AverageAccelerometerGroup(throttle, count, tickS, tickE, id);
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
