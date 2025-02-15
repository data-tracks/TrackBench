package dev.kafka.serialize;

import dev.kafka.average.AverageBrake;
import dev.kafka.util.SerdeUtil;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageBrakeSerde implements Serde<AverageBrake> {

    @Override
    public Serializer<AverageBrake> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageBrake> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageBrake> {

        @Override
        public byte[] serialize( String topic, AverageBrake data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( Integer.BYTES * 7 );
            buffer.putInt( data.temp );
            buffer.putInt( data.pressure );
            buffer.putLong( data.count );
            buffer.putInt( data.wear );

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


    public static class AverageDeserializer implements Deserializer<AverageBrake> {

        @Override
        public AverageBrake deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            int temp = buffer.getInt();
            int pressure = buffer.getInt();
            int count = buffer.getInt();
            int tickS = buffer.getInt();
            int tickE = buffer.getInt();
            int id = buffer.getInt();
            int wear = buffer.getInt();
            long tick = buffer.getLong();
            return new AverageBrake( temp, pressure, count, tickS, tickE, id, wear, tick );
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
