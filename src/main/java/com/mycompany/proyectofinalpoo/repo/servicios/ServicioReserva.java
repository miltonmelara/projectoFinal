/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;

/**
 *
 * @author Bebe
 */
public class ServicioReserva {
    private final ReservaRepo reservaRepo;
    private final ClienteRepo clienteRepo;
    private final ServicioRepo servicioRepo;
    private final ParteRepo parteRepo;

    public ServicioReserva(ReservaRepo reservaRepo, ClienteRepo clienteRepo, ServicioRepo servicioRepo, ParteRepo parteRepo) {
        this.reservaRepo = Objects.requireNonNull(reservaRepo);
        this.clienteRepo = Objects.requireNonNull(clienteRepo);
        this.servicioRepo = Objects.requireNonNull(servicioRepo);
        this.parteRepo = Objects.requireNonNull(parteRepo);
    }

    /** Crea una reserva validando existencia de cliente y servicio. */
    public Reserva createReserva(String clienteId, String servicioId, LocalDateTime fecha, String mecanico) {
        if (clienteRepo.findById(clienteId).isEmpty()) throw new NotFoundException("Cliente no existe: " + clienteId);
        if (servicioRepo.findById(servicioId).isEmpty()) throw new NotFoundException("Servicio no existe: " + servicioId);
        if (fecha == null) throw new ValidationException("fecha requerida");
    Reserva reserva = new Reserva(null, clienteId, servicioId, fecha, ReservaEstado.PROGRAMADA, mecanico);
    reservaRepo.save(reserva);
    return reserva;
    }

    /** Actualiza una reserva existente. */
    public Reserva updateReserva(Reserva reserva) {
        if (reserva.getId() == null) throw new ValidationException("id requerido para actualizar reserva");
        reservaRepo.update(reserva);
        return reserva;
    }

    public void deleteReserva(String id) { reservaRepo.delete(id); }

    /**
     * Cambia el estado de la reserva. Si pasa a FINALIZADA o ENTREGADA por primera vez,
     * deduce inventario basado en el servicio.
     */
    public Reserva changeEstado(String reservaId, ReservaEstado nuevoEstado) {
    Reserva reserva = reservaRepo.findById(reservaId).orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservaId));
    ReservaEstado estadoAnterior = reserva.getEstado();
    reserva.setEstado(nuevoEstado);
        // Bonus: deducir inventario cuando FINALIZADA o ENTREGADA
    boolean debeDeducir = (nuevoEstado == ReservaEstado.FINALIZADA || nuevoEstado == ReservaEstado.ENTREGADA)
        && !(estadoAnterior == ReservaEstado.FINALIZADA || estadoAnterior == ReservaEstado.ENTREGADA);
    if (debeDeducir) {
        Servicio servicio = servicioRepo.findById(reserva.getServicioId()).orElseThrow(() -> new NotFoundException("Servicio no encontrado: " + reserva.getServicioId()));
            Map<String,Integer> req = servicio.getPartesRequeridas();
            if (!req.isEmpty()) {
                // validar stock
                Map<String, Parte> inventario = new HashMap<>();
                for (Parte p : parteRepo.findAll()) inventario.put(p.getId(), p);
                List<String> faltantes = new ArrayList<>();
                for (Map.Entry<String,Integer> e : req.entrySet()) {
                    Parte p = inventario.get(e.getKey());
                    int need = e.getValue();
                    if (p == null || p.getCantidad() < need) faltantes.add(e.getKey() + "(req=" + need + ")");
                }
                if (!faltantes.isEmpty()) {
            reserva.setEstado(estadoAnterior); // revert
                    throw new StockException("Stock insuficiente para: " + String.join(", ", faltantes));
                }
                // deducir
                for (Map.Entry<String,Integer> e : req.entrySet()) {
                    Parte p = inventario.get(e.getKey());
                    p.reducirCantidad(e.getValue());
                    parteRepo.update(p);
                }
            }
        }
    reservaRepo.update(reserva);
    return reserva;
    }

    public List<Reserva> calendarioPorDia(LocalDate dia) { return reservaRepo.findByFecha(dia); }
}
