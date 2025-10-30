package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Path;
import java.util.*;
import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

public class ParteFileRepo implements ParteRepo {
    private static final char SEP = ';';
    private static final int ID_MAX = 99999;

    private final Path csvFilePath;
    private final String[] HEADERS = {"id","nombre","categoria","cantidad","precioUnitario","costo"};

    public ParteFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("partes.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
        renumerarIdsSiEsNecesario();
    }

    @Override public void save(Parte p) {
        Parte normal = normalizarParte(p);
        if (!esIdNumericoValido(normal.getId())) {
            normal.setId(formatearId(siguienteId()));
        } else {
            normal.setId(formatearId(extraerNumero(normal.getId())));
        }
        List<Parte> partes = findAll();
        partes.add(normal);
        writeAll(partes);
        copiarDatos(normal, p);
    }

    @Override public Optional<Parte> findById(String id) {
        return findAll().stream().filter(x -> Objects.equals(x.getId(), id)).findFirst();
    }

    @Override public List<Parte> findAll() {
        List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
        List<Parte> partes = new ArrayList<>();
        for (String[] row : rows) {
            if (row.length < 6) continue;
            if ("id".equalsIgnoreCase(row[0])) continue;
            Parte parte = new Parte(
                CsvUtil.trimToNull(row[0]),
                row[1],
                row[2],
                CsvUtil.parseInt(row[3], 0),
                CsvUtil.parseDouble(row[4], 0.0),
                CsvUtil.parseDouble(row[5], 0.0)
            );
            partes.add(parte);
        }
        return partes;
    }

    @Override public void update(Parte p) {
        Parte normal = normalizarParte(p);
        if (esIdNumericoValido(normal.getId())) {
            normal.setId(formatearId(extraerNumero(normal.getId())));
        }
        List<Parte> partes = findAll();
        for (int i = 0; i < partes.size(); i++) {
            if (Objects.equals(partes.get(i).getId(), normal.getId())) {
                partes.set(i, normal);
                break;
            }
        }
        writeAll(partes);
        copiarDatos(normal, p);
    }

    @Override public void delete(String id) {
        if (id == null) return;
        String nid = id.trim();
        if (nid.isEmpty()) return;
        List<Parte> restantes = new ArrayList<>();
        for (Parte parte : findAll()) {
            if (!Objects.equals(parte.getId(), nid)) restantes.add(parte);
        }
        writeAll(restantes);
    }

    /* ===================== Persistencia ===================== */
    private void writeAll(List<Parte> partes) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Parte parte : partes) {
            rows.add(new String[]{
                parte.getId(),
                parte.getNombre(),
                parte.getCategoria(),
                String.valueOf(parte.getCantidad()),
                String.valueOf(parte.getPrecioUnitario()),
                String.valueOf(parte.getCosto())
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }

    /* ===================== IDs con prefijo P ===================== */
    private boolean esIdNumericoValido(String id) {
        if (id == null) return false;
        String s = id.trim();
        if (!s.matches("[Pp]?\\d{1,5}")) return false; // permite Pxxxxx o xxxxx
        int v = extraerNumero(s);
        return v >= 1 && v <= ID_MAX;
    }

    private int extraerNumero(String idConOCsinPrefijo) {
        String s = idConOCsinPrefijo.trim();
        if (s.toUpperCase().startsWith("P")) s = s.substring(1);
        return Integer.parseInt(s);
    }

    private int siguienteId() {
        List<Parte> existentes = findAll();
        Set<Integer> usados = new HashSet<>();
        for (Parte p : existentes) {
            if (esIdNumericoValido(p.getId())) {
                usados.add(extraerNumero(p.getId()));
            }
        }
        for (int i = 1; i <= ID_MAX; i++) {
            if (!usados.contains(i)) return i;
        }
        throw new IllegalStateException("Se alcanzó el límite de IDs de partes (" + ID_MAX + ").");
    }

    private void renumerarIdsSiEsNecesario() {
        List<Parte> todos = findAll();
        boolean cambiado = false;
        Set<Integer> ocupados = new HashSet<>();

        for (Parte p : todos) {
            if (esIdNumericoValido(p.getId())) {
                ocupados.add(extraerNumero(p.getId()));
            }
        }

        int cursor = 1;
        for (Parte p : todos) {
            if (!esIdNumericoValido(p.getId())) {
                while (ocupados.contains(cursor) && cursor <= ID_MAX) cursor++;
                if (cursor > ID_MAX)
                    throw new IllegalStateException("Sin espacio para renumerar partes.");
                p.setId(formatearId(cursor));
                ocupados.add(cursor);
                cursor++;
                cambiado = true;
            } else {
                p.setId(formatearId(extraerNumero(p.getId())));
            }
        }

        if (cambiado) writeAll(todos);
    }

    private String formatearId(int valor) {
        return "P" + String.format("%05d", valor);
    }

    /* ===================== Normalización ===================== */
    private Parte normalizarParte(Parte p) {
        Parte c = new Parte();
        c.setId(p.getId());
        c.setNombre(normalizarTextoGeneral(p.getNombre()));
        c.setCategoria(normalizarTextoGeneral(p.getCategoria()));
        c.setCantidad(p.getCantidad());
        c.setPrecioUnitario(p.getPrecioUnitario());
        c.setCosto(p.getCosto());
        return c;
    }

    private String normalizarTextoGeneral(String valor) {
        if (valor == null) return null;
        String t = valor.trim();
        return t.isEmpty() ? t : t;
    }

    private void copiarDatos(Parte origen, Parte destino) {
        destino.setId(origen.getId());
        destino.setNombre(origen.getNombre());
        destino.setCategoria(origen.getCategoria());
        destino.setCantidad(origen.getCantidad());
        destino.setPrecioUnitario(origen.getPrecioUnitario());
        destino.setCosto(origen.getCosto());
    }
}
