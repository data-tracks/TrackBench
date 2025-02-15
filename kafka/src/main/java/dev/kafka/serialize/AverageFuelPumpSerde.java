package dev.kafka.serialize;

import dev.kafka.average.AverageFuelPump;
import dev.kafka.util.SerdeUtil;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageFuelPumpSerde implements Serde<AverageFuelPump> {

    @Override
    public Serializer<AverageFuelPump> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageFuelPump> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageFuelPump> {

        @Override
        public byte[] serialize( String topic, AverageFuelPump data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( Double.BYTES * 2 + Integer.BYTES * 4 );
            buffer.putDouble( data.temp );
            buffer.putDouble( data.flowRate );

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


    public static class AverageDeserializer implements Deserializer<AverageFuelPump> {

        @Override
        public AverageFuelPump deserialize( String topic, byte[] data ) {
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
            long tick = buffer.getLong();
            return new AverageFuelPump( temp, flowRate, count, tickS, tickE, id, tick );
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
