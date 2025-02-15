package dev.kafka.util;

import dev.kafka.average.Average;
import java.nio.ByteBuffer;

public class SerdeUtil {

    public static void addDefault( ByteBuffer buffer, Average average ) {
        buffer.putLong( average.count );
        buffer.putLong( average.tickStart );
        buffer.putLong( average.tickEnd );
        buffer.putLong( average.id );
        buffer.putLong( average.tick );
    }

}
