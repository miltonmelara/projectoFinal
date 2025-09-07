/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo;
import com.mycompany.proyectofinalpoo.Parte;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bebe
 */
public interface ParteRepo {
    void save(Parte p);
    Optional<Parte> findById(String id);
    List<Parte> findAll();
    void update(Parte p);
    void delete(String id);
}