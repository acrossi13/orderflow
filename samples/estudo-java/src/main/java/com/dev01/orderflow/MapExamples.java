package com.dev01.orderflow;

import java.util.*;

public final class MapExamples {
    private MapExamples() {}

    // Agrupa códigos por prefixo (antes do "_")
    public static Map<String, List<String>> groupByPrefix(List<String> codes) {
        Map<String, List<String>> out = new HashMap<>();

        for (String c : codes) {
            if (c == null || c.isBlank()) continue;

            String normalized = c.trim().toUpperCase();
            String prefix = normalized.contains("_") ? normalized.substring(0, normalized.indexOf('_')) : normalized;

            out.computeIfAbsent(prefix, k -> new ArrayList<>()).add(normalized);
        }
        return out;
    }

    // Conta quantas vezes cada status aparece
    public static Map<String, Integer> countStatuses(List<String> statuses) {
        Map<String, Integer> out = new HashMap<>();

        for (String s : statuses) {
            if (s == null || s.isBlank()) continue;
            String key = s.trim().toUpperCase();
            out.merge(key, 1, Integer::sum);
        }
        return out;
    }
}