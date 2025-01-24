package dev.datageneration.util;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Value(staticConstructor = "of")
public class Pair<Left, Right> {
    Left left;
    Right right;

    public static <Left, Right> List<Left> lefts(List<Pair<Left,Right>> pairs) {
        List<Left> lefts = new ArrayList<>();
        pairs.forEach(pair -> lefts.add(pair.left));
        return lefts;
    }

}
