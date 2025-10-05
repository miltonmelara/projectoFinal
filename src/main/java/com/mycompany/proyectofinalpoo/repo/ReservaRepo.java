/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo;
import com.mycompany.proyectofinalpoo.Reserva;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bebe
 */
public interface ReservaRepo {
    void save(Reserva r);
    Optional<Reserva> findById(String id);
    List<Reserva> findAll();
    void update(Reserva r);
    void delete(String id);
    List<Reserva> findByClienteId(String clienteId);
    List<Reserva> findByFecha(LocalDate day);
    List<Reserva> findEntreFechas(LocalDate inicio, LocalDate fin);
}
