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

    @Override public void save(Cliente cliente) {
        Cliente clienteNormalizado = normalizarCliente(cliente);
        if (clienteNormalizado.getId() == null) clienteNormalizado.setId(UUID.randomUUID().toString());
        List<Cliente> clientes = findAll();
        clientes.add(clienteNormalizado);
        writeAll(clientes);
        copiarDatos(clienteNormalizado, cliente);
    }

    @Override public Optional<Cliente> findById(String id) { return findAll().stream().filter(actual -> Objects.equals(actual.getId(), id)).findFirst(); }

    @Override public Optional<Cliente> findByNombre(String nombre) {
        if (nombre == null) return Optional.empty();
        String nombreNormalizado = nombre.trim();
        if (nombreNormalizado.isEmpty()) return Optional.empty();
        return findAll().stream().filter(actual -> nombreNormalizado.equalsIgnoreCase(actual.getNombre())).findFirst();
    }

    @Override public Optional<Cliente> findByContacto(String contacto) {
        String contactoNormalizado = normalizarTextoContacto(contacto);
        if (contactoNormalizado == null) return Optional.empty();
        return findAll().stream().filter(actual -> contactoNormalizado.equalsIgnoreCase(normalizarTextoContacto(actual.getContacto()))).findFirst();
    }

    @Override public List<Cliente> findAll() {
        List<String[]> filas = CsvUtil.readAll(csvFilePath, SEP);
        List<Cliente> clientes = new ArrayList<>();
        for (String[] fila : filas) {
            if (fila.length < 6) continue;
            if (fila[0] != null && fila[0].equalsIgnoreCase("id")) continue;
            Cliente cliente = construirCliente(
                CsvUtil.trimToNull(fila[0]),
                fila[1],
                fila[2],
                fila[3],
                fila[4],
                CsvUtil.parseInt(fila[5], 0)
            );
            clientes.add(cliente);
        }
        return clientes;
    }

    @Override public boolean existsByContacto(String contacto) { return findByContacto(contacto).isPresent(); }

    @Override public void update(Cliente cliente) {
        Cliente clienteNormalizado = normalizarCliente(cliente);
        List<Cliente> clientes = findAll();
        for (int i = 0; i < clientes.size(); i++) {
            if (Objects.equals(clientes.get(i).getId(), clienteNormalizado.getId())) {
                clientes.set(i, clienteNormalizado);
                break;
            }
        }
        writeAll(clientes);
        copiarDatos(clienteNormalizado, cliente);
    }

    @Override public void delete(String id) {
        List<Cliente> clientes = new ArrayList<>();
        for (Cliente cliente : findAll()) if (!Objects.equals(cliente.getId(), id)) clientes.add(cliente);
        writeAll(clientes);
    }

    private void writeAll(List<Cliente> clientes) {
        List<String[]> filas = new ArrayList<>();
        filas.add(HEADERS);
        for (Cliente cliente : clientes) {
            filas.add(new String[]{
                    cliente.getId(),
                    cliente.getNombre(),
                    cliente.getContacto(),
                    cliente.getMarcaAuto(),
                    cliente.getModeloAuto(),
                    String.valueOf(cliente.getAnioAuto())
            });
        }
        CsvUtil.writeAll(csvFilePath, filas, SEP);
    }

    private Cliente normalizarCliente(Cliente cliente) {
        Cliente copia = new Cliente();
        copia.setId(cliente.getId());
        copia.setNombre(normalizarTextoGeneral(cliente.getNombre()));
        copia.setContacto(normalizarTextoContacto(cliente.getContacto()));
        copia.setMarcaAuto(normalizarTextoGeneral(cliente.getMarcaAuto()));
        copia.setModeloAuto(normalizarTextoGeneral(cliente.getModeloAuto()));
        copia.setAnioAuto(cliente.getAnioAuto());
        return copia;
    }

    private Cliente construirCliente(String id, String nombre, String contacto, String marca, String modelo, int anio) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(normalizarTextoGeneral(nombre));
        cliente.setContacto(normalizarTextoContacto(contacto));
        cliente.setMarcaAuto(normalizarTextoGeneral(marca));
        cliente.setModeloAuto(normalizarTextoGeneral(modelo));
        cliente.setAnioAuto(anio);
        return cliente;
    }

    private String normalizarTextoGeneral(String valor) {
        if (valor == null) return null;
        String texto = valor.trim();
        if (texto.isEmpty()) return texto;
        return texto;
    }

    private String normalizarTextoContacto(String valor) {
        if (valor == null) return null;
        String texto = valor.trim().replaceAll("\\s+", " ");
        if (texto.isEmpty()) return texto;
        return texto;
    }

    private void copiarDatos(Cliente origen, Cliente destino) {
        destino.setId(origen.getId());
        destino.setNombre(origen.getNombre());
        destino.setContacto(origen.getContacto());
        destino.setMarcaAuto(origen.getMarcaAuto());
        destino.setModeloAuto(origen.getModeloAuto());
        destino.setAnioAuto(origen.getAnioAuto());
    }
}
