package com.mycompany.proyectofinalpoo.repo.servicios.dto;

import java.time.LocalDateTime;

public class ResumenReservaReporte {
    private final String reservaId;
    private final String clienteNombre;
    private final String mecanicoNombre;
    private final String servicioNombre;
    private final LocalDateTime fecha;

    public ResumenReservaReporte(String reservaId, String clienteNombre, String mecanicoNombre, String servicioNombre, LocalDateTime fecha) {
        this.reservaId = reservaId;
        this.clienteNombre = clienteNombre;
        this.mecanicoNombre = mecanicoNombre;
        this.servicioNombre = servicioNombre;
        this.fecha = fecha;
    }

    public String getReservaId() { return reservaId; }
    public String getClienteNombre() { return clienteNombre; }
    public String getMecanicoNombre() { return mecanicoNombre; }
    public String getServicioNombre() { return servicioNombre; }
    public LocalDateTime getFecha() { return fecha; }
}
