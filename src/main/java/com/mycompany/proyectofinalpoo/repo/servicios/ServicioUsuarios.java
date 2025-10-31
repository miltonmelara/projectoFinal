package com.mycompany.proyectofinalpoo.repo.servicios;

import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;

import java.util.List;
import java.util.Optional;

public class ServicioUsuarios {
    private final UsuarioRepo repo;

    public ServicioUsuarios(UsuarioRepo repo) {
        this.repo = repo;
    }

    // Semillas de conveniencia (las usas en InicioSesion)
    public void seedAdminDefault(String username, String password) {
        if (!repo.existsByUsername(username)) {
            Usuario admin = new Usuario();
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setRol(RolUsuario.ADMIN);
            repo.save(admin);
        } else {
            // Si existe pero no es ADMIN, lo forzamos a ADMIN (opcional)
            repo.findByUsername(username).ifPresent(u -> {
                if (u.getRol() != RolUsuario.ADMIN) {
                    u.setRol(RolUsuario.ADMIN);
                    repo.update(u);
                }
            });
        }
        asegurarUnicoAdmin(); // opcional para garantizar que solo haya uno
    }

    public void seedUsuario(String username, String password, RolUsuario rol) {
        if (!repo.existsByUsername(username)) {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(password);
            u.setRol(rol);
            repo.save(u);
        }
    }

    // Gestión de mecánicos
    public void crearMecanico(String username, String password) {
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("El usuario ya existe: " + username);
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(password);
        u.setRol(RolUsuario.MECANICO);
        repo.save(u);
    }

    public boolean eliminarMecanico(String username) {
        Optional<Usuario> u = repo.findByUsername(username);
        if (u.isEmpty()) return false;
        if (u.get().getRol() == RolUsuario.ADMIN) {
            throw new IllegalStateException("No puedes eliminar al administrador.");
        }
        return repo.delete(username);
    }

    public List<Usuario> listarMecanicos() {
        return repo.findByRole(RolUsuario.MECANICO);
    }

    public Usuario actualizarPassword(String username, String nuevoPassword) {
        Usuario usuario = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        if (usuario.getRol() != RolUsuario.MECANICO) {
            throw new IllegalArgumentException("Solo se permiten contraseñas de mecánicos");
        }
        usuario.setPassword(nuevoPassword);
        repo.update(usuario);
        return usuario;
    }

    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    public Optional<Usuario> getAdmin() {
        return repo.findByRole(RolUsuario.ADMIN).stream().findFirst();
    }

    // Garantiza que haya exactamente un ADMIN (ajústalo a tu política)
    private void asegurarUnicoAdmin() {
        List<Usuario> admins = repo.findByRole(RolUsuario.ADMIN);
        if (admins.isEmpty()) {
            // Crea uno si no hay (no debería pasar si llamaste seedAdminDefault)
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRol(RolUsuario.ADMIN);
            repo.save(admin);
        } else if (admins.size() > 1) {
            // Si hay más de uno, dejamos el primero y degradamos el resto a MECÁNICO
            for (int i = 1; i < admins.size(); i++) {
                Usuario extra = admins.get(i);
                extra.setRol(RolUsuario.MECANICO);
                repo.update(extra);
            }
        }
    }
}
