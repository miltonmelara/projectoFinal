package com.mycompany.proyectofinalpoo.repo.servicios.dto;

public class DetalleConsumoReserva {
    private String parteId;
    private String nombreParte;
    private int cantidad;

    public DetalleConsumoReserva(String parteId, String nombreParte, int cantidad) {
        this.parteId = parteId;
        this.nombreParte = nombreParte;
        this.cantidad = cantidad;
    }

    public String getParteId() {
        return parteId;
    }

    public String getNombreParte() {
        return nombreParte;
    }

    public int getCantidad() {
        return cantidad;
    }
}
