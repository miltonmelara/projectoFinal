package com.mycompany.proyectofinalpoo.util;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MigracionIdsReservas {

    // ✅ Método que renumera automáticamente los IDs existentes
    public static void ejecutarEnCarpeta(Path carpeta) throws Exception {
        Path reservas = carpeta.resolve("reservas.csv");
        if (!Files.exists(reservas)) {
            System.out.println("⚠️ No se encontró el archivo reservas.csv en: " + carpeta.toString());
            return;
        }

        List<String> lineas = Files.readAllLines(reservas, StandardCharsets.UTF_8);
        if (lineas.size() <= 1) {
            System.out.println("⚠️ No hay registros de reservas para renumerar.");
            return;
        }

        String encabezado = lineas.get(0);
        List<String> nuevas = new ArrayList<>();
        nuevas.add(encabezado);

        int contador = 1;
        for (int i = 1; i < lineas.size(); i++) {
            String l = lineas.get(i);
            if (l.trim().isEmpty()) continue;
            String[] campos = l.split(";");
            // cambia el primer campo (ID)
            campos[0] = String.format("R%06d", contador++);
            nuevas.add(String.join(";", campos));
        }

        Files.write(reservas, nuevas, StandardCharsets.UTF_8);
        System.out.println("✅ IDs de reservas renumerados correctamente (" + (contador - 1) + " registros actualizados).");
    }
}
