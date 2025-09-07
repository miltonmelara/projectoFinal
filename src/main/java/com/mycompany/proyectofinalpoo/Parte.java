/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo;
import java.util.Objects;

/**
 *
 * @author Bebe
 */
public class Parte {
    private String id;
    private String nombre;
    private String categoria;
    private int cantidad;
    private double precioUnitario;
    private double costo;

    public Parte() {}

    public Parte(String id, String nombre, String categoria, int cantidad, double precioUnitario, double costo) {
    this.id = id;
    this.nombre = nombre;
    this.categoria = categoria;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
    this.costo = costo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = (id != null && id.isBlank()) ? null : id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre requerido");
        this.nombre = nombre.trim();
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) throw new IllegalArgumentException("categoria requerida");
        this.categoria = categoria.trim();
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("cantidad no puede ser negativa");
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario < 0) throw new IllegalArgumentException("precioUnitario no puede ser negativo");
        this.precioUnitario = precioUnitario;
    }

    public double getCosto() { return costo; }
    public void setCosto(double costo) {
        if (costo < 0) throw new IllegalArgumentException("costo no puede ser negativo");
        this.costo = costo;
    }

    public void reducirCantidad(int n) {
        if (n < 0) throw new IllegalArgumentException("reducción negativa");
        if (n > cantidad) throw new IllegalArgumentException("stock insuficiente");
        this.cantidad -= n;
    }

    public boolean verificarBajoStock(int threshold) {
        return cantidad <= Math.max(0, threshold);
    }

    @Override public String toString() {
        return "Parte{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", costo=" + costo +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parte)) return false;
        Parte parte = (Parte) o;
        return Objects.equals(id, parte.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

}
