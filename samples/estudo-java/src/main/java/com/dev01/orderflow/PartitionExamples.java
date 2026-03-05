package com.dev01.orderflow;

import java.util.*;
import java.util.stream.Collectors;

public final class PartitionExamples {
    private PartitionExamples() {}

    public static Map<Boolean, List<String>> partitionValidCodes(List<String> items) {
        return items.stream()
                .map(s -> s == null ? null : s.trim().toUpperCase())
                .collect(Collectors.partitioningBy(PartitionExamples::isValidCode));
    }

    private static boolean isValidCode(String s) {
        return s != null && !s.isBlank() && s.matches("[A-Z0-9_]{3,20}");
    }
}