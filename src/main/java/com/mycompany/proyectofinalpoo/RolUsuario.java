package com.mycompany.proyectofinalpoo;

/**
 * Representa los diferentes roles de usuario del sistema.
 */
public enum RolUsuario {
    ADMIN,
    MECANICO;

    public static RolUsuario fromNombre(String valor) {
        if (valor == null) throw new IllegalArgumentException("rol requerido");
        String normalizado = valor.trim().toUpperCase();
        for (RolUsuario rol : values()) {
            if (rol.name().equals(normalizado)) return rol;
        }
        throw new IllegalArgumentException("rol inválido: " + valor);
    }
}
