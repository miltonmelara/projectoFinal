/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo;
import java.util.Objects;

/**
 *
 * @author Bebe
 */
public class Usuario {
    private String username;
    private String password;
    private RolUsuario rol;

    public Usuario() {}

    public Usuario(String username, String password, RolUsuario rol) {
        setUsername(username);
        setPassword(password);
        setRol(rol);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username requerido");
        this.username = username.trim();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        if (password == null) throw new IllegalArgumentException("password requerido");
        this.password = password;
    }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) {
        if (rol == null) throw new IllegalArgumentException("rol requerido");
        this.rol = rol;
    }

    public void setRole(String role) {
        setRol(RolUsuario.fromNombre(role));
    }

    public String getRole() {
        return rol != null ? rol.name() : null;
    }

    @Override public String toString() {
        return "Usuario{" +
                "username='" + username + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(username, usuario.username);
    }

    @Override public int hashCode() { return Objects.hash(username); }

}
