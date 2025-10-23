package com.mycompany.proyectofinalpoo.repo.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ReporteInventario;

/**
 * Servicio para generar reportes de uso y costo de inventario en un periodo determinado.
 */
public class ServicioReporteInventario {
    private final ReservaRepo reservaRepo;
    private final ServicioRepo servicioRepo;
    private final ParteRepo parteRepo;

    public ServicioReporteInventario(ReservaRepo reservaRepo, ServicioRepo servicioRepo, ParteRepo parteRepo) {
        this.reservaRepo = Objects.requireNonNull(reservaRepo, "reservaRepo");
        this.servicioRepo = Objects.requireNonNull(servicioRepo, "servicioRepo");
        this.parteRepo = Objects.requireNonNull(parteRepo, "parteRepo");
    }

    /**
     * Genera un reporte con el total de partes utilizadas y costo asociado entre dos fechas (inclusive).
     * Solo se consideran las reservas FINALIZADAS o ENTREGADAS porque son las que disparan la deducción de inventario.
     *
     * @throws ValidationException si alguna de las fechas es nula
     */
    public ReporteInventario generarReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) throw new ValidationException("rango de fechas requerido");

        LocalDate inicio = fechaInicio;
        LocalDate fin = fechaFin;
        if (inicio.isAfter(fin)) {
            inicio = fechaFin;
            fin = fechaInicio;
        }

        List<Reserva> reservas = reservaRepo.findEntreFechas(inicio, fin);
        Map<String, Servicio> cacheServicios = new HashMap<>();
        Map<String, Integer> consumoPorParte = new HashMap<>();
        int reservasContabilizadas = 0;

        for (Reserva reserva : reservas) {
            if (!esReservaConsumida(reserva)) continue;
            Servicio servicio = cacheServicios.computeIfAbsent(reserva.getServicioId(), this::cargarServicio);
            if (servicio == null) continue;
            reservasContabilizadas++;
            for (Map.Entry<String, Integer> entry : servicio.getPartesRequeridas().entrySet()) {
                String parteId = entry.getKey();
                Integer cantidad = entry.getValue();
                if (parteId == null || cantidad == null || cantidad <= 0) continue;
                consumoPorParte.merge(parteId, cantidad, Integer::sum);
            }
        }

        if (consumoPorParte.isEmpty()) {
            return new ReporteInventario(inicio, fin, reservasContabilizadas, 0, 0.0, Collections.emptyList());
        }

        Map<String, Parte> inventario = cargarInventarioActual();
        List<ReporteInventario.DetalleParte> detalles = new ArrayList<>();
        int totalUnidades = 0;
        double costoTotal = 0.0;

        for (Map.Entry<String, Integer> consumo : consumoPorParte.entrySet()) {
            String parteId = consumo.getKey();
            int unidades = consumo.getValue();
            totalUnidades += unidades;

            Parte parte = inventario.get(parteId);
            String nombre = parte != null ? parte.getNombre() : parteId;
            String categoria = parte != null ? parte.getCategoria() : "DESCONOCIDA";
            double costoUnitario = parte != null ? parte.getCosto() : 0.0;
            double costoParte = costoUnitario * unidades;
            costoTotal += costoParte;

            detalles.add(new ReporteInventario.DetalleParte(
                    parteId,
                    nombre,
                    categoria,
                    unidades,
                    costoUnitario,
                    costoParte
            ));
        }

        detalles.sort(Comparator.comparing(ReporteInventario.DetalleParte::getNombre, String.CASE_INSENSITIVE_ORDER));

        return new ReporteInventario(inicio, fin, reservasContabilizadas, totalUnidades, costoTotal, detalles);
    }

    private boolean esReservaConsumida(Reserva reserva) {
        if (reserva == null || reserva.getEstado() == null) return false;
        ReservaEstado estado = reserva.getEstado();
        return estado == ReservaEstado.FINALIZADA || estado == ReservaEstado.ENTREGADA;
    }

    private Servicio cargarServicio(String servicioId) {
        if (servicioId == null) return null;
        return servicioRepo.findById(servicioId).orElse(null);
    }

    private Map<String, Parte> cargarInventarioActual() {
        Map<String, Parte> inventario = new HashMap<>();
        for (Parte parte : parteRepo.findAll()) {
            if (parte.getId() != null) inventario.put(parte.getId(), parte);
        }
        return inventario;
    }
}
