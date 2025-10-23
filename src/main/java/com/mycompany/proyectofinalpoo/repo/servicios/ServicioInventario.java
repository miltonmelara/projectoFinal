/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;

/**
 *
 * @author Bebe
 */
public class ServicioInventario {
    private final ParteRepo parteRepo;

    public ServicioInventario(ParteRepo parteRepo) { this.parteRepo = Objects.requireNonNull(parteRepo); }

    /**
     * Agrega una parte al inventario.
     * @throws ValidationException si cantidad o precios son negativos
     */
    public Parte addParte(String nombre, String categoria, int cantidad, double precioUnit, double costo) {
        requireAdmin();
        if (cantidad < 0) throw new ValidationException("cantidad no puede ser negativa");
        if (precioUnit < 0 || costo < 0) throw new ValidationException("precios/costo no pueden ser negativos");
        Parte nuevaParte = new Parte(null, nombre, categoria, cantidad, precioUnit, costo);
        parteRepo.save(nuevaParte);
        return nuevaParte;
    }

    /** Actualiza una parte existente por id. */
    public Parte updateParte(Parte p) {
        requireAdmin();
        if (p.getId() == null) throw new ValidationException("id requerido para actualizar");
        parteRepo.update(p);
        return p;
    }

    public void deleteParte(String id) {
        requireAdmin();
        parteRepo.delete(id);
    }

    /** Lista inventario con filtros opcionales por categoría y umbral de bajo stock. */
    public List<Parte> listInventory(Optional<String> categoria, Optional<Integer> lowStockThreshold) {
        SecurityContext.requireUser();
        List<Parte> todas = parteRepo.findAll();
        List<Parte> filtradas = new ArrayList<>();
        for (Parte parte : todas) {
            boolean coincide = true;
            if (categoria.isPresent()) coincide &= parte.getCategoria().equalsIgnoreCase(categoria.get());
            if (lowStockThreshold.isPresent()) coincide &= parte.verificarBajoStock(lowStockThreshold.get());
            if (coincide) filtradas.add(parte);
        }
        return filtradas;
    }

    private void requireAdmin() {
        Usuario usuario = SecurityContext.requireUser();
        ControlAcceso.requireRol(usuario, RolUsuario.ADMIN);
    }
}
