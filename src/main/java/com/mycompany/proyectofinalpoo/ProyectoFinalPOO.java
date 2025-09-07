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
import java.util.Scanner;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioCliente;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioInventario;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.HistorialCliente;

/**
 *
 * @author Bebe
 */
public class ProyectoFinalPOO {
    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        Path dataDir = Path.of("data");
        ClienteRepo clienteRepo = new ClienteFileRepo(dataDir);
        ParteRepo parteRepo = new ParteFileRepo(dataDir);
        ServicioRepo servicioRepo = new ServicioFileRepo(dataDir);
        ReservaRepo reservaRepo = new ReservaFileRepo(dataDir);

        seedIfEmpty(clienteRepo, parteRepo, servicioRepo, reservaRepo);

        ServicioInventario inv = new ServicioInventario(parteRepo);
        ServicioReserva reservas = new ServicioReserva(reservaRepo, clienteRepo, servicioRepo, parteRepo);
        ServicioCliente clientes = new ServicioCliente(clienteRepo, reservaRepo, servicioRepo);

        while (true) {
            System.out.println("=== Taller CLI ===");
            System.out.println("1) Agregar parte al inventario");
            System.out.println("2) Crear reserva");
            System.out.println("3) Cambiar estado de reserva");
            System.out.println("4) Mostrar historial de cliente");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String op = SC.nextLine().trim();
            try {
                switch (op) {
                    case "1": AddParte(inv); break;
                    case "2": CrearReserva(reservas, clienteRepo, servicioRepo); break;
                    case "3": CambiarEstado(reservas, reservaRepo); break;
                    case "4": Historial(clientes, clienteRepo); break;
                    case "0": System.out.println("Adiós"); return;
                    default: System.out.println("Opción inválida");
                }
            } catch (RuntimeException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
            System.out.println();
        }
    }

    private static void seedIfEmpty(ClienteRepo cr, ParteRepo pr, ServicioRepo sr, ReservaRepo rr) {
        if (cr.findAll().isEmpty()) {
            cr.save(new Cliente(null, "Ana Pérez", "555-111", "Toyota", "Corolla", 2018));
            cr.save(new Cliente(null, "Luis Gómez", "555-222", "Honda", "Civic", 2019));
            cr.save(new Cliente(null, "Marta Díaz", "555-333", "Ford", "Focus", 2016));
        }
        if (pr.findAll().isEmpty()) {
            pr.save(new Parte(null, "FILTRO-ACEITE", "Filtros", 10, 8.5, 5.0));
            pr.save(new Parte(null, "ACEITE-5W30", "Aceites", 40, 12.0, 8.0));
            pr.save(new Parte(null, "BUJIA-IRIDIUM", "Encendido", 25, 15.0, 9.0));
            pr.save(new Parte(null, "PASTILLA-FRENO", "Frenos", 30, 20.0, 12.0));
            pr.save(new Parte(null, "FILTRO-AIRE", "Filtros", 15, 10.0, 6.0));
        }
        if (sr.findAll().isEmpty()) {
            // map by nombre -> id
            Map<String,String> idByNombre = new HashMap<>();
            for (Parte p : pr.findAll()) idByNombre.put(p.getNombre(), p.getId());

            Map<String,Integer> cambioAceite = new LinkedHashMap<>();
            cambioAceite.put(idByNombre.getOrDefault("FILTRO-ACEITE", "FILTRO-ACEITE"), 1);
            cambioAceite.put(idByNombre.getOrDefault("ACEITE-5W30", "ACEITE-5W30"), 4);
            sr.save(new Servicio(null, "Cambio de aceite", 60, 0, 60.0, cambioAceite));

            Map<String,Integer> afinacion = new LinkedHashMap<>();
            afinacion.put(idByNombre.getOrDefault("BUJIA-IRIDIUM", "BUJIA-IRIDIUM"), 4);
            afinacion.put(idByNombre.getOrDefault("FILTRO-AIRE", "FILTRO-AIRE"), 1);
            sr.save(new Servicio(null, "Afinación", 120, 0, 150.0, afinacion));
        }
        if (rr.findAll().isEmpty()) {
            List<Cliente> cs = cr.findAll();
            List<Servicio> ss = sr.findAll();
            if (!cs.isEmpty() && !ss.isEmpty()) {
                rr.save(new Reserva(null, cs.get(0).getId(), ss.get(0).getId(), LocalDateTime.now().plusDays(1), ReservaEstado.PROGRAMADA, "Carlos"));
                rr.save(new Reserva(null, cs.get(1).getId(), ss.get(1).getId(), LocalDateTime.now().minusDays(1), ReservaEstado.FINALIZADA, "Ana"));
                rr.save(new Reserva(null, cs.get(2).getId(), ss.get(0).getId(), LocalDateTime.now().plusDays(2), ReservaEstado.EN_PROGRESO, "Luis"));
            }
        }
    }

