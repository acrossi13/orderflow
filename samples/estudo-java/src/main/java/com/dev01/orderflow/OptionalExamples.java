package com.dev01.orderflow;

import java.util.Optional;

public final class OptionalExamples {
    private OptionalExamples() {}

    public static String normalizeToken(String token) {
        return Optional.ofNullable(token)
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .orElse("default");
    }
}