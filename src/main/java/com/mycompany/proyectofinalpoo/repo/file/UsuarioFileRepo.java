package com.mycompany.proyectofinalpoo.repo.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.util.CsvUtil;

public class UsuarioFileRepo implements UsuarioRepo {
    private static final char SEP = ';';
    private final Path csvFilePath;
    private static final String[] HEADERS = {"username","password","rol"};

    public UsuarioFileRepo(Path dataDir) {
        this.csvFilePath = dataDir.resolve("usuarios.csv");
        CsvUtil.ensureHeaders(csvFilePath, HEADERS, SEP);
    }

    @Override
    public void save(Usuario usuario) {
        Objects.requireNonNull(usuario, "usuario");
        if (findByUsername(usuario.getUsername()).isPresent()) {
            throw new IllegalArgumentException("usuario ya existe: " + usuario.getUsername());
        }
        List<Usuario> usuarios = findAll();
        usuarios.add(usuario);
        writeAll(usuarios);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String buscado = username.trim();
        if (buscado.isEmpty()) return Optional.empty();
        return findAll().stream().filter(u -> buscado.equalsIgnoreCase(u.getUsername())).findFirst();
    }

    @Override
    public List<Usuario> findAll() {
        List<String[]> rows = CsvUtil.readAll(csvFilePath, SEP);
        List<Usuario> usuarios = new ArrayList<>();
        for (String[] row : rows) {
            if (row.length < 3) continue;
            if ("username".equalsIgnoreCase(row[0])) continue;
            Usuario usuario = new Usuario();
            usuario.setUsername(row[0]);
            usuario.setPassword(row[1]);
            usuario.setRol(RolUsuario.fromNombre(row[2]));
            usuarios.add(usuario);
        }
        return usuarios;
    }

    @Override
    public void update(Usuario usuario) {
        Objects.requireNonNull(usuario, "usuario");
        List<Usuario> usuarios = findAll();
        boolean updated = false;
        for (int i = 0; i < usuarios.size(); i++) {
            if (Objects.equals(usuarios.get(i).getUsername(), usuario.getUsername())) {
                usuarios.set(i, usuario);
                updated = true;
                break;
            }
        }
        if (!updated) throw new IllegalArgumentException("usuario no encontrado: " + usuario.getUsername());
        writeAll(usuarios);
    }

    @Override
    public boolean delete(String username) {
        List<Usuario> usuarios = new ArrayList<>();
        boolean removed = false;
        for (Usuario usuario : findAll()) {
            if (Objects.equals(usuario.getUsername(), username)) {
                removed = true;
            } else {
                usuarios.add(usuario);
            }
        }
        if (!removed) return false;
        writeAll(usuarios);
        return true;
    }

    private void writeAll(List<Usuario> usuarios) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Usuario usuario : usuarios) {
            rows.add(new String[]{
                    usuario.getUsername(),
                    usuario.getPassword(),
                    usuario.getRol().name()
            });
        }
        CsvUtil.writeAll(csvFilePath, rows, SEP);
    }
}
