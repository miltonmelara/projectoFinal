/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo;
import java.util.List;
import java.util.Optional;

import com.mycompany.proyectofinalpoo.Cliente;

/**
 *
 * @author Bebe
 */
public interface ClienteRepo {
    abstract void save(Cliente c);
    Optional<Cliente> findById(String id);
    Optional<Cliente> findByNombre(String nombre);
    List<Cliente> findAll();
    boolean existsByContacto(String contacto);
    void update(Cliente c);
    void delete(String id);
}
