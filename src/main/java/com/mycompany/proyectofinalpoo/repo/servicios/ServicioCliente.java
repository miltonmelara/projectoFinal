/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.HistorialCliente;

/**
 *
 * @author Bebe
 */
public class ServicioCliente {
    private final ClienteRepo clienteRepo;
    private final ReservaRepo reservaRepo;
    private final ServicioRepo servicioRepo;

    public static class NuevoClienteRequest {
        private final String nombre;
        private final String contacto;
        private final String marcaAuto;
        private final String modeloAuto;
        private final Integer anioAuto;

        public NuevoClienteRequest(String nombre, String contacto, String marcaAuto, String modeloAuto, Integer anioAuto) {
            this.nombre = nombre;
            this.contacto = contacto;
            this.marcaAuto = marcaAuto;
            this.modeloAuto = modeloAuto;
            this.anioAuto = anioAuto;
        }

        public String getNombre() { return nombre; }
        public String getContacto() { return contacto; }
        public String getMarcaAuto() { return marcaAuto; }
        public String getModeloAuto() { return modeloAuto; }
        public Integer getAnioAuto() { return anioAuto; }

        public Cliente toCliente() {
            Cliente cliente = new Cliente();
            cliente.setId(null);
            cliente.setNombre(nombre);
            cliente.setContacto(contacto);
            cliente.setMarcaAuto(marcaAuto);
            cliente.setModeloAuto(modeloAuto);
            cliente.setAnioAuto(anioAuto);
            return cliente;
        }
    }

    public ServicioCliente(ClienteRepo clienteRepo, ReservaRepo reservaRepo, ServicioRepo servicioRepo) {
        this.clienteRepo = Objects.requireNonNull(clienteRepo);
        this.reservaRepo = Objects.requireNonNull(reservaRepo);
        this.servicioRepo = Objects.requireNonNull(servicioRepo);
    }
    
    private NuevoClienteRequest validar(NuevoClienteRequest solicitud) {
        if (solicitud == null) throw new ValidationException("solicitud requerida");
        String nombreNormalizado = normalizarCadena(solicitud.getNombre());
        if (nombreNormalizado == null) throw new ValidationException("nombre requerido");
        String contactoNormalizado = normalizarContacto(solicitud.getContacto());
        if (contactoNormalizado == null) throw new ValidationException("contacto requerido");
        String marcaNormalizada = normalizarCadena(solicitud.getMarcaAuto());
        if (marcaNormalizada == null) throw new ValidationException("marca requerida");
        String modeloNormalizado = normalizarCadena(solicitud.getModeloAuto());
        if (modeloNormalizado == null) throw new ValidationException("modelo requerido");
        Integer anioAuto = solicitud.getAnioAuto();
        if (anioAuto == null) throw new ValidationException("año requerido");
        int anioNormalizado = anioAuto;
        int anioMinimo = 1980;
        int anioMaximo = LocalDate.now().getYear() + 1;
        if (anioNormalizado < anioMinimo || anioNormalizado > anioMaximo) throw new ValidationException("año fuera de rango");
        if (clienteRepo.existsByContacto(contactoNormalizado)) throw new ValidationException("contacto duplicado");
        return new NuevoClienteRequest(nombreNormalizado, contactoNormalizado, marcaNormalizada, modeloNormalizado, anioNormalizado);
    }

    private String normalizarCadena(String valor) {
        if (valor == null) return null;
        String resultado = valor.trim();
        if (resultado.isEmpty()) return null;
        return resultado;
    }

    private String normalizarContacto(String contacto) {
        if (contacto == null) return null;
        String resultado = contacto.trim().replaceAll("\\s+", " ");
        if (resultado.isEmpty()) return null;
        return resultado;
    }

    /**
     * Crea un nuevo cliente validando datos y asegura consistencia antes de persistir.
     * Formularios futuros enviarán la información usando esta operación y deben manejar ValidationException ante entradas inválidas.
     */
    public Cliente crearCliente(NuevoClienteRequest solicitud) {
        NuevoClienteRequest solicitudNormalizada = validar(solicitud);
        Cliente clienteNuevo = solicitudNormalizada.toCliente();
        clienteRepo.save(clienteNuevo);
        return clienteNuevo;
    }

    /** Actualiza un cliente existente. */
    public Cliente updateCliente(Cliente cliente) {
        if (cliente.getId() == null) throw new ValidationException("id requerido para actualizar cliente");
        clienteRepo.update(cliente);
        return cliente;
    }

    public void deleteCliente(String id) { clienteRepo.delete(id); }

    /** Obtiene historial de reservas y servicios del cliente. */
    public HistorialCliente getHistorial(String clienteId) {
        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + clienteId));
    List<Reserva> reservas = reservaRepo.findByClienteId(clienteId);
    List<Servicio> servicios = new ArrayList<>();
    for (Reserva reserva : reservas) servicioRepo.findById(reserva.getServicioId()).ifPresent(servicios::add);
    return new HistorialCliente(cliente, reservas, servicios);
    }
}
