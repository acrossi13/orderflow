package com.dev01.orderflow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CompletableFutureExamples {
    private CompletableFutureExamples() {}
    static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public static CompletableFuture<String> fetchCustomer(String code) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return "customer=" + code;
        });
    }

    public static CompletableFuture<String> fetchOrders(String code) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(300);
            return "orders=42";
        });
    }

    public static CompletableFuture<String> buildDashboard(String code) {
        var c = fetchCustomer(code);
        var o = fetchOrders(code);
        return c.thenCombine(o, (cc, oo) -> cc + " | " + oo);
    }
}
