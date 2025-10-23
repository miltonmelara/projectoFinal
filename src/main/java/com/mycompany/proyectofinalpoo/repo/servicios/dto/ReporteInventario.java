package com.mycompany.proyectofinalpoo.repo.servicios.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa un reporte de uso y costo de inventario durante un periodo.
 */
public class ReporteInventario {
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final int reservasProcesadas;
    private final int totalUnidadesUtilizadas;
    private final double costoTotal;
    private final List<DetalleParte> detalles;

    public ReporteInventario(LocalDate fechaInicio,
                             LocalDate fechaFin,
                             int reservasProcesadas,
                             int totalUnidadesUtilizadas,
                             double costoTotal,
                             List<DetalleParte> detalles) {
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "fechaInicio");
        this.fechaFin = Objects.requireNonNull(fechaFin, "fechaFin");
        this.reservasProcesadas = reservasProcesadas;
        this.totalUnidadesUtilizadas = totalUnidadesUtilizadas;
        this.costoTotal = costoTotal;
        this.detalles = detalles == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(detalles));
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public int getReservasProcesadas() { return reservasProcesadas; }
    public int getTotalUnidadesUtilizadas() { return totalUnidadesUtilizadas; }
    public double getCostoTotal() { return costoTotal; }
    public List<DetalleParte> getDetalles() { return detalles; }

    public static class DetalleParte {
        private final String parteId;
        private final String nombre;
        private final String categoria;
        private final int unidadesUtilizadas;
        private final double costoUnitario;
        private final double costoTotal;

        public DetalleParte(String parteId,
                            String nombre,
                            String categoria,
                            int unidadesUtilizadas,
                            double costoUnitario,
                            double costoTotal) {
            this.parteId = parteId;
            this.nombre = nombre;
            this.categoria = categoria;
            this.unidadesUtilizadas = unidadesUtilizadas;
            this.costoUnitario = costoUnitario;
            this.costoTotal = costoTotal;
        }

        public String getParteId() { return parteId; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public int getUnidadesUtilizadas() { return unidadesUtilizadas; }
        public double getCostoUnitario() { return costoUnitario; }
        public double getCostoTotal() { return costoTotal; }
    }
}
