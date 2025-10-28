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
import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios;

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
        if (cr.findAll().isEmpty()) {
            cr.save(new Cliente(null, "Ana Pérez", "555111", "Toyota", "Corolla", 2019));
            cr.save(new Cliente(null, "Luis Gómez", "555222", "Honda", "Civic", 2020));
            cr.save(new Cliente(null, "Marta Díaz", "555333", "Ford", "Focus", 2018));
        }
        if (pr.findAll().isEmpty()) {
            pr.save(new Parte(null, "FILTRO-ACEITE", "Filtros", 10, 8.5, 5.0));
            pr.save(new Parte(null, "ACEITE-5W30", "Aceites", 40, 12.0, 8.0));
            pr.save(new Parte(null, "BUJIA-IRIDIUM", "Encendido", 25, 15.0, 9.0));
            pr.save(new Parte(null, "PASTILLA-FRENO", "Frenos", 30, 20.0, 12.0));
            pr.save(new Parte(null, "FILTRO-AIRE", "Filtros", 15, 10.0, 6.0));
        }
        if (sr.findAll().isEmpty()) {
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
