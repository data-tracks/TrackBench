package dev.kafka.serialize;

import dev.kafka.average.AverageEngineGroup;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class AverageEngineGroupSerde implements Serde<AverageEngineGroup> {

    @Override
    public Serializer<AverageEngineGroup> serializer() {
        return new AverageSerializer();
    }

    @Override
    public Deserializer<AverageEngineGroup> deserializer() {
        return new AverageDeserializer();
    }

    public static class AverageSerializer implements Serializer<AverageEngineGroup> {

        @Override
        public byte[] serialize(String topic, AverageEngineGroup data) {
            if (data == null) {
                return null;
            }
            try {
                // Allocate a ByteBuffer of the correct size
                ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * 9 + Integer.BYTES * 6 + Long.BYTES * 3);
                buffer.putInt(data.temp);
                buffer.putLong(data.rpm);
                buffer.putInt(data.fuelFlow);
                buffer.putDouble(data.oilPressure);
                buffer.putDouble(data.fuelPressure);
                buffer.putDouble(data.exhaust);
                buffer.putInt(data.count);
                buffer.putInt(data.tickStart);
                buffer.putInt(data.tickEnd);
                buffer.putInt(data.id);
                buffer.putDouble(data.maxExhaust);
                buffer.putDouble(data.minExhaust);
                buffer.putDouble(data.maxFuelP);
                buffer.putDouble(data.minFuelP);
                buffer.putDouble(data.maxOilP);
                buffer.putDouble(data.minOilP);
                buffer.putLong(data.maxRPM);
                buffer.putLong(data.minRPM);
                return buffer.array();
            } catch (Exception e) {
                System.err.println("Serialization error: " + e.getMessage());
                return null;
            }
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

    public static class AverageDeserializer implements Deserializer<AverageEngineGroup> {

        @Override
        public AverageEngineGroup deserialize(String topic, byte[] data) {
            if (data == null || data.length != (Double.BYTES * 3 + Integer.BYTES * 6 + Long.BYTES)) {
                System.err.println("Deserialization error: Invalid byte array length");
                return null; // Handle null or malformed input
            }
            try {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                int temp = buffer.getInt();
                long rpm = buffer.getLong();
                int fuelFlow = buffer.getInt();
                double oilPressure = buffer.getDouble();
                double fuelPressure = buffer.getDouble();
                double exhaust = buffer.getDouble();
                int count = buffer.getInt();
                int tickStart = buffer.getInt();
                int tickEnd = buffer.getInt();
                int id = buffer.getInt();
                return new AverageEngineGroup(temp, rpm, fuelFlow, oilPressure, fuelPressure, exhaust, count, tickStart, tickEnd, id);
            } catch (Exception e) {
                System.err.println("Deserialization error: " + e.getMessage());
                return null; // or handle the error in another way
            }
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
