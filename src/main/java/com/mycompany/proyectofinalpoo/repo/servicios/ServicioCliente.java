/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.HistorialCliente;

/**
 *
 * @author Bebe
 */
public class ServicioCliente {
    private final ClienteRepo clienteRepo;
    private final ReservaRepo reservaRepo;
    private final ServicioRepo servicioRepo;

    public ServicioCliente(ClienteRepo clienteRepo, ReservaRepo reservaRepo, ServicioRepo servicioRepo) {
        this.clienteRepo = Objects.requireNonNull(clienteRepo);
        this.reservaRepo = Objects.requireNonNull(reservaRepo);
        this.servicioRepo = Objects.requireNonNull(servicioRepo);
    }

    /** Crea un nuevo cliente. */
    public Cliente addCliente(String nombre, String contacto, String marca, String modelo, int anio) {
        Cliente c = new Cliente(null, nombre, contacto, marca, modelo, anio);
        clienteRepo.save(c);
        return c;
    }

    /** Actualiza un cliente existente. */
    public Cliente updateCliente(Cliente cliente) {
        if (cliente.getId() == null) throw new ValidationException("id requerido para actualizar cliente");
        clienteRepo.update(cliente);
        return cliente;
    }

    public void deleteCliente(String id) { clienteRepo.delete(id); }

    /** Obtiene historial de reservas y servicios del cliente. */
    public HistorialCliente getHistorial(String clienteId) {
        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + clienteId));
    List<Reserva> reservas = reservaRepo.findByClienteId(clienteId);
    List<Servicio> servicios = new ArrayList<>();
    for (Reserva reserva : reservas) servicioRepo.findById(reserva.getServicioId()).ifPresent(servicios::add);
    return new HistorialCliente(cliente, reservas, servicios);
    }
}
