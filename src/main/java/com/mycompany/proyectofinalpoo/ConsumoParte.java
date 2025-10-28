package com.mycompany.proyectofinalpoo;

import java.time.LocalDateTime;

public class ConsumoParte {
    private String id;
    private String reservaId;
    private String parteId;
    private int cantidad;
    private LocalDateTime fechaHora;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReservaId() { return reservaId; }
    public void setReservaId(String reservaId) { this.reservaId = reservaId; }
    public String getParteId() { return parteId; }
    public void setParteId(String parteId) { this.parteId = parteId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
