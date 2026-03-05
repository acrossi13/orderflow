package com.dev01.orderflow;


import java.io.BufferedReader;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class IoUtils {
    private IoUtils() {
    }

    public static void writeText(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter bw = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(content);
        }
    }

    public static String readText(Path path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    public static void appendText(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter bw = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            bw.write(content);
            bw.newLine();
        }
    }

    public static void forEachLine(Path path, Consumer<String> handler) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) handler.accept(line);
        }
    }

    public static long copy(Path from, Path to) throws IOException {
        Path parent = to.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (InputStream in = Files.newInputStream(from);
             OutputStream out = Files.newOutputStream(to,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buf = new byte[8192];
            long total = 0;
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
                total += n;
            }
            return total;
        }
    }

    public static List<String> readLines(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        forEachLine(path, lines::add);
        return lines;
    }
}