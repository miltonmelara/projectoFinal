/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Bebe
 */
public class Reserva {
    private String id;
    private String clienteId;
    private String servicioId;
    private LocalDateTime fecha;
    private ReservaEstado estado = ReservaEstado.PROGRAMADA;
    private String mecanicoAsignado;

    public Reserva() {}

    public Reserva(String id, String clienteId, String servicioId, LocalDateTime fecha, ReservaEstado estado, String mecanicoAsignado) {
    this.id = id;
    this.clienteId = clienteId;
    this.servicioId = servicioId;
    this.fecha = fecha;
    this.estado = (estado == null ? ReservaEstado.PROGRAMADA : estado);
    this.mecanicoAsignado = mecanicoAsignado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = (id != null && id.isBlank()) ? null : id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) throw new IllegalArgumentException("clienteId requerido");
        this.clienteId = clienteId.trim();
    }
    public String getServicioId() { return servicioId; }
    public void setServicioId(String servicioId) {
        if (servicioId == null || servicioId.isBlank()) throw new IllegalArgumentException("servicioId requerido");
        this.servicioId = servicioId.trim();
    }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) throw new IllegalArgumentException("fecha requerida");
        this.fecha = fecha;
    }
    public ReservaEstado getEstado() { return estado; }
    public void setEstado(ReservaEstado estado) {
        if (estado == null) throw new IllegalArgumentException("estado requerido");
        this.estado = estado;
    }
    public String getMecanicoAsignado() { return mecanicoAsignado; }
    public void setMecanicoAsignado(String mecanicoAsignado) {
        if (mecanicoAsignado == null || mecanicoAsignado.isBlank()) throw new IllegalArgumentException("mecánico requerido");
        this.mecanicoAsignado = mecanicoAsignado.trim();
    }

    @Override public String toString() {
        return "Reserva{" +
                "id='" + id + '\'' +
                ", clienteId='" + clienteId + '\'' +
                ", servicioId='" + servicioId + '\'' +
                ", fecha=" + fecha +
                ", estado=" + estado +
                ", mecanicoAsignado='" + mecanicoAsignado + '\'' +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva)) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }
}
