package com.mycompany.proyectofinalpoo.gui;

import com.mycompany.proyectofinalpoo.RolUsuario;

public final class Permisos {

    private Permisos() {}

    public static boolean esAdmin(RolUsuario r) {
        return r == RolUsuario.ADMIN;
    }

    public static boolean esMecanico(RolUsuario r) {
        return r == RolUsuario.MECANICO;
    }

    
    public static boolean verCalendario(RolUsuario r) { return true; }                 
    public static boolean verClientes(RolUsuario r)   { return esAdmin(r); }           
    public static boolean verReservas(RolUsuario r)   { return true; }               
    public static boolean verInventario(RolUsuario r) { return esAdmin(r); }           
    public static boolean verEstados(RolUsuario r)    { return esAdmin(r); }          
    public static boolean verHistorial(RolUsuario r)  { return true; }               
    public static boolean verReportes(RolUsuario r)   { return esAdmin(r); }           
    public static boolean verUsuarios(RolUsuario r)   { return esAdmin(r); }           
}
