package com.mycompany.proyectofinalpoo.gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class VentanaReporteConsumo extends JFrame {
    public VentanaReporteConsumo() {
        setTitle("Reporte de consumo por reserva");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new FormularioReporteConsumo(), BorderLayout.CENTER);
    }
}
