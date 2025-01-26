package dev.datageneration.util;

import java.io.IOException;
import org.json.JSONObject;

public interface JsonTarget {

    void attach( JSONObject freqObject ) throws IOException;

    void close() throws IOException;

}
