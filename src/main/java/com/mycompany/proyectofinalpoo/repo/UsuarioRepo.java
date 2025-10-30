package com.mycompany.proyectofinalpoo.repo;

import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.RolUsuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepo {
    void save(Usuario u);                
    void update(Usuario u);               
    boolean delete(String username);     

    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Usuario> findAll();
    List<Usuario> findByRole(RolUsuario role);
}
