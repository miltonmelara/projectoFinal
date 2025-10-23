/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.CalendarioReservas;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DiaCalendario;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ResumenReservasGlobal;

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
        requireAdmin();
        if (clienteRepo.findById(clienteId).isEmpty()) throw new NotFoundException("Cliente no existe: " + clienteId);
        if (servicioRepo.findById(servicioId).isEmpty()) throw new NotFoundException("Servicio no existe: " + servicioId);
        if (fecha == null) throw new ValidationException("fecha requerida");
    Reserva reserva = new Reserva(null, clienteId, servicioId, fecha, ReservaEstado.PROGRAMADA, mecanico);
    reservaRepo.save(reserva);
    return reserva;
    }

    public static class ActualizarReservaRequest {
        private final String clienteId;
        private final String servicioId;
        private final LocalDateTime fecha;
        private final String mecanico;

        public ActualizarReservaRequest(String clienteId, String servicioId, LocalDateTime fecha, String mecanico) {
            this.clienteId = clienteId;
            this.servicioId = servicioId;
            this.fecha = fecha;
            this.mecanico = mecanico;
        }

        public String getClienteId() { return clienteId; }
        public String getServicioId() { return servicioId; }
        public LocalDateTime getFecha() { return fecha; }
        public String getMecanico() { return mecanico; }
    }

    /** Actualiza los datos básicos de una reserva existente. */
    public Reserva updateReserva(String reservaId, ActualizarReservaRequest cambios) {
        requireAdmin();
        if (reservaId == null || reservaId.trim().isEmpty()) throw new ValidationException("id de reserva requerido");
        if (cambios == null) throw new ValidationException("datos de actualización requeridos");

        Reserva reserva = reservaRepo.findById(reservaId.trim())
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservaId));

        if (reserva.getEstado() == ReservaEstado.FINALIZADA || reserva.getEstado() == ReservaEstado.ENTREGADA) {
            throw new ValidationException("no se puede actualizar una reserva finalizada o entregada");
        }

        if (cambios.getClienteId() != null) {
            String nuevoCliente = normalizarId(cambios.getClienteId(), "cliente");
            clienteRepo.findById(nuevoCliente)
                    .orElseThrow(() -> new NotFoundException("Cliente no existe: " + nuevoCliente));
            reserva.setClienteId(nuevoCliente);
        }

        if (cambios.getServicioId() != null) {
            String nuevoServicio = normalizarId(cambios.getServicioId(), "servicio");
            servicioRepo.findById(nuevoServicio)
                    .orElseThrow(() -> new NotFoundException("Servicio no existe: " + nuevoServicio));
            reserva.setServicioId(nuevoServicio);
        }

        if (cambios.getFecha() != null) {
            reserva.setFecha(cambios.getFecha());
        }

        if (cambios.getMecanico() != null) {
            String mecanico = cambios.getMecanico().trim();
            if (mecanico.isEmpty()) throw new ValidationException("mecánico no puede ser vacío");
            reserva.setMecanicoAsignado(mecanico);
        }

        reservaRepo.update(reserva);
        return reserva;
    }

    public void deleteReserva(String id) {
        requireAdmin();
        reservaRepo.delete(id);
    }

    /**
     * Cambia el estado de la reserva. Si pasa a FINALIZADA o ENTREGADA por primera vez,
     * deduce inventario basado en el servicio.
     */
    public Reserva changeEstado(String reservaId, ReservaEstado nuevoEstado) {
        Usuario usuario = SecurityContext.requireUser();
        boolean esAdmin = ControlAcceso.tieneRol(usuario, RolUsuario.ADMIN);
        boolean esMecanico = ControlAcceso.tieneRol(usuario, RolUsuario.MECANICO);
        if (!esAdmin && !esMecanico) throw new AutorizacionException("permiso denegado para cambiar estado de reserva");

        Reserva reserva = reservaRepo.findById(reservaId).orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservaId));

        if (esMecanico) {
            String asignado = reserva.getMecanicoAsignado();
            if (asignado == null || !asignado.equalsIgnoreCase(usuario.getUsername())) {
                throw new AutorizacionException("reservas asignadas a otro mecánico");
            }
            if (nuevoEstado == ReservaEstado.PROGRAMADA) {
                throw new ValidationException("el mecánico no puede regresar la reserva a PROGRAMADA");
            }
        }

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

    public List<Reserva> calendarioPorDia(LocalDate dia) {
        requireAdmin();
        return reservaRepo.findByFecha(dia);
    }

    /** Lista las reservas asignadas al usuario autenticado cuando es mecánico. */
    public List<Reserva> listarReservasAsignadasAlActual() {
        Usuario usuario = SecurityContext.requireUser();
        boolean esAdmin = ControlAcceso.tieneRol(usuario, RolUsuario.ADMIN);
        boolean esMecanico = ControlAcceso.tieneRol(usuario, RolUsuario.MECANICO);
        if (!esAdmin && !esMecanico) throw new AutorizacionException("rol no autorizado para consultar reservas asignadas");
        if (esAdmin) {
            return reservaRepo.findAll();
        }
        return reservaRepo.findByMecanico(usuario.getUsername());
    }

    /** Permite a un administrador consultar reservas asignadas a un mecánico específico. */
    public List<Reserva> listarReservasPorMecanico(String mecanicoAsignado) {
        requireAdmin();
        return reservaRepo.findByMecanico(mecanicoAsignado);
    }

    public DiaCalendario obtenerDiaCalendario(LocalDate fecha, String mecanico, ReservaEstado estado) {
        requireAdmin();
        if (fecha == null) throw new ValidationException("fecha requerida");
        String filtroMecanico = normalizarFiltroMecanico(mecanico);
        List<Reserva> reservas = filtrarReservas(reservaRepo.findByFecha(fecha), filtroMecanico, estado);
        return construirDiaCalendario(fecha, reservas);
    }

    public CalendarioReservas generarCalendario(LocalDate fechaInicio, LocalDate fechaFin, String mecanico, ReservaEstado estado) {
        requireAdmin();
        if (fechaInicio == null || fechaFin == null) throw new ValidationException("rango requerido");
        LocalDate inicio = fechaInicio;
        LocalDate fin = fechaFin;
        if (inicio.isAfter(fin)) {
            inicio = fechaFin;
            fin = fechaInicio;
        }
        String filtroMecanico = normalizarFiltroMecanico(mecanico);
        List<Reserva> reservas = reservaRepo.findEntreFechas(inicio, fin);
        Map<LocalDate, List<Reserva>> agrupadas = agruparPorFecha(reservas);
        List<DiaCalendario> dias = new ArrayList<>();
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fin)) {
            List<Reserva> delDia = agrupadas.getOrDefault(cursor, Collections.emptyList());
            List<Reserva> filtradas = filtrarReservas(delDia, filtroMecanico, estado);
            dias.add(construirDiaCalendario(cursor, filtradas));
            cursor = cursor.plusDays(1);
        }
        int totalReservas = 0;
        int totalProgramadas = 0;
        int totalEnProgreso = 0;
        int totalFinalizadas = 0;
        int totalEntregadas = 0;
        for (DiaCalendario dia : dias) {
            totalReservas += dia.obtenerReservas().size();
            totalProgramadas += dia.obtenerTotalProgramadas();
            totalEnProgreso += dia.obtenerTotalEnProgreso();
            totalFinalizadas += dia.obtenerTotalFinalizadas();
            totalEntregadas += dia.obtenerTotalEntregadas();
        }
        return new CalendarioReservas(inicio, fin, dias, totalReservas, totalProgramadas, totalEnProgreso, totalFinalizadas, totalEntregadas);
    }

    public ResumenReservasGlobal obtenerResumenGlobal() {
        requireAdmin();
        List<Reserva> reservas = reservaRepo.findAll();
        int total = reservas.size();
        int programadas = 0;
        int enProgreso = 0;
        int finalizadas = 0;
        int entregadas = 0;
        for (Reserva reserva : reservas) {
            ReservaEstado estado = reserva.getEstado();
            if (estado == ReservaEstado.PROGRAMADA) programadas++;
            else if (estado == ReservaEstado.EN_PROGRESO) enProgreso++;
            else if (estado == ReservaEstado.FINALIZADA) finalizadas++;
            else if (estado == ReservaEstado.ENTREGADA) entregadas++;
        }
        return new ResumenReservasGlobal(total, programadas, enProgreso, finalizadas, entregadas);
    }

    private Map<LocalDate, List<Reserva>> agruparPorFecha(List<Reserva> reservas) {
        Map<LocalDate, List<Reserva>> mapa = new HashMap<>();
        for (Reserva reserva : reservas) {
            LocalDate fecha = reserva.getFecha().toLocalDate();
            mapa.computeIfAbsent(fecha, f -> new ArrayList<>()).add(reserva);
        }
        return mapa;
    }

    private List<Reserva> filtrarReservas(List<Reserva> reservas, String mecanico, ReservaEstado estado) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (estado != null && reserva.getEstado() != estado) continue;
            if (!coincideMecanico(reserva.getMecanicoAsignado(), mecanico)) continue;
            resultado.add(reserva);
        }
        List<Reserva> ordenadas = new ArrayList<>(resultado);
        ordenadas.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
        return ordenadas;
    }

    private boolean coincideMecanico(String valorActual, String filtro) {
        if (filtro == null) return true;
        if (valorActual == null) return false;
        return valorActual.equalsIgnoreCase(filtro);
    }

    private String normalizarFiltroMecanico(String mecanico) {
        if (mecanico == null) return null;
        String valor = mecanico.trim();
        if (valor.isEmpty()) return null;
        return valor;
    }

    private DiaCalendario construirDiaCalendario(LocalDate fecha, List<Reserva> reservas) {
        int programadas = 0;
        int enProgreso = 0;
        int finalizadas = 0;
        int entregadas = 0;
        for (Reserva reserva : reservas) {
            ReservaEstado estado = reserva.getEstado();
            if (estado == ReservaEstado.PROGRAMADA) programadas++;
            else if (estado == ReservaEstado.EN_PROGRESO) enProgreso++;
            else if (estado == ReservaEstado.FINALIZADA) finalizadas++;
            else if (estado == ReservaEstado.ENTREGADA) entregadas++;
        }
        return new DiaCalendario(fecha, reservas, programadas, enProgreso, finalizadas, entregadas);
    }

    private String normalizarId(String valor, String campo) {
        String result = (valor == null) ? null : valor.trim();
        if (result == null || result.isEmpty()) throw new ValidationException(campo + " requerido");
        return result;
    }

    private void requireAdmin() {
        Usuario usuario = SecurityContext.requireUser();
        ControlAcceso.requireRol(usuario, RolUsuario.ADMIN);
    }
}
