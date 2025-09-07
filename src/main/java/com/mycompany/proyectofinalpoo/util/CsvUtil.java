/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.util;

import com.mycompany.proyectofinalpoo.ReservaEstado;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Bebe
 */
public final class CsvUtil {
    private CsvUtil() {}

    public static List<String[]> readAll(Path p, char sep) {
        ensureFile(p);
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                rows.add(split(line, sep));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo CSV: " + p + ": " + e.getMessage(), e);
        }
        return rows;
    }

    public static void writeAll(Path p, List<String[]> rows, char sep) {
        ensureFile(p);
        try (BufferedWriter bw = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            // Asumimos que la primera fila del archivo ya contiene headers; el caller debe incluirla.
            for (int i = 0; i < rows.size(); i++) {
                String[] r = rows.get(i);
                bw.write(join(r, sep));
                if (i < rows.size() - 1) bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo CSV: " + p + ": " + e.getMessage(), e);
        }
    }

    public static String[] split(String line, char sep) {
        String[] parts = line.split(java.util.regex.Pattern.quote(String.valueOf(sep)),-1);
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    public static String join(String[] arr, char sep) {
        return String.join(String.valueOf(sep), arr);
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    public static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    public static ReservaEstado parseEstado(String s, ReservaEstado def) {
        try { return ReservaEstado.valueOf(s.trim()); } catch (Exception e) { return def; }
    }

    public static LocalDateTime parseDateTime(String s) {
        return LocalDateTime.parse(s.trim());
    }

    public static String formatDateTime(LocalDateTime dt) { return dt.toString(); }

    public static LocalDate parseDate(String s) { return LocalDate.parse(s.trim()); }

    public static String[] ensureHeaders(Path p, String[] headers, char sep) {
        ensureDir(p.getParent());
        if (!Files.exists(p)) {
            List<String[]> rows = new ArrayList<>();
            rows.add(headers);
            writeAll(p, rows, sep);
        } else {
            // ensure first line is headers
            try {
                List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                if (lines.isEmpty()) {
                    List<String[]> rows = new ArrayList<>();
                    rows.add(headers);
                    writeAll(p, rows, sep);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return headers;
    }

    private static void ensureFile(Path p) {
        ensureDir(p.getParent());
        if (!Files.exists(p)) {
            try { Files.createFile(p); } catch (IOException e) { throw new RuntimeException(e); }
        }
    }

    private static void ensureDir(Path dir) {
        try { if (dir != null) Files.createDirectories(dir); } catch (IOException e) { throw new RuntimeException(e); }
    }
}
