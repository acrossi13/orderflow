package com.dev01.orderflow.orderapi.api.errors;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(OrderNotFoundException ex) {
        var pd = ProblemDetail.forStatus(404);
        pd.setTitle("Not Found");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://httpstatuses.com/404"));
        return ResponseEntity.status(404).body(pd);
    }
}