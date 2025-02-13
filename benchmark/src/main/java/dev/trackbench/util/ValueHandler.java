package dev.trackbench.util;

import dev.trackbench.simulation.processing.Value;
import java.util.function.Consumer;
import java.util.function.Function;

public record ValueHandler(Function<Value, Value> extractor, Function<Value, Value> setter) {

}
