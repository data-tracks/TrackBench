package dev.trackbench.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;

public interface JsonTarget {

    void attach( JsonNode value );

    default void attach( List<JsonNode> values ) {
        for ( JsonNode value : values ) {
            attach( value );
        }
    }

    void close() throws IOException;

}
