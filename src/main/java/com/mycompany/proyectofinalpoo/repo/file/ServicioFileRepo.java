/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

/**
 *
 * @author Bebe
 */
public class ServicioFileRepo implements ServicioRepo {
    private static final char SEP = ';';
    private final Path csvFilePath;
    private final String[] HEADERS = {"id","nombre","duracionMin","costoTotal","precio","partesRequeridasJSON"};

    public ServicioFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("servicios.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
    }

    @Override public void save(Servicio servicio) {
        if (servicio.getId() == null) servicio.setId(UUID.randomUUID().toString());
        List<Servicio> servicios = findAll();
        servicios.add(servicio);
        writeAll(servicios);
    }

    @Override public Optional<Servicio> findById(String id) { return findAll().stream().filter(x -> Objects.equals(x.getId(), id)).findFirst(); }

    @Override public List<Servicio> findAll() {
        List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
        List<Servicio> servicios = new ArrayList<>();
        for (String[] row : rows) {
            if (row.length < 6) continue;
            Map<String,Integer> partesRequeridas = parseJsonMap(row[5]);
            Servicio servicio = new Servicio(
                    CsvUtil.trimToNull(row[0]), row[1], CsvUtil.parseInt(row[2], 0), CsvUtil.parseDouble(row[3], 0.0), CsvUtil.parseDouble(row[4], 0.0), partesRequeridas
            );
            servicios.add(servicio);
        }
        return servicios;
    }

    @Override public void update(Servicio servicio) {
        List<Servicio> servicios = findAll();
        for (int i = 0; i < servicios.size(); i++) if (Objects.equals(servicios.get(i).getId(), servicio.getId())) { servicios.set(i, servicio); break; }
        writeAll(servicios);
    }

    @Override public void delete(String id) {
        List<Servicio> servicios = new ArrayList<>();
        for (Servicio servicio : findAll()) if (!Objects.equals(servicio.getId(), id)) servicios.add(servicio);
        writeAll(servicios);
    }

    private void writeAll(List<Servicio> servicios) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Servicio servicio : servicios) {
            rows.add(new String[]{
                    servicio.getId(), servicio.getNombre(), String.valueOf(servicio.getDuracionMin()), String.valueOf(servicio.getCostoTotal()), String.valueOf(servicio.getPrecio()), toJsonMap(servicio.getPartesRequeridas())
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }

    private static Map<String,Integer> parseJsonMap(String json) {
        Map<String,Integer> map = new LinkedHashMap<>();
        if (json == null) return map;
        String s = json.trim();
        if (s.isEmpty() || s.equals("{}")) return map;
        if (s.startsWith("{") && s.endsWith("}")) s = s.substring(1, s.length()-1);
        if (s.trim().isEmpty()) return map;
        String[] entries = s.split(",");
        for (String entry : entries) {
            String[] kv = entry.split(":");
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            if (key.startsWith("\"") && key.endsWith("\"")) key = key.substring(1, key.length()-1);
            try { map.put(key, Integer.parseInt(kv[1].trim())); } catch (NumberFormatException ex) { map.put(key, 0); }
        }
        return map;
    }

    private static String toJsonMap(Map<String,Integer> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String,Integer> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(e.getKey()).append('"').append(':').append(e.getValue());
        }
        sb.append('}');
        return sb.toString();
    }
}
