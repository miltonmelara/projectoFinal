package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Path;
import java.util.*;
import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

public class ClienteFileRepo implements ClienteRepo {
    private static final char SEP = ';';
    private static final int ID_MAX = 99999;
    private final Path csvFilePath;
    private final String[] HEADERS = {"id","nombre","contacto","marca","modelo","año"};

    public ClienteFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("clientes.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
        renumerarIdsSiEsNecesario();
    }

    @Override
    public void save(Cliente cliente) {
        Cliente normal = normalizarCliente(cliente);
        if (!esIdNumericoValido(normal.getId())) {
            normal.setId(formatearId(siguienteId()));
        } else {
            // Reescribir con padding + prefijo C
            normal.setId(formatearId(extraerNumero(normal.getId())));
        }
        List<Cliente> clientes = findAll();
        clientes.add(normal);
        writeAll(clientes);
        copiarDatos(normal, cliente);
    }

    @Override public Optional<Cliente> findById(String id) {
        return findAll().stream().filter(c -> Objects.equals(c.getId(), id)).findFirst();
    }

    @Override public Optional<Cliente> findByNombre(String nombre) {
        if (nombre == null) return Optional.empty();
        String n = nombre.trim();
        if (n.isEmpty()) return Optional.empty();
        return findAll().stream().filter(c -> n.equalsIgnoreCase(c.getNombre())).findFirst();
    }

    @Override public Optional<Cliente> findByContacto(String contacto) {
        String t = normalizarTextoContacto(contacto);
        if (t == null) return Optional.empty();
        return findAll().stream()
                .filter(c -> t.equalsIgnoreCase(normalizarTextoContacto(c.getContacto())))
                .findFirst();
    }

    @Override public List<Cliente> findAll() {
        List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
        List<Cliente> out = new ArrayList<>();
        for (String[] r : rows) {
            if (r.length < 6) continue;
            if ("id".equalsIgnoreCase(r[0])) continue; // skip header
            Cliente c = construirCliente(
                    CsvUtil.trimToNull(r[0]),
                    r[1], r[2], r[3], r[4],
                    CsvUtil.parseInt(r[5], 0)
            );
            out.add(c);
        }
        return out;
    }

    @Override public boolean existsByContacto(String contacto) {
        return findByContacto(contacto).isPresent();
    }

    @Override public void update(Cliente cliente) {
        Cliente normal = normalizarCliente(cliente);
        if (esIdNumericoValido(normal.getId())) {
            normal.setId(formatearId(extraerNumero(normal.getId())));
        }
        List<Cliente> todos = findAll();
        for (int i = 0; i < todos.size(); i++) {
            if (Objects.equals(todos.get(i).getId(), normal.getId())) {
                todos.set(i, normal);
                break;
            }
        }
        writeAll(todos);
        copiarDatos(normal, cliente);
    }

    @Override public boolean delete(String id) {
        if (id == null) return false;
        String nid = id.trim();
        if (nid.isEmpty()) return false;
        List<Cliente> todos = findAll();
        List<Cliente> restantes = new ArrayList<>();
        boolean eliminado = false;
        for (Cliente c : todos) {
            if (Objects.equals(c.getId(), nid)) eliminado = true;
            else restantes.add(c);
        }
        if (!eliminado) return false;
        writeAll(restantes);
        return true;
    }

    /* ===================== Persistencia ===================== */
    private void writeAll(List<Cliente> clientes) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Cliente c : clientes) {
            rows.add(new String[] {
                    c.getId(),
                    c.getNombre(),
                    c.getContacto(),
                    c.getMarcaAuto(),
                    c.getModeloAuto(),
                    String.valueOf(c.getAnioAuto())
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }

    /* ===================== IDs con prefijo C ===================== */

    private boolean esIdNumericoValido(String id) {
        if (id == null) return false;
        String s = id.trim();
        if (!s.matches("[Cc]?\\d{1,5}")) return false; // permite Cxxxxx o xxxxx
        int v = extraerNumero(s);
        return v >= 1 && v <= ID_MAX;
    }

    private int extraerNumero(String idConOCsinPrefijo) {
        String s = idConOCsinPrefijo.trim();
        if (s.toUpperCase().startsWith("C")) s = s.substring(1);
        return Integer.parseInt(s);
    }

    private int siguienteId() {
        List<Cliente> existentes = findAll();
        Set<Integer> usados = new HashSet<>();
        for (Cliente c : existentes) {
            if (esIdNumericoValido(c.getId())) {
                usados.add(extraerNumero(c.getId()));
            }
        }
        for (int i = 1; i <= ID_MAX; i++) {
            if (!usados.contains(i)) return i;
        }
        throw new IllegalStateException("Se alcanzó el límite de IDs de clientes (" + ID_MAX + ").");
    }

    private void renumerarIdsSiEsNecesario() {
        List<Cliente> todos = findAll();
        boolean cambiado = false;
        Set<Integer> ocupados = new HashSet<>();

        for (Cliente c : todos) {
            if (esIdNumericoValido(c.getId())) {
                ocupados.add(extraerNumero(c.getId()));
            }
        }

        int cursor = 1;
        for (Cliente c : todos) {
            if (!esIdNumericoValido(c.getId())) {
                while (ocupados.contains(cursor) && cursor <= ID_MAX) cursor++;
                if (cursor > ID_MAX)
                    throw new IllegalStateException("Sin espacio para renumerar clientes.");
                c.setId(formatearId(cursor));
                ocupados.add(cursor);
                cursor++;
                cambiado = true;
            } else {
                c.setId(formatearId(extraerNumero(c.getId())));
            }
        }

        if (cambiado) writeAll(todos);
    }

    private String formatearId(int valor) {
        return "C" + String.format("%05d", valor);
    }

    /* ===================== Normalización ===================== */

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
        Cliente c = new Cliente();
        c.setId(id);
        c.setNombre(normalizarTextoGeneral(nombre));
        c.setContacto(normalizarTextoContacto(contacto));
        c.setMarcaAuto(normalizarTextoGeneral(marca));
        c.setModeloAuto(normalizarTextoGeneral(modelo));
        c.setAnioAuto(anio);
        return c;
    }

    private String normalizarTextoGeneral(String valor) {
        if (valor == null) return null;
        String t = valor.trim();
        return t.isEmpty() ? t : t;
    }

    private String normalizarTextoContacto(String valor) {
        if (valor == null) return null;
        String t = valor.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? t : t;
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
