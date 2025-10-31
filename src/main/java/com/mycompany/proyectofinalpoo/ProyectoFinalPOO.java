/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.proyectofinalpoo;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo;

public class ProyectoFinalPOO {
    public static void main(String[] args) {
        Path dataDir = Path.of("data");
        ClienteRepo clienteRepo = new ClienteFileRepo(dataDir);
        ParteRepo parteRepo = new ParteFileRepo(dataDir);
        ServicioRepo servicioRepo = new ServicioFileRepo(dataDir);
        ReservaRepo reservaRepo = new ReservaFileRepo(dataDir);
        UsuarioRepo usuarioRepo = new UsuarioFileRepo(dataDir);

        seedDatos(clienteRepo, parteRepo, servicioRepo, reservaRepo);
        seedUsuarios(usuarioRepo);
    }

    private static void seedDatos(ClienteRepo cr, ParteRepo pr, ServicioRepo sr, ReservaRepo rr) {
        List<Cliente> clientes = ensureClientes(cr);
        ensurePartes(pr);
        List<Servicio> servicios = ensureServicios(sr, pr);
        ensureReservas(rr, clientes, servicios);
    }

    private static List<Cliente> ensureClientes(ClienteRepo repo) {
        List<Cliente> clientes = repo.findAll();
        if (clientes.isEmpty()) {
            repo.save(new Cliente(null, "Ana Pérez", "555111", "Toyota", "Corolla", 2019));
            repo.save(new Cliente(null, "Luis Gómez", "555222", "Honda", "Civic", 2020));
            repo.save(new Cliente(null, "Marta Díaz", "555333", "Ford", "Focus", 2018));
            clientes = repo.findAll();
        }
        return clientes;
    }

    private static void ensurePartes(ParteRepo repo) {
        final Object[][] partesBase = {
                {"FILTRO-ACEITE", "Filtros", 25, 8.5, 5.0},
                {"ACEITE-5W30", "Aceites", 60, 12.0, 8.0},
                {"BUJIA-IRIDIUM", "Encendido", 40, 15.0, 9.0},
                {"PASTILLA-FRENO", "Frenos", 35, 20.0, 12.0},
                {"FILTRO-AIRE", "Filtros", 30, 10.0, 6.0}
        };

        Map<String, Parte> existentes = new HashMap<>();
        for (Parte p : repo.findAll()) {
            existentes.put(p.getNombre().toLowerCase(), p);
        }

        for (Object[] base : partesBase) {
            String nombre = (String) base[0];
            String categoria = (String) base[1];
            int cantidad = (Integer) base[2];
            double precio = (Double) base[3];
            double costo = (Double) base[4];

            Parte existente = existentes.get(nombre.toLowerCase());
            if (existente == null) {
                Parte nueva = new Parte(null, nombre, categoria, cantidad, precio, costo);
                repo.save(nueva);
                existentes.put(nombre.toLowerCase(), nueva);
            } else {
                boolean cambio = false;
                if (!categoria.equalsIgnoreCase(existente.getCategoria())) {
                    existente.setCategoria(categoria);
                    cambio = true;
                }
                if (existente.getCantidad() < cantidad) {
                    existente.setCantidad(cantidad);
                    cambio = true;
                }
                if (Double.compare(existente.getPrecioUnitario(), precio) != 0) {
                    existente.setPrecioUnitario(precio);
                    cambio = true;
                }
                if (Double.compare(existente.getCosto(), costo) != 0) {
                    existente.setCosto(costo);
                    cambio = true;
                }
                if (cambio) {
                    repo.update(existente);
                }
            }
        }
    }

    private static List<Servicio> ensureServicios(ServicioRepo servicioRepo, ParteRepo parteRepo) {
        Map<String, Servicio> serviciosPorNombre = new HashMap<>();
        for (Servicio s : servicioRepo.findAll()) {
            serviciosPorNombre.put(s.getNombre().toLowerCase(), s);
        }

        Map<String, Parte> partesPorNombre = new HashMap<>();
        for (Parte parte : parteRepo.findAll()) {
            partesPorNombre.put(parte.getNombre().toLowerCase(), parte);
        }

        Map<String,Integer> partesCambioAceite = new LinkedHashMap<>();
        partesCambioAceite.put("FILTRO-ACEITE", 1);
        partesCambioAceite.put("ACEITE-5W30", 4);

        Map<String,Integer> partesAfinacion = new LinkedHashMap<>();
        partesAfinacion.put("BUJIA-IRIDIUM", 4);
        partesAfinacion.put("FILTRO-AIRE", 1);

        ensureServicio(servicioRepo, parteRepo, serviciosPorNombre, partesPorNombre,
                "Cambio de aceite", 60, 60.0, partesCambioAceite);
        ensureServicio(servicioRepo, parteRepo, serviciosPorNombre, partesPorNombre,
                "Afinación", 120, 150.0, partesAfinacion);

        repararServiciosSinPartes(servicioRepo, parteRepo);
        return servicioRepo.findAll();
    }

