package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mycompany.proyectofinalpoo.ConsumoParte;
import com.mycompany.proyectofinalpoo.repo.ConsumoParteRepo;

public class ConsumoParteFileRepo implements ConsumoParteRepo {
    private final Path csv;
    private final String SEP = ";";
    private final String[] HEAD = new String[]{"id","reservaId","parteId","cantidad","fechaHora"};

    public ConsumoParteFileRepo(Path dataDir) {
        this.csv = dataDir.resolve("consumos.csv");
        ensureHeaders();
    }

    public List<ConsumoParte> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        List<ConsumoParte> out = new ArrayList<>();
        for (ConsumoParte c : readAll()) {
            LocalDate d = c.getFechaHora().toLocalDate();
            if ((d.isEqual(inicio) || d.isAfter(inicio)) && (d.isEqual(fin) || d.isBefore(fin))) out.add(c);
        }
        return out;
    }

    public List<ConsumoParte> findByReservaId(String reservaId) {
        List<ConsumoParte> out = new ArrayList<>();
        for (ConsumoParte c : readAll()) if (reservaId.equals(c.getReservaId())) out.add(c);
        return out;
    }

    public Optional<ConsumoParte> findById(String id) {
        for (ConsumoParte c : readAll()) if (id.equals(c.getId())) return Optional.of(c);
        return Optional.empty();
    }

    public void save(ConsumoParte c) {
        if (c.getId() == null || c.getId().isEmpty()) c.setId(java.util.UUID.randomUUID().toString());
        List<ConsumoParte> all = readAll();
        all.add(c);
        writeAll(all);
    }

    public void saveAll(List<ConsumoParte> lista) {
        for (ConsumoParte c : lista) if (c.getId() == null || c.getId().isEmpty()) c.setId(java.util.UUID.randomUUID().toString());
        List<ConsumoParte> all = readAll();
        all.addAll(lista);
        writeAll(all);
    }

    public void deleteByReservaId(String reservaId) {
        List<ConsumoParte> all = readAll();
        List<ConsumoParte> keep = new ArrayList<>();
        for (ConsumoParte c : all) if (!reservaId.equals(c.getReservaId())) keep.add(c);
        writeAll(keep);
    }

    private void ensureHeaders() {
        try {
            if (!Files.exists(csv.getParent())) Files.createDirectories(csv.getParent());
            if (!Files.exists(csv)) {
                List<String> head = new ArrayList<>();
                head.add(String.join(SEP, HEAD));
                Files.write(csv, head, StandardCharsets.UTF_8);
            } else {
                List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
                if (lines.isEmpty() || !lines.get(0).equals(String.join(SEP, HEAD))) {
                    List<String> newLines = new ArrayList<>();
                    newLines.add(String.join(SEP, HEAD));
                    for (int i = 0; i < lines.size(); i++) {
                        String ln = lines.get(i);
                        if (i == 0 && ln.trim().isEmpty()) continue;
                        if (i == 0 && ln.equals(String.join(SEP, HEAD))) continue;
                        newLines.add(ln);
                    }
                    Files.write(csv, newLines, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private List<ConsumoParte> readAll() {
        List<ConsumoParte> out = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String ln = lines.get(i);
                if (ln == null || ln.trim().isEmpty()) continue;
                String[] r = ln.split(SEP, -1);
                if (r.length < 5) continue;
                ConsumoParte c = new ConsumoParte();
                c.setId(r[0]);
                c.setReservaId(r[1]);
                c.setParteId(r[2]);
                try { c.setCantidad(Integer.parseInt(r[3])); } catch (Exception ex) { c.setCantidad(0); }
                try { c.setFechaHora(java.time.LocalDateTime.parse(r[4])); } catch (Exception ex) { c.setFechaHora(java.time.LocalDateTime.now()); }
                out.add(c);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return out;
    }

    private void writeAll(List<ConsumoParte> lista) {
        try {
            List<String> rows = new ArrayList<>();
            rows.add(String.join(SEP, HEAD));
            for (ConsumoParte c : lista) {
                StringBuilder sb = new StringBuilder();
                sb.append(nz(c.getId())).append(SEP)
                  .append(nz(c.getReservaId())).append(SEP)
                  .append(nz(c.getParteId())).append(SEP)
                  .append(String.valueOf(c.getCantidad())).append(SEP)
                  .append(c.getFechaHora() == null ? "" : c.getFechaHora().toString());
                rows.add(sb.toString());
            }
            Files.write(csv, rows, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String nz(String s) { return s == null ? "" : s; }
}
