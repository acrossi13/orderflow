package com.dev01.orderflow.orderapi.api.errors;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String from, String to) {
        super("Invalid status transition: " + from + " -> " + to);
    }
}