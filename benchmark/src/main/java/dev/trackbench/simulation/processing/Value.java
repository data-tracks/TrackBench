package dev.trackbench.simulation.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;


@Getter
@Setter
@Slf4j
public final class Value {
    JsonNode node;
    long tick;

    public Value(long tick, JsonNode node) {
        this.tick = tick;
        this.node = node;
    }

    public List<Value> ensureDouble() {
        if (this.node.isDouble()) {
            return List.of(this);
        }
        try {
            double value = Double.parseDouble(this.node.asText());
            this.node = new DoubleNode(value);
            return List.of(this);
        } catch (Exception e) {
            //log.warn("Inconsistent value {}", this.node);
            return List.of();
        }
    }

    public List<Value> ensureInt() {
        if (this.node.isInt()) {
            return List.of(this);
        }
        try {
            int value = Integer.parseInt(this.node.asText());
            this.node = new IntNode(value);
            return List.of(this);
        } catch (Exception e) {
            //log.warn("Inconsistent value {}", this.node);
            return List.of();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Value) obj;
        return this.tick == that.tick &&
                Objects.equals(this.node, that.node);
    }


}
