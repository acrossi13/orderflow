package com.dev01.orderflow;

import java.time.*;

public final class TimeDiffExamples {
    private TimeDiffExamples() {}

    public static long measureMillis(Runnable task) {
        Instant start = Instant.now();
        task.run();
        return Duration.between(start, Instant.now()).toMillis();
    }

    public static int yearsOld(LocalDate birth) {
        return Period.between(birth, LocalDate.now()).getYears();
    }
}