package com.dev01.orderflow;

import java.util.Arrays;
import java.util.List;

public class PeekDemo {
    public static void main(String[] args) {
        List<String> items = Arrays.asList(" dev_01 ", "x", " DEV_02_OK ", null, "  ", "ABC");

        var out = items.stream()
                .peek(s -> System.out.println("RAW: " + s))
                .filter(s -> s != null)
                .peek(s -> System.out.println("NOT NULL: " + s))
                .map(String::trim)
                .peek(s -> System.out.println("TRIM: " + s))
                .filter(s -> !s.isEmpty())
                .peek(s -> System.out.println("NOT EMPTY: " + s))
                .map(String::toUpperCase)
                .peek(s -> System.out.println("UPPER: " + s))
                .filter(s -> s.startsWith("DEV"))
                .peek(s -> System.out.println("DEV ONLY: " + s))
                .toList(); // se estiver em Java 8, use collect(toList())

        System.out.println("RESULT: " + out);
    }
}