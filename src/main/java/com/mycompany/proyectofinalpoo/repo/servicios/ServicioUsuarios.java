package com.mycompany.proyectofinalpoo.repo.servicios;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;

public class ServicioUsuarios {
    private final UsuarioRepo usuarioRepo;

    public ServicioUsuarios(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = Objects.requireNonNull(usuarioRepo);
    }

    public Usuario iniciarSesion(String username, String password) {
        Usuario usuario = autenticar(username, password);
        SecurityContext.setCurrentUser(usuario);
        return usuario;
    }

    public void cerrarSesion() {
        SecurityContext.clear();
    }

    public Usuario crearUsuario(String username, String password, RolUsuario rol) {
        ControlAcceso.requireRol(SecurityContext.requireUser(), RolUsuario.ADMIN);
        Usuario usuario = construirUsuario(username, password, rol);
        usuarioRepo.save(usuario);
        return usuario;
    }

    public List<Usuario> listarUsuarios() {
        ControlAcceso.requireRol(SecurityContext.requireUser(), RolUsuario.ADMIN);
        return usuarioRepo.findAll();
    }

    public void cambiarPassword(String username, String passwordActual, String nuevoPassword) {
        Usuario usuarioActual = autenticar(username, passwordActual);
        Usuario solicitante = SecurityContext.requireUser();
        if (!solicitante.getUsername().equals(usuarioActual.getUsername()) &&
                !ControlAcceso.tieneRol(solicitante, RolUsuario.ADMIN)) {
            throw new AutorizacionException("no tiene permisos para cambiar la contraseña de otro usuario");
        }
        usuarioActual.setPassword(validarPassword(nuevoPassword));
        usuarioRepo.update(usuarioActual);
    }

    public void seedAdminDefault(String username, String password) {
        Optional<Usuario> existente = usuarioRepo.findByUsername(username);
        if (existente.isEmpty()) {
            Usuario admin = construirUsuario(username, password, RolUsuario.ADMIN);
            usuarioRepo.save(admin);
        }
    }

    public Usuario obtenerUsuarioActual() {
        return SecurityContext.getCurrentUser();
    }

    public boolean usuarioActualEsAdmin() {
        return ControlAcceso.tieneRol(SecurityContext.getCurrentUser(), RolUsuario.ADMIN);
    }

    private Usuario autenticar(String username, String password) {
        String user = validarUsername(username);
        String pass = validarPassword(password);
        Usuario usuario = usuarioRepo.findByUsername(user)
                .orElseThrow(() -> new AutorizacionException("credenciales inválidas"));
        if (!usuario.getPassword().equals(pass)) throw new AutorizacionException("credenciales inválidas");
        return usuario;
    }

    private Usuario construirUsuario(String username, String password, RolUsuario rol) {
        String user = validarUsername(username);
        String pass = validarPassword(password);
        if (rol == null) throw new ValidationException("rol requerido");
        return new Usuario(user, pass, rol);
    }

    private String validarUsername(String username) {
        if (username == null) throw new ValidationException("username requerido");
        String valor = username.trim();
        if (valor.isEmpty()) throw new ValidationException("username requerido");
        if (valor.length() < 3) throw new ValidationException("username muy corto");
        return valor;
    }

    private String validarPassword(String password) {
        if (password == null) throw new ValidationException("password requerido");
        String valor = password.trim();
        if (valor.length() < 4) throw new ValidationException("password debe tener al menos 4 caracteres");
        return valor;
    }
}
