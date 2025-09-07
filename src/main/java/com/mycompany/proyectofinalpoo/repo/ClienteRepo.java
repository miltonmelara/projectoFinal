/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo;
import com.mycompany.proyectofinalpoo.Cliente;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bebe
 */
public interface ClienteRepo {
    abstract void save(Cliente c);
    Optional<Cliente> findById(String id);
    List<Cliente> findAll();
    void update(Cliente c);
    void delete(String id);
}
