package com.example.demo;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public final class IoUtils {
    private IoUtils() {}

    // 1) Ler arquivo inteiro (String)
    public static String readText(Path path) throws IOException {
        return readText(path, StandardCharsets.UTF_8);
    }

    public static String readText(Path path, Charset charset) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, charset)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            // remove o último \n se existir
            if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    // 2) Escrever texto (sobrescreve)
    public static void writeText(Path path, String content) throws IOException {
        writeText(path, content, StandardCharsets.UTF_8);
    }

    public static void writeText(Path path, String content, Charset charset) throws IOException {
        // garante diretório pai
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter bw = Files.newBufferedWriter(
                path, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(content);
        }
    }

    // 3) Escrever texto (append)
    public static void appendText(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter bw = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(content);
            bw.newLine();
        }
    }

    // 4) Copiar arquivo via stream (bom pra arquivos grandes)
    public static long copy(Path from, Path to) throws IOException {
        Path parent = to.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (InputStream in = Files.newInputStream(from);
             OutputStream out = Files.newOutputStream(to, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            return in.transferTo(out); // Java 9+; se estiver em Java 8, te passo a versão com buffer
        }
    }

    // 5) Ler linhas e processar com callback (não carrega tudo em memória)
    public static void forEachLine(Path path, Consumer<String> handler) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                handler.accept(line);
            }
        }
    }

    // 6) Ler todas as linhas (Lista) - cuidado com arquivo gigante
    public static List<String> readLines(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        forEachLine(path, lines::add);
        return lines;
    }

    // 7) Ler bytes (arquivo inteiro)
    public static byte[] readBytes(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out); // Java 9+
            return out.toByteArray();
        }
    }

    // 8) Ler .gz (gzip) como texto
    public static String readGzipText(Path gzFile) throws IOException {
        try (InputStream raw = Files.newInputStream(gzFile);
             GZIPInputStream gz = new GZIPInputStream(raw);
             InputStreamReader isr = new InputStreamReader(gz, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }
}