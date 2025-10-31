package com.mycompany.proyectofinalpoo.gui.componentes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GestorEventosSistema {
    private static final List<Consumer<Void>> oyentesMecanicos = new ArrayList<>();
    private static final List<Consumer<LocalDate>> oyentesReservas = new ArrayList<>();

    private GestorEventosSistema() {}

    public static void suscribirMecanicos(Consumer<Void> listener) {
        if (listener != null) oyentesMecanicos.add(listener);
    }

    public static void desuscribirMecanicos(Consumer<Void> listener) {
        oyentesMecanicos.remove(listener);
    }

    public static void notificarCambioMecanicos() {
        for (Consumer<Void> listener : List.copyOf(oyentesMecanicos)) {
            try { listener.accept(null); } catch (Exception ignored) {}
        }
    }

    public static void suscribirReservas(Consumer<LocalDate> listener) {
        if (listener != null) oyentesReservas.add(listener);
    }

    public static void desuscribirReservas(Consumer<LocalDate> listener) {
        oyentesReservas.remove(listener);
    }

    public static void notificarCambioReservas(LocalDate fechaReferencia) {
        for (Consumer<LocalDate> listener : List.copyOf(oyentesReservas)) {
            try { listener.accept(fechaReferencia); } catch (Exception ignored) {}
        }
    }
}
