package dev.trackbench.simulation.aggregate;

import dev.trackbench.simulation.processing.Value;
import java.util.List;

public class SingleExtractor extends Extractor {

    private final String path;


    public SingleExtractor( String path ) {
        String adjustedPath = path.replace( ".", "/" );

        this.path = adjustedPath.startsWith( "/" ) ? adjustedPath : "/" + adjustedPath;
    }



    @Override
    public void next( List<Value> values ) {
        toAllSteps( values.stream().map( v -> new Value( v.getId(), v.getTick(), v.getNode().at( path ) ) ).toList() );
    }

}
