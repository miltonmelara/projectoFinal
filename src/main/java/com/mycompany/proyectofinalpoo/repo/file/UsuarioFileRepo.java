package com.mycompany.proyectofinalpoo.repo.file;

import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UsuarioFileRepo implements UsuarioRepo {
    private static final char SEP = ';';
    private final Path csvFilePath;
    // Estructura del CSV: username;password;rol
    private static final String[] HEADERS = {"username","password","rol"};

    public UsuarioFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("usuarios.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
    }

    /* ===================== CRUD principal ===================== */

    @Override
    public void save(Usuario u) {
        if (u == null || isBlank(u.getUsername())) {
            throw new IllegalArgumentException("Usuario inválido");
        }
        List<Usuario> todos = findAll();
        int idx = indexOfUsername(todos, u.getUsername());
        Usuario normal = normalizar(u);

        if (idx >= 0) {
            // si ya existe, actualizamos (política conservadora)
            todos.set(idx, normal);
        } else {
            todos.add(normal);
        }
        writeAll(todos);
        copiar(normal, u);
    }

    @Override
    public void update(Usuario u) {
        if (u == null || isBlank(u.getUsername())) {
            throw new IllegalArgumentException("Usuario inválido");
        }
        List<Usuario> todos = findAll();
        int idx = indexOfUsername(todos, u.getUsername());
        if (idx < 0) {
            throw new IllegalStateException("No existe el usuario: " + u.getUsername());
        }
        Usuario normal = normalizar(u);
        todos.set(idx, normal);
        writeAll(todos);
        copiar(normal, u);
    }

    @Override
    public boolean delete(String username) {
        if (isBlank(username)) return false;
        List<Usuario> todos = findAll();
        List<Usuario> restantes = new ArrayList<>();
        boolean eliminado = false;
        for (Usuario x : todos) {
            if (equalsIgnoreCase(x.getUsername(), username)) {
                eliminado = true;
            } else {
                restantes.add(x);
            }
        }
        if (eliminado) writeAll(restantes);
        return eliminado;
    }

    /* ===================== Lecturas ===================== */

    @Override
    public Optional<Usuario> findByUsername(String username) {
        if (isBlank(username)) return Optional.empty();
        return findAll().stream()
                .filter(u -> equalsIgnoreCase(u.getUsername(), username))
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public List<Usuario> findAll() {
        List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
        List<Usuario> out = new ArrayList<>();
        for (String[] r : rows) {
            if (r.length < 3) continue;
            if ("username".equalsIgnoreCase(r[0])) continue; // header
            Usuario u = new Usuario();
            u.setUsername(nzTrim(r[0]));
            u.setPassword(nzTrim(r[1]));
            u.setRol(parseRol(nzTrim(r[2])));
            out.add(u);
        }
        return out;
    }

    @Override
    public List<Usuario> findByRole(RolUsuario role) {
        List<Usuario> out = new ArrayList<>();
        for (Usuario u : findAll()) {
            if (u.getRol() == role) out.add(u);
        }
        return out;
    }

    /* ===================== Helpers internos ===================== */

    private void writeAll(List<Usuario> usuarios) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Usuario u : usuarios) {
            rows.add(new String[]{
                    nz(u.getUsername()),
                    nz(u.getPassword()),
                    u.getRol() == null ? RolUsuario.MECANICO.name() : u.getRol().name()
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }

    private Usuario normalizar(Usuario src) {
        Usuario u = new Usuario();
        u.setUsername(nzTrim(src.getUsername()));
        u.setPassword(nz(src.getPassword())); // aquí podrías hashear más adelante
        u.setRol(src.getRol() == null ? RolUsuario.MECANICO : src.getRol());
        return u;
    }

    private void copiar(Usuario origen, Usuario destino) {
        destino.setUsername(origen.getUsername());
        destino.setPassword(origen.getPassword());
        destino.setRol(origen.getRol());
    }

    private int indexOfUsername(List<Usuario> lista, String username) {
        for (int i = 0; i < lista.size(); i++) {
            if (equalsIgnoreCase(lista.get(i).getUsername(), username)) return i;
        }
        return -1;
    }

    private RolUsuario parseRol(String raw) {
        if (raw == null) return RolUsuario.MECANICO;
        try {
            return RolUsuario.valueOf(raw.trim().toUpperCase());
        } catch (Exception ignored) {
            return RolUsuario.MECANICO;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return Objects.equals(a, b);
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String nzTrim(String s) { return s == null ? "" : s.trim(); }
}
