/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios.dto;

import java.util.ArrayList;
import java.util.List;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;

/**
 *
 * @author Bebe
 */
public class HistorialCliente {
    private Cliente cliente;
    private List<Reserva> reservas = new ArrayList<>();
    private List<Servicio> servicios = new ArrayList<>();

    public HistorialCliente() {}

    public HistorialCliente(Cliente cliente, List<Reserva> reservas, List<Servicio> servicios) {
        this.cliente = cliente;
        if (reservas != null) this.reservas = reservas;
        if (servicios != null) this.servicios = servicios;
    }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
    public List<Servicio> getServicios() { return servicios; }
    public void setServicios(List<Servicio> servicios) { this.servicios = servicios; }
}
