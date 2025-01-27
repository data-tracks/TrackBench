package dev.datageneration.util;

import java.util.ArrayList;
import java.util.List;

public record Pair<Left, Right>(Left left, Right right) {

    public static <Left, Right> List<Left> lefts( List<Pair<Left, Right>> pairs ) {
        List<Left> lefts = new ArrayList<>();
        pairs.forEach( pair -> lefts.add( pair.left ) );
        return lefts;
    }

}
