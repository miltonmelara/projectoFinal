/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo;
import com.mycompany.proyectofinalpoo.Servicio;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bebe
 */
public interface ServicioRepo {
    void save(Servicio s);
    Optional<Servicio> findById(String id);
    List<Servicio> findAll();
    void update(Servicio s);
    void delete(String id);
}
