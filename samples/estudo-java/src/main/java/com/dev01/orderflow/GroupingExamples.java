package com.dev01.orderflow;

import java.util.*;
import java.util.stream.Collectors;

public final class GroupingExamples {
    private GroupingExamples() {}

    public static Map<String, List<String>> groupByPrefix(List<String> codes) {
        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(GroupingExamples::prefix));
    }

    private static String prefix(String code) {
        int idx = code.indexOf('_');
        return (idx >= 0) ? code.substring(0, idx) : code;
    }
}