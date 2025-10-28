package com.mycompany.proyectofinalpoo.repo.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.CalendarioReservas;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DiaCalendario;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ResumenReservasGlobal;

public class ServicioReserva {
    private final ReservaRepo reservaRepo;
    private final ClienteRepo clienteRepo;
    private final ServicioRepo servicioRepo;
    private final ParteRepo parteRepo;
    private final UsuarioRepo usuarioRepo;
    private final com.mycompany.proyectofinalpoo.repo.ConsumoParteRepo consumoRepo;

    public ServicioReserva(com.mycompany.proyectofinalpoo.repo.ReservaRepo reservaRepo,
                           com.mycompany.proyectofinalpoo.repo.ServicioRepo servicioRepo,
                           com.mycompany.proyectofinalpoo.repo.ParteRepo parteRepo,
                           com.mycompany.proyectofinalpoo.repo.ClienteRepo clienteRepo,
                           com.mycompany.proyectofinalpoo.repo.UsuarioRepo usuarioRepo,
                           com.mycompany.proyectofinalpoo.repo.ConsumoParteRepo consumoRepo) {
        this.reservaRepo = reservaRepo;
        this.servicioRepo = servicioRepo;
        this.parteRepo = parteRepo;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
        this.consumoRepo = consumoRepo;
    }

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

    public String obtenerNombreCliente(String idCliente) {
        return clienteRepo.findById(idCliente).map(c -> c.getNombre()).orElse("[" + idCliente + "]");
    }

    public String obtenerNombreServicio(String idServicio) {
        return servicioRepo.findById(idServicio).map(s -> s.getNombre()).orElse("[" + idServicio + "]");
    }
    
    public java.util.Map<String,Integer> obtenerPartesRequeridasPorReserva(String reservaId) {
    com.mycompany.proyectofinalpoo.Reserva r = reservaRepo.findById(reservaId)
        .orElseThrow(() -> new NotFoundException("reserva no encontrada"));
    return obtenerPartesRequeridas(r.getServicioId());
}


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
            clienteRepo.findById(nuevoCliente).orElseThrow(() -> new NotFoundException("Cliente no existe: " + nuevoCliente));
            reserva.setClienteId(nuevoCliente);
        }

        if (cambios.getServicioId() != null) {
            String nuevoServicio = normalizarId(cambios.getServicioId(), "servicio");
            servicioRepo.findById(nuevoServicio).orElseThrow(() -> new NotFoundException("Servicio no existe: " + nuevoServicio));
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
        boolean debeDeducir = (nuevoEstado == ReservaEstado.FINALIZADA || nuevoEstado == ReservaEstado.ENTREGADA)
                && !(estadoAnterior == ReservaEstado.FINALIZADA || estadoAnterior == ReservaEstado.ENTREGADA);
        if (debeDeducir) {
            Servicio servicio = servicioRepo.findById(reserva.getServicioId()).orElseThrow(() -> new NotFoundException("Servicio no encontrado: " + reserva.getServicioId()));
            Map<String,Integer> req = servicio.getPartesRequeridas();
            if (!req.isEmpty()) {
                Map<String, Parte> inventario = new HashMap<>();
                for (Parte p : parteRepo.findAll()) inventario.put(p.getId(), p);
                List<String> faltantes = new ArrayList<>();
                for (Map.Entry<String,Integer> e : req.entrySet()) {
                    Parte p = inventario.get(e.getKey());
                    int need = e.getValue();
                    if (p == null || p.getCantidad() < need) faltantes.add(e.getKey() + "(req=" + need + ")");
                }
                if (!faltantes.isEmpty()) {
                    reserva.setEstado(estadoAnterior);
                    throw new StockException("Stock insuficiente para: " + String.join(", ", faltantes));
                }
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

    public List<Reserva> listarReservasAsignadasAlActual() {
        Usuario usuario = SecurityContext.requireUser();
        boolean esAdmin = ControlAcceso.tieneRol(usuario, RolUsuario.ADMIN);
        boolean esMecanico = ControlAcceso.tieneRol(usuario, RolUsuario.MECANICO);
        if (!esAdmin && !esMecanico) throw new AutorizacionException("rol no autorizado para consultar reservas asignadas");
        if (esAdmin) return reservaRepo.findAll();
        return reservaRepo.findByMecanico(usuario.getUsername());
    }

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

    public void registrarConsumo(String reservaId, String parteId, int cantidad, java.time.LocalDateTime fechaHora) {
        if (reservaId == null || reservaId.isEmpty()) throw new ValidationException("reserva requerida");
        if (parteId == null || parteId.isEmpty()) throw new ValidationException("parte requerida");
        if (cantidad <= 0) throw new ValidationException("cantidad inválida");
        Reserva r = reservaRepo.findById(reservaId).orElseThrow(() -> new NotFoundException("reserva no encontrada"));
        com.mycompany.proyectofinalpoo.ConsumoParte c = new com.mycompany.proyectofinalpoo.ConsumoParte();
        c.setReservaId(reservaId);
        c.setParteId(parteId);
        c.setCantidad(cantidad);
        c.setFechaHora(fechaHora == null ? java.time.LocalDateTime.now() : fechaHora);
        consumoRepo.save(c);
    }

    public Reserva cerrarReservaConConsumos(String reservaId, java.util.List<com.mycompany.proyectofinalpoo.ConsumoParte> consumos, ReservaEstado nuevoEstado) {
        if (reservaId == null || reservaId.isEmpty()) throw new ValidationException("reserva requerida");
        Reserva r = reservaRepo.findById(reservaId).orElseThrow(() -> new NotFoundException("reserva no encontrada"));
        if (!(r.getEstado() == ReservaEstado.PROGRAMADA || r.getEstado() == ReservaEstado.EN_PROGRESO)) {
            throw new ValidationException("estado no permite cierre");
        }
        consumoRepo.deleteByReservaId(reservaId);
        if (consumos != null && !consumos.isEmpty()) {
            for (com.mycompany.proyectofinalpoo.ConsumoParte c : consumos) {
                c.setReservaId(reservaId);
                if (c.getFechaHora() == null) c.setFechaHora(java.time.LocalDateTime.now());
            }
            consumoRepo.saveAll(consumos);
        }
        r.setEstado(nuevoEstado == null ? ReservaEstado.FINALIZADA : nuevoEstado);
        reservaRepo.update(r);
        return r;
    }

    public java.util.Map<String,Integer> obtenerPartesRequeridas(String servicioId) {
        return servicioRepo.findById(servicioId).map(s -> s.getPartesRequeridas()).orElse(java.util.Collections.emptyMap());
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
