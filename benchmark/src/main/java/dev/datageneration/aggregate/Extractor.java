package dev.datageneration.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datageneration.processing.Step;
import java.util.List;

public abstract class Extractor extends Step{

    public Extractor( List<Step> steps ) {
        super( steps );
    }

}
