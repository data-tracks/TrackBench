package dev.kafka.serialize;

import dev.kafka.average.AverageAccelerometer;
import dev.kafka.util.SerdeUtil;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageAccelerometerSerde implements Serde<AverageAccelerometer> {

    @Override
    public Serializer<AverageAccelerometer> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageAccelerometer> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageAccelerometer> {

        @Override
        public byte[] serialize(String topic, AverageAccelerometer data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES + Integer.BYTES * 4);
            buffer.putDouble(data.throttle);

            SerdeUtil.addDefault( buffer, data );
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

    public static class AverageDeserializer implements Deserializer<AverageAccelerometer> {

        @Override
        public AverageAccelerometer deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            double throttle = buffer.getDouble();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            long tick = buffer.getLong();
            return new AverageAccelerometer(throttle, count, tickS, tickE, id, tick);
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
