package com.dev01.orderflow;

public class SwitchExamples {

    public static String handleAction(String action) {
        if (action == null) return "null";

        return switch (action.trim().toUpperCase()) {
            case "CREATE" -> "criando...";
            case "UPDATE" -> "atualizando...";
            case "DELETE" -> "deletando...";
            default       -> "ação desconhecida: " + action;
        };
    }

    public static Integer statusToHttpCode(String status) {
        if (status == null) return 400;
      return  switch (status.trim().toUpperCase()) {
            case "CREATED" -> 201;
            case "OK" -> 200;
            case "NO_CONTENT" -> 204;
            case "BAD_REQUEST" -> 400;
            case "UNAUTHORIZED" -> 401;
            default -> 500;
        };
    }
}
