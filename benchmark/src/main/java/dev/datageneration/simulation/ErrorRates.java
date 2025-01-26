package dev.datageneration.simulation;

import lombok.Value;

@Value(staticConstructor = "of")
public class ErrorRates {

    double noDataRate;
    double dropRate;
}
