/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

/**
 *
 * @author Bebe
 */
public class ReservaFileRepo implements ReservaRepo {
    private static final char SEP = ';';
    private final Path csvFilePath;
    private final String[] HEADERS = {"id","clienteId","servicioId","fechaISO","estado","mecanicoAsignado"};

    public ReservaFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("reservas.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
    }

    @Override public void save(Reserva r) {
        if (r.getId() == null) r.setId(UUID.randomUUID().toString());
    List<Reserva> reservas = findAll();
    reservas.add(r);
    writeAll(reservas);
    }

    @Override public Optional<Reserva> findById(String id) { return findAll().stream().filter(x -> Objects.equals(x.getId(), id)).findFirst(); }

    @Override public List<Reserva> findAll() {
    List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
    List<Reserva> reservas = new ArrayList<>();
    for (String[] row : rows) {
        if (row.length < 6) continue;
        Reserva reserva = new Reserva(
            CsvUtil.trimToNull(row[0]), row[1], row[2], CsvUtil.parseDateTime(row[3]), CsvUtil.parseEstado(row[4], ReservaEstado.PROGRAMADA), row[5]
        );
        reservas.add(reserva);
        }
    return reservas;
    }

    @Override public void update(Reserva r) {
    List<Reserva> reservas = findAll();
    for (int i = 0; i < reservas.size(); i++) if (Objects.equals(reservas.get(i).getId(), r.getId())) { reservas.set(i, r); break; }
    writeAll(reservas);
    }

    @Override public void delete(String id) {
    List<Reserva> reservas = new ArrayList<>();
    for (Reserva reserva : findAll()) if (!Objects.equals(reserva.getId(), id)) reservas.add(reserva);
    writeAll(reservas);
    }

    @Override public List<Reserva> findByClienteId(String clienteId) {
    List<Reserva> reservas = new ArrayList<>();
    for (Reserva reserva : findAll()) if (Objects.equals(reserva.getClienteId(), clienteId)) reservas.add(reserva);
    return reservas;
    }

    @Override public List<Reserva> findByFecha(LocalDate day) {
    List<Reserva> reservas = new ArrayList<>();
    for (Reserva reserva : findAll()) if (reserva.getFecha().toLocalDate().isEqual(day)) reservas.add(reserva);
    return reservas;
    }

    private void writeAll(List<Reserva> reservas) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Reserva reserva : reservas) {
            rows.add(new String[]{
                    reserva.getId(), reserva.getClienteId(), reserva.getServicioId(), CsvUtil.formatDateTime(reserva.getFecha()), reserva.getEstado().name(), reserva.getMecanicoAsignado()
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }
}
