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
public class Cliente {
    private String id;
    private String nombre;
    private String contacto;
    private String marcaAuto;
    private String modeloAuto;
    private int anioAuto;

    public Cliente() {}

    public Cliente(String id, String nombre, String contacto, String marcaAuto, String modeloAuto, int anioAuto) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.marcaAuto = marcaAuto;
        this.modeloAuto = modeloAuto;
        this.anioAuto = anioAuto;
    }

    public String getId() { return id; }
    public void setId(String id) {
        if (id != null && id.isBlank()) id = null;
        this.id = id;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre requerido");
        this.nombre = nombre.trim();
    }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) {
        if (contacto == null || contacto.isBlank()) throw new IllegalArgumentException("contacto requerido");
        this.contacto = contacto.trim();
    }

    public String getMarcaAuto() { return marcaAuto; }
    public void setMarcaAuto(String marcaAuto) {
        if (marcaAuto == null || marcaAuto.isBlank()) throw new IllegalArgumentException("marca requerida");
        this.marcaAuto = marcaAuto.trim();
    }

    public String getModeloAuto() { return modeloAuto; }
    public void setModeloAuto(String modeloAuto) {
        if (modeloAuto == null || modeloAuto.isBlank()) throw new IllegalArgumentException("modelo requerido");
        this.modeloAuto = modeloAuto.trim();
    }

    public int getAnioAuto() { return anioAuto; }
    public void setAnioAuto(int anioAuto) {
        this.anioAuto = anioAuto;
    }

    @Override public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", contacto='" + contacto + '\'' +
                ", marcaAuto='" + marcaAuto + '\'' +
                ", modeloAuto='" + modeloAuto + '\'' +
                ", anioAuto=" + anioAuto +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }
}
