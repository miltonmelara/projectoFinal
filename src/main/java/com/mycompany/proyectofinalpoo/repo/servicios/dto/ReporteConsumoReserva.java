package com.mycompany.proyectofinalpoo.repo.servicios.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReporteConsumoReserva {
    private String reservaId;
    private String clienteNombre;
    private String servicioNombre;
    private LocalDateTime fechaReserva;
    private List<DetalleConsumoReserva> detalles;
    private int totalUnidades;

    public ReporteConsumoReserva(String reservaId, String clienteNombre, String servicioNombre, LocalDateTime fechaReserva, List<DetalleConsumoReserva> detalles, int totalUnidades) {
        this.reservaId = reservaId;
        this.clienteNombre = clienteNombre;
        this.servicioNombre = servicioNombre;
        this.fechaReserva = fechaReserva;
        this.detalles = detalles;
        this.totalUnidades = totalUnidades;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public List<DetalleConsumoReserva> getDetalles() {
        return detalles;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }
}
