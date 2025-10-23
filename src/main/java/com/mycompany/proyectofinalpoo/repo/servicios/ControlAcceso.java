package com.mycompany.proyectofinalpoo.repo.servicios;

import java.util.Arrays;

import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;

public final class ControlAcceso {
    private ControlAcceso() {}

    public static void requireRol(Usuario usuario, RolUsuario... rolesPermitidos) {
        if (usuario == null) throw new AutorizacionException("usuario no autenticado");
        if (rolesPermitidos == null || rolesPermitidos.length == 0) return;
        boolean autorizado = Arrays.stream(rolesPermitidos).anyMatch(rol -> usuario.getRol() == rol);
        if (!autorizado) throw new AutorizacionException("permiso denegado para el usuario: " + usuario.getUsername());
    }

    public static boolean tieneRol(Usuario usuario, RolUsuario rol) {
        if (usuario == null || rol == null) return false;
        return usuario.getRol() == rol;
    }
}
