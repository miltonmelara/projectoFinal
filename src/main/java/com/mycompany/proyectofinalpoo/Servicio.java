/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Bebe
 */
public class Servicio {
    private String id;
    private String nombre;
    private int duracionMin;
    private double costoTotal;
    private double precio;
    private Map<String, Integer> partesRequeridas = new HashMap<>();

    public Servicio() {}

    public Servicio(String id, String nombre, int duracionMin, double costoTotal, double precio, Map<String, Integer> partesRequeridas) {
    this.id = id;
    this.nombre = nombre;
    this.duracionMin = duracionMin;
    this.costoTotal = costoTotal;
    this.precio = precio;
    this.partesRequeridas = (partesRequeridas == null) ? new HashMap<>() : new HashMap<>(partesRequeridas);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = (id != null && id.isBlank()) ? null : id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre requerido");
        this.nombre = nombre.trim();
    }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) {
        if (duracionMin < 0) throw new IllegalArgumentException("duración negativa");
        this.duracionMin = duracionMin;
    }

    public double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(double costoTotal) {
        if (costoTotal < 0) throw new IllegalArgumentException("costoTotal negativo");
        this.costoTotal = costoTotal;
    }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) {
        if (precio < 0) throw new IllegalArgumentException("precio negativo");
        this.precio = precio;
    }

    public Map<String, Integer> getPartesRequeridas() { return Collections.unmodifiableMap(partesRequeridas); }
    public void setPartesRequeridas(Map<String, Integer> partesRequeridas) {
        this.partesRequeridas = new HashMap<>();
        if (partesRequeridas != null) {
            for (Map.Entry<String,Integer> e : partesRequeridas.entrySet()) {
                if (e.getKey() == null || e.getKey().isBlank()) continue;
                Integer val = e.getValue();
                if (val == null) {
                    this.partesRequeridas.put(e.getKey(), 0);
                } else {
                    if (val < 0) throw new IllegalArgumentException("cantidad negativa en partesRequeridas");
                    this.partesRequeridas.put(e.getKey(), val);
                }
            }
        }
    }

    public double calcularCostoTotal(Map<String, Parte> snapshotInventario) {
        double total = 0.0;
        if (snapshotInventario == null) return 0.0;
        for (Map.Entry<String,Integer> e : partesRequeridas.entrySet()) {
            Parte p = snapshotInventario.get(e.getKey());
            int qty = e.getValue();
            if (p != null) total += p.getCosto() * qty;
        }
        return total;
    }

    @Override public String toString() {
        return "Servicio{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", duracionMin=" + duracionMin +
                ", costoTotal=" + costoTotal +
                ", precio=" + precio +
                ", partesRequeridas=" + partesRequeridas +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servicio)) return false;
        Servicio that = (Servicio) o;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

}
