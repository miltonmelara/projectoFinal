package com.mycompany.proyectofinalpoo.util;

import java.util.concurrent.atomic.AtomicInteger;

public class GeneradorIds {
    private static final AtomicInteger contadorClientes = new AtomicInteger(0);
    private static final AtomicInteger contadorPartes = new AtomicInteger(0);
    private static final AtomicInteger contadorReservas = new AtomicInteger(0);

    public static synchronized String generarIdCliente() {
        return String.format("C%03d", contadorClientes.incrementAndGet());
    }

    public static synchronized String generarIdParte() {
        return String.format("P%03d", contadorPartes.incrementAndGet());
    }

    public static synchronized String generarIdReserva() {
        return String.format("R%03d", contadorReservas.incrementAndGet());
    }

    // Permite inicializar los contadores al arrancar el sistema (por ejemplo, desde los repositorios)
    public static void inicializarContadores(int clientes, int partes, int reservas) {
        contadorClientes.set(clientes);
        contadorPartes.set(partes);
        contadorReservas.set(reservas);
    }
}
