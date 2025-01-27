package dev.datageneration.processing;

import com.fasterxml.jackson.databind.JsonNode;


public record Value(long tick, JsonNode node) {

}
