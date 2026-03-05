package com.dev01.orderflow;

import java.util.List;

public class StreamExamples {

    /*
    Stream é um pipeline de processamento de dados:
	•	source (de onde vem): list.stream()
	•	operações intermediárias (transformam/filtram): filter, map, sorted
	•	operação terminal (executa e gera resultado): toList, collect, forEach, count

Importante: stream é lazy. Nada roda até você chamar a operação terminal.
     */
    private StreamExamples() {
    }

    public static List<String> devCodesUpper(List<String> items) {
        return items.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .filter(s -> s.startsWith("DEV"))
                .map(String::toUpperCase)
                .toList(); //java 8 funciona com .collect(Collectors.toList());
    }
}
