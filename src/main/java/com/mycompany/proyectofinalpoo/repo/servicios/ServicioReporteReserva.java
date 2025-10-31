package com.mycompany.proyectofinalpoo.repo.servicios;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ConsumoParteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DetalleConsumoReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ReporteConsumoReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ResumenReservaReporte;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class ServicioReporteReserva {
    private final ReservaRepo reservaRepo;
    private final ClienteRepo clienteRepo;
    private final ServicioRepo servicioRepo;
    private final ConsumoParteRepo consumoParteRepo;
    private final ParteRepo parteRepo;

    public ServicioReporteReserva(ReservaRepo reservaRepo, ClienteRepo clienteRepo, ServicioRepo servicioRepo, ConsumoParteRepo consumoParteRepo, ParteRepo parteRepo) {
        this.reservaRepo = reservaRepo;
        this.clienteRepo = clienteRepo;
        this.servicioRepo = servicioRepo;
        this.consumoParteRepo = consumoParteRepo;
        this.parteRepo = parteRepo;
    }

    public ReporteConsumoReserva generarReportePorReserva(String reservaId) {
        Reserva reserva = reservaRepo.findById(reservaId).orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        Cliente cliente = clienteRepo.findById(reserva.getClienteId()).orElse(null);
        Servicio servicio = servicioRepo.findById(reserva.getServicioId()).orElse(null);
        LocalDateTime fecha = reserva.getFecha();

        var consumos = consumoParteRepo.findByReservaId(reservaId);
        List<DetalleConsumoReserva> detalles = new ArrayList<>();
        int totalUnidades = 0;

        for (var c : consumos) {
            Parte parte = parteRepo.findById(c.getParteId()).orElse(null);
            String nombreParte = parte != null ? parte.getNombre() : c.getParteId();
            int cantidad = c.getCantidad();
            detalles.add(new DetalleConsumoReserva(c.getParteId(), nombreParte, cantidad));
            totalUnidades += cantidad;
        }

        String clienteNombre = cliente != null ? cliente.getNombre() : "";
        String servicioNombre = servicio != null ? servicio.getNombre() : "";

        return new ReporteConsumoReserva(reservaId, clienteNombre, servicioNombre, fecha, detalles, totalUnidades);
    }

    public List<ResumenReservaReporte> buscarReservas(String clienteId,
                                                      String mecanico,
                                                      LocalDate desde,
                                                      LocalDate hasta) {
        List<Reserva> reservas = reservaRepo.findAll();
        List<ResumenReservaReporte> resultados = new ArrayList<>();
        for (Reserva r : reservas) {
            if (clienteId != null && !clienteId.isBlank() && !clienteId.equalsIgnoreCase(r.getClienteId())) continue;
            if (mecanico != null && !mecanico.isBlank()) {
                String mec = r.getMecanicoAsignado();
                if (mec == null || !mec.equalsIgnoreCase(mecanico)) continue;
            }
            LocalDate fecha = r.getFecha().toLocalDate();
            if (desde != null && fecha.isBefore(desde)) continue;
            if (hasta != null && fecha.isAfter(hasta)) continue;

            Cliente cliente = clienteRepo.findById(r.getClienteId()).orElse(null);
            Servicio servicio = servicioRepo.findById(r.getServicioId()).orElse(null);
            String clienteNombre = cliente != null ? cliente.getNombre() : r.getClienteId();
            String servicioNombre = servicio != null ? servicio.getNombre() : r.getServicioId();
            String mecanicoNombre = r.getMecanicoAsignado();
            resultados.add(new ResumenReservaReporte(r.getId(), clienteNombre, mecanicoNombre, servicioNombre, r.getFecha()));
        }
        resultados.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) return 0;
            if (a.getFecha() == null) return 1;
            if (b.getFecha() == null) return -1;
            return b.getFecha().compareTo(a.getFecha());
        });
        return resultados;
    }
}
