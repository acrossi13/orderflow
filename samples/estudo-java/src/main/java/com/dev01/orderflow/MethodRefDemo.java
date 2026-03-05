package com.dev01.orderflow;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodRefDemo {

    public static void main(String[] args) {
        exemploMetodoEstatico();
        exemploMetodoDeInstanciaDeObjeto();
        exemploMetodoDeInstanciaDoParametro();
        exemploConstrutor();
    }

    // A) Referência a método estático: Classe::metodoStatic
    // Lambda equivalente: x -> Integer.parseInt(x)
    static void exemploMetodoEstatico() {
        System.out.println("\nA) Método estático");

        List<String> xs = Arrays.asList("10", "20", "30");

        List<Integer> nums = xs.stream()
                .map(Integer::parseInt) // chama Integer.parseInt(s) para cada item
                .collect(Collectors.toList());

        System.out.println(nums); // [10, 20, 30]
    }

    // B) Referência a método de instância (objeto específico): objeto::metodo
    // Lambda equivalente: s -> printer.print(s)
    static void exemploMetodoDeInstanciaDeObjeto() {
        System.out.println("\nB) Método de instância de um objeto específico");

        Printer printer = new Printer(">> ");

        List<String> xs = Arrays.asList("a", "b", "c");

        // Em vez de: xs.forEach(s -> printer.print(s));
        xs.forEach(printer::print);
    }

    // C) Referência a método de instância do "parâmetro": Tipo::metodo
    // Lambda equivalente: s -> s.trim() ou s -> s.toUpperCase()
    static void exemploMetodoDeInstanciaDoParametro() {
        System.out.println("\nC) Método de instância do parâmetro (Tipo::metodo)");

        List<String> xs = Arrays.asList(" dev_01 ", "abc", " DEV_02 ");

        List<String> out = xs.stream()
                .map(String::trim)        // para cada s, chama s.trim()
                .map(String::toUpperCase) // para cada s, chama s.toUpperCase()
                .collect(Collectors.toList());

        System.out.println(out); // [DEV_01, ABC, DEV_02]
    }

    // D) Referência a construtor: Tipo::new
    // Lambda equivalente: s -> new StringBuilder(s)
    static void exemploConstrutor() {
        System.out.println("\nD) Construtor (Tipo::new)");

        List<String> xs = Arrays.asList("x", "y");

        List<StringBuilder> builders = xs.stream()
                .map(StringBuilder::new) // new StringBuilder(s)
                .collect(Collectors.toList());

        // só pra mostrar que virou StringBuilder
        builders.forEach(sb -> sb.append("!"));
        System.out.println(builders); // [x!, y!]
    }

    // classe auxiliar pro exemplo B
    static class Printer {
        private final String prefix;

        Printer(String prefix) {
            this.prefix = prefix;
        }

        void print(String s) {
            System.out.println(prefix + s);
        }
    }
/*
    x -> Integer.parseInt(x) = Integer::parseInt

    imprimir
      antes
        items.forEach(x -> System.out.println(x));
     depois
        items.forEach(System.out::println);

       Transformar string
        antes
           stream.map(s -> s.trim());
        depois
            stream.map(String::trim);

        Chamar metodo de um objeto específico

          Printer p = new Printer();
        antes
              items.forEach(s -> p.print(s));
        depois
              items.forEach(p::print);

        constructor

        antes
            stream.map(s -> new StringBuilder(s));
        depois
            stream.map(StringBuilder::new);

        Quando não dá pra trocar (ou não vale)?

          quando em mais de uma operação
            s -> s.trim().toUpperCase()   // não vira :: direto
            aqui vc mantem lambda

            stream.map(MethodRefDemo::normalize);

                    static String normalize(String s) {
                        return s.trim().toUpperCase();
                    }


            quando precisa de logica
                s -> s != null && s.length() >= 3
     */
}