    private static void AddParte(ServicioInventario inv) {
        System.out.print("Nombre: "); String nombre = SC.nextLine();
        System.out.print("Categoria: "); String cat = SC.nextLine();
        System.out.print("Cantidad: "); int cant = Integer.parseInt(SC.nextLine());
        System.out.print("Precio unitario: "); double pu = Double.parseDouble(SC.nextLine());
        System.out.print("Costo: "); double costo = Double.parseDouble(SC.nextLine());
        Parte p = inv.addParte(nombre, cat, cant, pu, costo);
        System.out.println("Parte agregada: " + p);
    }

    private static void CrearReserva(ServicioReserva reservas, ClienteRepo cr, ServicioRepo sr) {
        List<Cliente> cs = cr.findAll();
        System.out.println("Clientes:");
        for (int i = 0; i < cs.size(); i++) System.out.printf("%d) %s (%s)\n", i+1, cs.get(i).getNombre(), cs.get(i).getId());
        System.out.print("Elija cliente #: "); int ci = Integer.parseInt(SC.nextLine()) - 1;
        List<Servicio> ss = sr.findAll();
        System.out.println("Servicios:");
        for (int i = 0; i < ss.size(); i++) System.out.printf("%d) %s (%s)\n", i+1, ss.get(i).getNombre(), ss.get(i).getId());
        System.out.print("Elija servicio #: "); int si = Integer.parseInt(SC.nextLine()) - 1;
        System.out.print("Mecánico: "); String mec = SC.nextLine();
        LocalDateTime fecha = LocalDateTime.now().plusDays(1);
        Reserva r = reservas.createReserva(cs.get(ci).getId(), ss.get(si).getId(), fecha, mec);
        System.out.println("Reserva creada: " + r);
    }

    private static void CambiarEstado(ServicioReserva reservas, ReservaRepo rr) {
        List<Reserva> rs = rr.findAll();
        for (int i = 0; i < rs.size(); i++) System.out.printf("%d) %s -> %s (%s)\n", i+1, rs.get(i).getId(), rs.get(i).getEstado(), rs.get(i).getServicioId());
        System.out.print("Elija reserva #: "); int idx = Integer.parseInt(SC.nextLine()) - 1;
        System.out.print("Nuevo estado (PROGRAMADA,EN_PROGRESO,FINALIZADA,ENTREGADA): ");
        ReservaEstado ne = ReservaEstado.valueOf(SC.nextLine().trim());
        Reserva r = reservas.changeEstado(rs.get(idx).getId(), ne);
        System.out.println("Actualizada: " + r);
    }

    private static void Historial(ServicioCliente cs, ClienteRepo cr) {
        List<Cliente> all = cr.findAll();
        for (int i = 0; i < all.size(); i++) System.out.printf("%d) %s (%s)\n", i+1, all.get(i).getNombre(), all.get(i).getId());
        System.out.print("Elija cliente #: "); int idx = Integer.parseInt(SC.nextLine()) - 1;
        HistorialCliente h = cs.getHistorial(all.get(idx).getId());
        System.out.println("Cliente: " + h.getCliente());
        System.out.println("Reservas: ");
        for (Reserva r : h.getReservas()) System.out.println(" - " + r);
        System.out.println("Servicios: ");
        for (Servicio s : h.getServicios()) System.out.println(" - " + s);
    }
}
