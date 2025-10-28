package com.mycompany.proyectofinalpoo.gui;

import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ConsumoParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReporteReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ReporteConsumoReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DetalleConsumoReserva;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FormularioReporteConsumo extends JPanel {
    private final ServicioReporteReserva servicio;
    private final JTextField campoReservaId;
    private final JButton botonBuscar;
    private final JButton botonExportar;
    private final JLabel etiquetaCliente;
    private final JLabel etiquetaServicio;
    private final JLabel etiquetaFecha;
    private final JLabel etiquetaTotalUnidades;
    private final JTable tabla;
    private final DefaultTableModel modelo;
    private ReporteConsumoReserva reporteActual;

    public FormularioReporteConsumo() {
        this.servicio = new ServicioReporteReserva(
                new ReservaFileRepo(Path.of("data")),
                new ClienteFileRepo(Path.of("data")),
                new ServicioFileRepo(Path.of("data")),
                new ConsumoParteFileRepo(Path.of("data")),
                new ParteFileRepo(Path.of("data"))
        );

        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));

        JPanel panelBusqueda = new JPanel(new GridBagLayout());
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        panelBusqueda.add(new JLabel("ID de reserva:"), c);

        campoReservaId = new JTextField(24);
        c.gridx = 1; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        panelBusqueda.add(campoReservaId, c);

        botonBuscar = new JButton("Buscar");
        c.gridx = 2; c.gridy = 0; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        panelBusqueda.add(botonBuscar, c);

        botonExportar = new JButton("Exportar CSV");
        botonExportar.setEnabled(false);
        c.gridx = 3; c.gridy = 0;
        panelBusqueda.add(botonExportar, c);

        JPanel panelCabecera = new JPanel(new GridBagLayout());
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        GridBagConstraints h = new GridBagConstraints();
        h.insets = new Insets(2, 4, 2, 4);
        h.gridx = 0; h.gridy = 0; h.anchor = GridBagConstraints.WEST;
        panelCabecera.add(new JLabel("Cliente:"), h);
        etiquetaCliente = new JLabel("-");
        h.gridx = 1; h.gridy = 0; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        panelCabecera.add(etiquetaCliente, h);

        h.gridx = 0; h.gridy = 1; h.weightx = 0; h.fill = GridBagConstraints.NONE;
        panelCabecera.add(new JLabel("Servicio:"), h);
        etiquetaServicio = new JLabel("-");
        h.gridx = 1; h.gridy = 1; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        panelCabecera.add(etiquetaServicio, h);

        h.gridx = 0; h.gridy = 2; h.weightx = 0; h.fill = GridBagConstraints.NONE;
        panelCabecera.add(new JLabel("Fecha:"), h);
        etiquetaFecha = new JLabel("-");
        h.gridx = 1; h.gridy = 2; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        panelCabecera.add(etiquetaFecha, h);

        h.gridx = 0; h.gridy = 3; h.weightx = 0; h.fill = GridBagConstraints.NONE;
        panelCabecera.add(new JLabel("Total unidades:"), h);
        etiquetaTotalUnidades = new JLabel("0");
        h.gridx = 1; h.gridy = 3; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        panelCabecera.add(etiquetaTotalUnidades, h);

        panelSuperior.add(panelBusqueda);
        panelSuperior.add(panelCabecera);

        modelo = new DefaultTableModel(new Object[]{"Parte", "Nombre", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        botonBuscar.addActionListener(e -> buscar());
        botonExportar.addActionListener(e -> exportar());
    }

    private void buscar() {
        String id = campoReservaId.getText() != null ? campoReservaId.getText().trim() : "";
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el ID de la reserva");
            return;
        }
        try {
            reporteActual = servicio.generarReportePorReserva(id);
            cargarEnPantalla(reporteActual);
            botonExportar.setEnabled(true);
        } catch (RuntimeException ex) {
            limpiarPantalla();
            botonExportar.setEnabled(false);
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void cargarEnPantalla(ReporteConsumoReserva r) {
        etiquetaCliente.setText(r.getClienteNombre() == null ? "" : r.getClienteNombre());
        etiquetaServicio.setText(r.getServicioNombre() == null ? "" : r.getServicioNombre());
        etiquetaFecha.setText(r.getFechaReserva() == null ? "" : r.getFechaReserva().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        etiquetaTotalUnidades.setText(String.valueOf(r.getTotalUnidades()));
        modelo.setRowCount(0);
        List<DetalleConsumoReserva> detalles = r.getDetalles();
        for (DetalleConsumoReserva d : detalles) {
            modelo.addRow(new Object[]{d.getParteId(), d.getNombreParte(), d.getCantidad()});
        }
    }

    private void limpiarPantalla() {
        etiquetaCliente.setText("-");
        etiquetaServicio.setText("-");
        etiquetaFecha.setText("-");
        etiquetaTotalUnidades.setText("0");
        modelo.setRowCount(0);
    }

    private void exportar() {
        if (reporteActual == null) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("reporte_consumo_reserva_" + reporteActual.getReservaId() + ".csv"));
        int r = fc.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        java.io.File destino = fc.getSelectedFile();
        try (BufferedWriter w = java.nio.file.Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8)) {
            w.write("Reserva;Cliente;Servicio;Fecha;TotalUnidades");
            w.newLine();
            String fecha = reporteActual.getFechaReserva() == null ? "" : reporteActual.getFechaReserva().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            w.write(escape(reporteActual.getReservaId()) + ";" + escape(reporteActual.getClienteNombre()) + ";" + escape(reporteActual.getServicioNombre()) + ";" + escape(fecha) + ";" + reporteActual.getTotalUnidades());
            w.newLine();
            w.write("ParteId;Nombre;Cantidad");
            w.newLine();
            for (DetalleConsumoReserva d : reporteActual.getDetalles()) {
                w.write(escape(d.getParteId()) + ";" + escape(d.getNombreParte()) + ";" + d.getCantidad());
                w.newLine();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar: " + ex.getMessage());
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        String t = s.replace(";", ",");
        if (t.contains("\"") || t.contains(",") || t.contains("\n")) {
            t = "\"" + t.replace("\"", "\"\"") + "\"";
        }
        return t;
    }
}
