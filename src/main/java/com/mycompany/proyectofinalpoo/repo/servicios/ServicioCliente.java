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
    }

    public static class ActualizarClienteRequest {
        private final String nombre;
        private final String contacto;
        private final String marcaAuto;
        private final String modeloAuto;
        private final Integer anioAuto;

        public ActualizarClienteRequest(String nombre, String contacto, String marcaAuto, String modeloAuto, Integer anioAuto) {
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
    }

    public ServicioCliente(ClienteRepo clienteRepo, ReservaRepo reservaRepo, ServicioRepo servicioRepo) {
        this.clienteRepo = Objects.requireNonNull(clienteRepo);
        this.reservaRepo = Objects.requireNonNull(reservaRepo);
        this.servicioRepo = Objects.requireNonNull(servicioRepo);
    }
    
    private DatosCliente validarDatosCliente(String idCliente, String nombre, String contacto, String marca, String modelo, Integer anio) {
        if (nombre == null || contacto == null || marca == null || modelo == null || anio == null) throw new ValidationException("datos incompletos");
        String nombreNormalizado = normalizarCadena(nombre);
        if (nombreNormalizado == null) throw new ValidationException("nombre requerido");
        String contactoNormalizado = normalizarContacto(contacto);
        if (contactoNormalizado == null) throw new ValidationException("contacto requerido");
        String marcaNormalizada = normalizarCadena(marca);
        if (marcaNormalizada == null) throw new ValidationException("marca requerida");
        String modeloNormalizado = normalizarCadena(modelo);
        if (modeloNormalizado == null) throw new ValidationException("modelo requerido");
        int anioNormalizado = anio;
        int anioMinimo = 1980;
        int anioMaximo = LocalDate.now().getYear() + 1;
        if (anioNormalizado < anioMinimo || anioNormalizado > anioMaximo) throw new ValidationException("año fuera de rango");
        Cliente coincidencia = clienteRepo.findByContacto(contactoNormalizado).orElse(null);
        if (coincidencia != null && (idCliente == null || !coincidencia.getId().equals(idCliente))) throw new ValidationException("contacto duplicado");
        return new DatosCliente(nombreNormalizado, contactoNormalizado, marcaNormalizada, modeloNormalizado, anioNormalizado);
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

    public Cliente crearCliente(NuevoClienteRequest solicitud) {
        if (solicitud == null) throw new ValidationException("solicitud requerida");
        DatosCliente datos = validarDatosCliente(null, solicitud.getNombre(), solicitud.getContacto(), solicitud.getMarcaAuto(), solicitud.getModeloAuto(), solicitud.getAnioAuto());
        Cliente cliente = new Cliente();
        datos.aplicar(cliente);
        clienteRepo.save(cliente);
        return cliente;
    }

    public Cliente actualizarCliente(String idCliente, ActualizarClienteRequest solicitud) {
        if (idCliente == null || idCliente.trim().isEmpty()) throw new ValidationException("id requerido");
        if (solicitud == null) throw new ValidationException("solicitud requerida");
        String idNormalizado = idCliente.trim();
        Cliente clienteExistente = clienteRepo.findById(idNormalizado).orElseThrow(() -> new NotFoundException("cliente no encontrado"));
        DatosCliente datos = validarDatosCliente(idNormalizado, solicitud.getNombre(), solicitud.getContacto(), solicitud.getMarcaAuto(), solicitud.getModeloAuto(), solicitud.getAnioAuto());
        datos.aplicar(clienteExistente);
        clienteRepo.update(clienteExistente);
        return clienteExistente;
    }

    public void deleteCliente(String id) { clienteRepo.delete(id); }

    public HistorialCliente getHistorial(String clienteId) {
        Cliente cliente = clienteRepo.findById(clienteId).orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + clienteId));
        List<Reserva> reservas = reservaRepo.findByClienteId(clienteId);
        List<Servicio> servicios = new ArrayList<>();
        for (Reserva reserva : reservas) servicioRepo.findById(reserva.getServicioId()).ifPresent(servicios::add);
        return new HistorialCliente(cliente, reservas, servicios);
    }

    private static class DatosCliente {
        private final String nombre;
        private final String contacto;
        private final String marca;
        private final String modelo;
        private final int anio;

        private DatosCliente(String nombre, String contacto, String marca, String modelo, int anio) {
            this.nombre = nombre;
            this.contacto = contacto;
            this.marca = marca;
            this.modelo = modelo;
            this.anio = anio;
        }

        private void aplicar(Cliente cliente) {
            cliente.setNombre(nombre);
            cliente.setContacto(contacto);
            cliente.setMarcaAuto(marca);
            cliente.setModeloAuto(modelo);
            cliente.setAnioAuto(anio);
        }
    }
}