    private static void ensureServicio(ServicioRepo servicioRepo,
                                       ParteRepo parteRepo,
                                       Map<String, Servicio> serviciosPorNombre,
                                       Map<String, Parte> partesPorNombre,
                                       String nombre,
                                       int duracionMin,
                                       double precio,
                                       Map<String,Integer> requeridasPorNombre) {

        Map<String,Integer> partesPorId = new LinkedHashMap<>();
        double costoTotal = 0.0;
        for (Map.Entry<String,Integer> entry : requeridasPorNombre.entrySet()) {
            String nombreParte = entry.getKey();
            int cantidad = entry.getValue() == null ? 0 : entry.getValue();
            Parte parte = partesPorNombre.get(nombreParte.toLowerCase());
            if (parte == null) {
                parte = new Parte(null, nombreParte, "Generales", Math.max(10, cantidad * 5), 0.0, 0.0);
                parteRepo.save(parte);
                partesPorNombre.put(nombreParte.toLowerCase(), parte);
            }
            partesPorId.put(parte.getId(), cantidad);
            costoTotal += parte.getCosto() * cantidad;
        }

        Servicio existente = serviciosPorNombre.get(nombre.toLowerCase());
        if (existente == null) {
            Servicio nuevo = new Servicio(null, nombre, duracionMin, costoTotal, precio, partesPorId);
            servicioRepo.save(nuevo);
            serviciosPorNombre.put(nombre.toLowerCase(), nuevo);
        } else {
            existente.setDuracionMin(duracionMin);
            existente.setPrecio(precio);
            existente.setCostoTotal(costoTotal);
            existente.setPartesRequeridas(partesPorId);
            servicioRepo.update(existente);
        }
    }

    private static void repararServiciosSinPartes(ServicioRepo servicioRepo, ParteRepo parteRepo) {
        Map<String, Parte> partesPorId = new HashMap<>();
        Map<String, Parte> partesPorNombre = new HashMap<>();
        for (Parte parte : parteRepo.findAll()) {
            partesPorId.put(parte.getId(), parte);
            partesPorNombre.put(parte.getNombre().toLowerCase(), parte);
        }

        for (Servicio servicio : servicioRepo.findAll()) {
            Map<String,Integer> originales = servicio.getPartesRequeridas();
            Map<String,Integer> reparadas = new LinkedHashMap<>();
            boolean cambio = false;
            double costoTotal = 0.0;

            if (originales.isEmpty()) {
                Parte kit = ensureKitParte(servicio, parteRepo, partesPorId, partesPorNombre);
                reparadas.put(kit.getId(), 1);
                costoTotal += kit.getCosto();
                cambio = true;
            } else {
                for (Map.Entry<String,Integer> entry : originales.entrySet()) {
                    String clave = entry.getKey();
                    int cantidad = entry.getValue() == null ? 0 : entry.getValue();
                    if (cantidad <= 0) {
                        cantidad = 1;
                        cambio = true;
                    }

                    Parte parte = partesPorId.get(clave);
                    if (parte == null) {
                        parte = partesPorNombre.get(clave.toLowerCase());
                        if (parte != null) cambio = true;
                    }
                    if (parte == null) {
                        parte = new Parte(null, clave, "Generales", Math.max(10, cantidad * 5), 0.0, 0.0);
                        parteRepo.save(parte);
                        partesPorId.put(parte.getId(), parte);
                        partesPorNombre.put(parte.getNombre().toLowerCase(), parte);
                        cambio = true;
                    }
                    reparadas.put(parte.getId(), cantidad);
                    costoTotal += parte.getCosto() * cantidad;
                }
            }

            if (cambio) {
                servicio.setPartesRequeridas(reparadas);
                servicio.setCostoTotal(costoTotal);
                servicioRepo.update(servicio);
            }
        }
    }

    private static Parte ensureKitParte(Servicio servicio,
                                        ParteRepo parteRepo,
                                        Map<String, Parte> partesPorId,
                                        Map<String, Parte> partesPorNombre) {
        String baseNombre = "KIT-" + servicio.getNombre()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("-+", "-");
        Parte existente = partesPorNombre.get(baseNombre.toLowerCase());
        if (existente == null) {
            existente = new Parte(null, baseNombre, "Servicios", 20, 0.0, 0.0);
            parteRepo.save(existente);
            partesPorId.put(existente.getId(), existente);
            partesPorNombre.put(existente.getNombre().toLowerCase(), existente);
        }
        return existente;
    }

    private static void ensureReservas(ReservaRepo reservaRepo, List<Cliente> clientes, List<Servicio> servicios) {
        if (!reservaRepo.findAll().isEmpty()) return;
        if (clientes.isEmpty() || servicios.isEmpty()) return;
        reservaRepo.save(new Reserva(null, clientes.get(0).getId(), servicios.get(0).getId(),
                LocalDateTime.now().plusDays(1), ReservaEstado.PROGRAMADA, "Carlos"));
        reservaRepo.save(new Reserva(null, clientes.get(Math.min(1, clientes.size() - 1)).getId(), servicios.get(servicios.size() > 1 ? 1 : 0).getId(),
                LocalDateTime.now().minusDays(1), ReservaEstado.FINALIZADA, "Ana"));
        reservaRepo.save(new Reserva(null, clientes.get(Math.min(2, clientes.size()-1)).getId(), servicios.get(0).getId(),
                LocalDateTime.now().plusDays(2), ReservaEstado.EN_PROGRESO, "Luis"));
    }

    private static void seedUsuarios(UsuarioRepo usuarioRepo) {
    com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios servicioUsuarios =
        new com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios(usuarioRepo);

    servicioUsuarios.seedAdminDefault("admin", "admin123");
    servicioUsuarios.seedUsuario("Carlos Rodriguez", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
    servicioUsuarios.seedUsuario("Maria Gonzales", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
    servicioUsuarios.seedUsuario("Luis Martinez", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
    servicioUsuarios.seedUsuario("Ana Perez", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
}

}
