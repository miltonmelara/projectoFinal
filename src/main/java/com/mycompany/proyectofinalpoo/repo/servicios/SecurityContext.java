package com.mycompany.proyectofinalpoo.repo.servicios;

import com.mycompany.proyectofinalpoo.Usuario;

public final class SecurityContext {
    private static final ThreadLocal<Usuario> CURRENT = new ThreadLocal<>();

    private SecurityContext() {}

    public static void setCurrentUser(Usuario usuario) {
        if (usuario == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(usuario);
        }
    }

    public static Usuario getCurrentUser() {
        return CURRENT.get();
    }

    public static Usuario requireUser() {
        Usuario usuario = getCurrentUser();
        if (usuario == null) throw new AutorizacionException("usuario no autenticado");
        return usuario;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
