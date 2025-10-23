package com.mycompany.proyectofinalpoo.repo;

import java.util.List;
import java.util.Optional;

import com.mycompany.proyectofinalpoo.Usuario;

public interface UsuarioRepo {
    void save(Usuario usuario);
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findAll();
    void update(Usuario usuario);
    boolean delete(String username);
}
