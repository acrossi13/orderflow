package com.dev01.orderflow;

import java.util.*;
import java.util.stream.Collectors;

public final class FlatMapExamples {
    private FlatMapExamples() {}

    public static List<String> flatten(List<List<String>> lists) {
        return lists.stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)     // achata
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList()); // Java 8
    }
}