package dev.kafka.serialize;

import dev.kafka.average.AverageHeat;
import dev.kafka.util.SerdeUtil;
import dev.kafka.util.SerdeUtil.SerdeValues;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class AverageHeatSerde implements Serde<AverageHeat> {

    @Override
    public Serializer<AverageHeat> serializer() {
        return new AverageSerializer();
    }


    @Override
    public Deserializer<AverageHeat> deserializer() {
        return new AverageDeserializer();
    }


    public static class AverageSerializer implements Serializer<AverageHeat> {

        @Override
        public byte[] serialize( String topic, AverageHeat data ) {
            if ( data == null ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.allocate( 20_000 );
            SerdeUtil.addDefault( buffer, data );
            buffer.putDouble( data.temp );

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


    public static class AverageDeserializer implements Deserializer<AverageHeat> {

        @Override
        public AverageHeat deserialize( String topic, byte[] data ) {
            if ( data == null || data.length == 0 ) {
                return null;
            }
            ByteBuffer buffer = ByteBuffer.wrap( data );
            SerdeValues values = SerdeUtil.readDefault( buffer );
            double temp = buffer.getDouble();
            return new AverageHeat( temp, values );
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
