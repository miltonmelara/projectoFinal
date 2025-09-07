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
    private String role;

    public Usuario() {}

    public Usuario(String username, String password, String role) {
    this.username = username;
    this.password = password;
    this.role = role;
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

    public String getRole() { return role; }
    public void setRole(String role) {
        if (role == null || role.isBlank()) throw new IllegalArgumentException("role requerido");
        this.role = role.trim();
    }

    @Override public String toString() {
        return "Usuario{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
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
