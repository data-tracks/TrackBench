package dev.datageneration.aggregate;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import java.util.List;

public class SingleExtractor extends Extractor {

    private final String path;


    public SingleExtractor( List<Step> steps, String path ) {
        super( steps );
        String adjustedPath = path.replace( ".", "/" );

        this.path = adjustedPath.startsWith( "/" ) ? adjustedPath : "/" + adjustedPath;
    }



    @Override
    public void next( List<Value> values ) {
        toAllSteps( values.stream().map( v -> new Value( v.tick(), v.node().at( path ) ) ).toList() );
    }

}
