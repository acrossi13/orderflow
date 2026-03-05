package com.dev01.orderflow;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.dev01.orderflow.LambdaExamples.printIf;
import static com.dev01.orderflow.OptionalExamples.normalizeToken;
import static com.dev01.orderflow.SwitchExamples.handleAction;
import static com.dev01.orderflow.SwitchExamples.statusToHttpCode;

public class Main {
    public static void main(String[] args) throws Exception {

        //java 7
        var p = Path.of("tmp/a.txt");

        IoUtils.writeText(p, "linha1\nlinha2");
        System.out.println(IoUtils.readText(p));

        IoUtils.appendText(p, "linha3");

        IoUtils.forEachLine(p, line -> System.out.println(">> " + line));

        IoUtils.copy(p, Path.of("tmp/b.txt"));
        System.out.println("copiado!");

        System.out.println(handleAction("create"));
        System.out.println(handleAction(" UPDATE "));
        System.out.println(handleAction(null));
        System.out.println(handleAction("X"));

        System.out.println(statusToHttpCode("OK"));           // 200
        System.out.println(statusToHttpCode("CREATED"));      // 201
        System.out.println(statusToHttpCode("NO_CONTENT"));   // 204
        System.out.println(statusToHttpCode("BAD_REQUEST"));  // 400
        System.out.println(statusToHttpCode("UNAUTHORIZED")); // 401

        System.out.println(statusToHttpCode(" ok "));         // se você usar trim/toUpperCase -> 200
        System.out.println(statusToHttpCode("X"));            // 500
        System.out.println(statusToHttpCode(""));             // 500 (ou o que você decidir)
        System.out.println(statusToHttpCode(null));           // 400

        //java 8
        var items1 = java.util.Arrays.asList("DEV_01", "x", "ABC", "12", "DEV_01_OK");

        /* lista = items
        para cada item da lista "items" = "s"
        aplique a regra "tamanho de s maior igual 3"
         */
        printIf(items1, s -> s.length() >= 3);
        printIf(items1, s -> s.startsWith("DEV"));
        printIf(items1, s -> s.matches("[A-Z0-9_]{3,20}"));

        var items2 = java.util.Arrays.asList("DEV_01", " x y ", "DEV_02_ok", null, "ABC");

        System.out.println(StreamExamples.devCodesUpper(items2));

        List<String> items = Arrays.asList(" DEV_01 ", "x", "DEV_02_OK", null, "ABC", " dev_03 ");

        // 1) Runnable: tarefa sem entrada/sem retorno
        Runnable startLog = () -> System.out.println("Iniciando pipeline...");
        startLog.run();

        // 2) Predicate<String>: regra true/false (filtrar)
        Predicate<String> isValid = s -> s != null && !s.isBlank();
        Predicate<String> isDev = s -> s.startsWith("DEV");

        // 3) Function<String, String>: transforma (map)
        Function<String, String> normalize = s -> s.trim().toUpperCase();

        // 4) Consumer<String>: consome (não retorna) (forEach)
        Consumer<String> printer = s -> System.out.println(">> " + s);

        items.stream()
                .filter(isValid)          // remove null e vazio
                .map(normalize)           // trim + upper
                .filter(isDev)            // fica só DEV*
                .forEach(printer);        // imprime

        Runnable endLog = () -> System.out.println("Pipeline finalizado.");
        endLog.run();

        System.out.println(normalizeToken(null));      // default
        System.out.println(normalizeToken("   "));     // default
        System.out.println(normalizeToken("  abc  ")); // abc


        System.out.println(CompletableFutureExamples.buildDashboard("DEV_01").join());


        System.out.println(DateTimeExamples.nowBrazilFormatted());

        long ms = DateTimeExamples.measureMillis(() -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        });
        System.out.println("durou: " + ms + "ms");

        var codes1 = java.util.Arrays.asList("DEV_01", "dev_02", "ABC_1", "DEV_03", "ABC_2", "X");
        System.out.println(MapExamples.groupByPrefix(codes1));

        var statuses = java.util.Arrays.asList("ok", "OK", "created", "ok", null, "bad_request");
        System.out.println(MapExamples.countStatuses(statuses));

        System.out.println("ms: " + TimeDiffExamples.measureMillis(() -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }));

        System.out.println("idade: " + TimeDiffExamples.yearsOld(java.time.LocalDate.of(1982, 4, 3)) + " anos");

        var codes2 = java.util.Arrays.asList("DEV_01", "dev_02", "ABC_1", "DEV_03", "ABC_2", "X", null, "  ");
        System.out.println(GroupingExamples.groupByPrefix(codes2));

        var items3 = java.util.Arrays.asList(" DEV_01 ", "x", "ABC", null, "DEV_02_OK", "  ", "12");
        var parts = PartitionExamples.partitionValidCodes(items3);

        System.out.println("VALIDOS: " + parts.get(true));
        System.out.println("INVALIDOS: " + parts.get(false));

        var lists = java.util.Arrays.asList(
                java.util.Arrays.asList(" DEV_01 ", "DEV_02"),
                java.util.Arrays.asList("ABC_1"),
                java.util.Arrays.asList("X", " ", null, "y")
        );

        System.out.println(FlatMapExamples.flatten(lists));
    }

    }

