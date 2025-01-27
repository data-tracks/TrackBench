package dev.datageneration.window;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datageneration.aggregate.Aggregator;
import dev.datageneration.aggregate.Extractor;
import dev.datageneration.processing.Step;
import dev.datageneration.processing.Value;
import dev.datageneration.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class SlidingWindow extends Window {

    Supplier<Aggregator> aggregateCreator;

    Extractor extractor;

    long relevantTicks;

    List<Pair<Long, Aggregator>> aggregators = new ArrayList<>();


    public SlidingWindow( List<Step> steps, Supplier<Aggregator> aggregateCreator, Extractor extractor, long relevantTicks ) {
        super(steps);
        this.aggregateCreator = aggregateCreator;
        this.relevantTicks = relevantTicks;
        this.extractor = extractor;
    }


    @Override
    public void next( Value value ) {
        Aggregator aggregator = aggregateCreator.get();
        JsonNode target = null;//extractor.extract( value.node() );

        // add aggregator for current
        aggregator.next( target );
        Pair<Long, Aggregator> pair = new Pair<>( value.tick(), aggregator );
        aggregators.add( pair );

        long lowestTick = value.tick() - relevantTicks;

        Iterator<Pair<Long, Aggregator>> iter = aggregators.iterator();

        // remove the older values
        while ( iter.hasNext() ) {
            Pair<Long, Aggregator> current = iter.next();
            if ( current.left() < lowestTick ) {
                aggregators.removeFirst();
            }else {
                // first usable value
                pair = iter.next();
                break;
            }
        }

        // update after values
        while ( iter.hasNext() ) {
            iter.next().right().next( target );
        }
        toAllSteps( pair.right().reset().stream().map( v -> new Value( value.tick(), v ) ).toList() );

    }


}
