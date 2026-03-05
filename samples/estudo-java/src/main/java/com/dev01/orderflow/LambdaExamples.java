package com.dev01.orderflow;

import java.util.List;
import java.util.function.Predicate;

public class LambdaExamples {
    /*
    1) O que é uma lambda (em 1 frase)
    Lambda é uma forma curta de implementar uma interface que
    tem 1 único método abstrato (functional interface),
                   sem escrever uma classe inteira.
     */
    private LambdaExamples() {
    }

    // predicate boolean
    public static void printIf(List<String> items, Predicate<String> rule) {
        for (String it : items) {
            if (rule.test(it)) {
                System.out.println(it);
            }
        }
    }

}
