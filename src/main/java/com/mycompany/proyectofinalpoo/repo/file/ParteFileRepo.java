/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

/**
 *
 * @author Bebe
 */
public class ParteFileRepo implements ParteRepo {
    private static final char SEP = ';';
    private final Path csvFilePath;
    private final String[] HEADERS = {"id","nombre","categoria","cantidad","precioUnitario","costo"};

    public ParteFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("partes.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
    }

    @Override public void save(Parte p) {
        if (p.getId() == null) p.setId(UUID.randomUUID().toString());
    List<Parte> partes = findAll();
    partes.add(p);
    writeAll(partes);
    }

    @Override public Optional<Parte> findById(String id) { return findAll().stream().filter(x -> Objects.equals(x.getId(), id)).findFirst(); }

    @Override public List<Parte> findAll() {
    List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
    List<Parte> partes = new ArrayList<>();
    for (String[] row : rows) {
        if (row.length < 6) continue;
        Parte parte = new Parte(
            CsvUtil.trimToNull(row[0]), row[1], row[2], CsvUtil.parseInt(row[3], 0), CsvUtil.parseDouble(row[4], 0.0), CsvUtil.parseDouble(row[5], 0.0)
        );
        partes.add(parte);
        }
    return partes;
    }

    @Override public void update(Parte p) {
    List<Parte> partes = findAll();
    for (int i = 0; i < partes.size(); i++) if (Objects.equals(partes.get(i).getId(), p.getId())) { partes.set(i, p); break; }
    writeAll(partes);
    }

    @Override public void delete(String id) {
    List<Parte> partes = new ArrayList<>();
    for (Parte parte : findAll()) if (!Objects.equals(parte.getId(), id)) partes.add(parte);
    writeAll(partes);
    }

    private void writeAll(List<Parte> partes) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Parte parte : partes) {
            rows.add(new String[]{
                    parte.getId(), parte.getNombre(), parte.getCategoria(), String.valueOf(parte.getCantidad()), String.valueOf(parte.getPrecioUnitario()), String.valueOf(parte.getCosto())
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }
}
