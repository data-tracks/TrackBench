package dev.kafka.serialize;

import dev.kafka.average.AverageSpeed;
import dev.kafka.util.SerdeUtil;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageSpeedSerde implements Serde<AverageSpeed> {

    @Override
    public Serializer<AverageSpeed> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageSpeed> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageSpeed> {

        @Override
        public byte[] serialize(String topic, AverageSpeed data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 2 + Integer.BYTES * 4);
            buffer.putDouble(data.speed);
            buffer.putDouble(data.wind);

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

    public static class AverageDeserializer implements Deserializer<AverageSpeed> {

        @Override
        public AverageSpeed deserialize(String topic, byte[] data) {
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
            long tick = buffer.getLong();
            return new AverageSpeed(speed, wind, count, tickS, tickE, id, tick);
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
