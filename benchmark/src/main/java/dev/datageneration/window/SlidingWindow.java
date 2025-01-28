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


    long relevantTicks;

    List<Pair<Long, Aggregator>> aggregators = new ArrayList<>();


    public SlidingWindow(Supplier<Aggregator> aggregateCreator, long relevantTicks ) {
        this.aggregateCreator = aggregateCreator;
        this.relevantTicks = relevantTicks;
    }


    @Override
    public void next( Value value ) {
        Aggregator aggregator = aggregateCreator.get();

        Pair<Long, Aggregator> pair = new Pair<>( value.getTick(), aggregator );
        aggregators.add( pair );

        long lowestTick = value.getTick() - relevantTicks;

        Iterator<Pair<Long, Aggregator>> iter = aggregators.iterator();

        long count = 0;
        // remove the older values
        while ( iter.hasNext() ) {
            Pair<Long, Aggregator> current = iter.next();
            if ( current.left() < lowestTick ) {
                // to remove later
                count++;
            }else {
                // first usable value
                pair = current;
                pair.right().next(value.getNode());
                break;
            }
        }

        // update after values
        while ( iter.hasNext() ) {
            iter.next().right().next( value.getNode() );
        }

        // remove old
        for (long i = 0; i < count; i++) {
            aggregators.removeFirst();
        }


        toAllSteps( pair.right().get().stream().map( v -> new Value( value.getTick(), v ) ).toList() );

    }


}
