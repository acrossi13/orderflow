package com.dev01.orderflow;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class DateTimeExamples {
    private DateTimeExamples() {}

    public static String nowBrazilFormatted() {
        var sp = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");
        return sp.format(fmt);
    }

    public static long measureMillis(Runnable task) {
        Instant start = Instant.now();
        task.run();
        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }
}