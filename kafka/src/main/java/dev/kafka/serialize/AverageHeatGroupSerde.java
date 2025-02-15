package dev.kafka.serialize;

import dev.kafka.average.AverageHeatGroup;
import dev.kafka.util.SerdeUtil;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageHeatGroupSerde implements Serde<AverageHeatGroup> {

    @Override
    public Serializer<AverageHeatGroup> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageHeatGroup> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageHeatGroup> {

        @Override
        public byte[] serialize(String topic, AverageHeatGroup data) {
            if (data == null) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate(20_000);
            buffer.putDouble(data.temp);
            buffer.putDouble(data.minTemp);
            buffer.putDouble(data.maxTemp);

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

    public static class AverageDeserializer implements Deserializer<AverageHeatGroup> {

        @Override
        public AverageHeatGroup deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            double temp = buffer.getDouble();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            double minTemp = buffer.getDouble();
            double maxTemp = buffer.getDouble();
            return new AverageHeatGroup(temp, count, tickS, tickE, id);
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
