package dev.kafka.serialize;

import dev.kafka.average.AverageFuelPumpGroup;
import dev.kafka.util.SerdeUtil;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageFuelPumpGroupSerde implements Serde<AverageFuelPumpGroup> {

    @Override
    public Serializer<AverageFuelPumpGroup> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageFuelPumpGroup> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageFuelPumpGroup> {

        @Override
        public byte[] serialize( String topic, AverageFuelPumpGroup data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( Double.BYTES * 6 + Integer.BYTES * 4 );
            buffer.putDouble( data.temp );
            buffer.putDouble( data.flowRate );
            buffer.putDouble( data.maxTemp );
            buffer.putDouble( data.minTemp );
            buffer.putDouble( data.maxFlow );
            buffer.putDouble( data.minFlow );

            SerdeUtil.addDefault( buffer, data );
            return buffer.array();
        }


        @Override
        public void configure( Map<String, ?> configs, boolean isKey ) {
            // No configuration needed
        }


        @Override
        public void close() {
            // No resources to close
        }

    }


    public static class AverageDeserializer implements Deserializer<AverageFuelPumpGroup> {

        @Override
        public AverageFuelPumpGroup deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            double temp = buffer.getDouble();
            double flowRate = buffer.getDouble();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            double maxTemp = buffer.getDouble();
            double minTemp = buffer.getDouble();
            double maxFlow = buffer.getDouble();
            double minFlow = buffer.getDouble();
            long tick = buffer.getLong();
            return new AverageFuelPumpGroup( temp, flowRate, count, tickS, tickE, id, tick );
        }


        @Override
        public void configure( Map<String, ?> configs, boolean isKey ) {
            // No configuration needed
        }


        @Override
        public void close() {
            // No resources to close
        }

    }

}
