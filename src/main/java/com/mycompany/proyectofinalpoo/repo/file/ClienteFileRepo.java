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

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

/**
 *
 * @author Bebe
 */
public class ClienteFileRepo implements ClienteRepo {
    private static final char SEP = ';';
    private final Path csvFilePath;
    private final String[] HEADERS = {"id","nombre","contacto","marca","modelo","año"};

    public ClienteFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("clientes.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
    }

    @Override public void save(Cliente c) {
        if (c.getId() == null) c.setId(UUID.randomUUID().toString());
    List<Cliente> clientes = findAll();
    clientes.add(c);
    writeAll(clientes);
    }

    @Override public Optional<Cliente> findById(String id) { return findAll().stream().filter(x -> Objects.equals(x.getId(), id)).findFirst(); }

    @Override public List<Cliente> findAll() {
    List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
    List<Cliente> clientes = new ArrayList<>();
    for (String[] row : rows) {
        if (row.length < 6) continue;
        Cliente cliente = new Cliente(
            CsvUtil.trimToNull(row[0]), row[1], row[2], row[3], row[4], CsvUtil.parseInt(row[5], 0)
        );
        clientes.add(cliente);
        }
    return clientes;
    }

    @Override public void update(Cliente c) {
    List<Cliente> clientes = findAll();
    for (int i = 0; i < clientes.size(); i++) if (Objects.equals(clientes.get(i).getId(), c.getId())) { clientes.set(i, c); break; }
    writeAll(clientes);
    }

    @Override public void delete(String id) {
    List<Cliente> clientes = new ArrayList<>();
    for (Cliente cliente : findAll()) if (!Objects.equals(cliente.getId(), id)) clientes.add(cliente);
    writeAll(clientes);
    }

    private void writeAll(List<Cliente> clientes) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Cliente cliente : clientes) {
            rows.add(new String[]{
                    cliente.getId(), cliente.getNombre(), cliente.getContacto(), cliente.getMarcaAuto(), cliente.getModeloAuto(), String.valueOf(cliente.getAnioAuto())
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }
}
