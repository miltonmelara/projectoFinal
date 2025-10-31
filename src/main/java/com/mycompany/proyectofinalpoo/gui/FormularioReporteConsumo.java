package com.mycompany.proyectofinalpoo.gui;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ConsumoParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReporteReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DetalleConsumoReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ReporteConsumoReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.ResumenReservaReporte;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.gui.componentes.GestorEventosSistema;
import com.mycompany.proyectofinalpoo.gui.componentes.SelectorFechaPopup;
import com.mycompany.proyectofinalpoo.gui.EstiloCombos;
import com.mycompany.proyectofinalpoo.gui.TemaNeoBlue;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FormularioReporteConsumo extends JPanel {
    private final ServicioReporteReserva servicio;
    private final ClienteRepo clienteRepo;
    private final UsuarioRepo usuarioRepo;

    private final JTextField campoReservaId = new JTextField(18);
    private final JComboBox<ItemCliente> comboCliente = new JComboBox<>();
    private final JComboBox<String> comboMecanico = new JComboBox<>();
    private final JSpinner spDesde = new JSpinner(new SpinnerDateModel());
    private final JSpinner spHasta = new JSpinner(new SpinnerDateModel());

    private final DefaultTableModel modeloResultados = new DefaultTableModel(
            new Object[]{"ID", "Fecha", "Cliente", "Mecánico", "Servicio"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaResultados = new JTable(modeloResultados);

    private final DefaultTableModel modeloDetalle = new DefaultTableModel(
            new Object[]{"Parte", "Nombre", "Cantidad"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaDetalle = new JTable(modeloDetalle);

    private final JLabel etiquetaCliente = new JLabel("-");
    private final JLabel etiquetaServicio = new JLabel("-");
    private final JLabel etiquetaFecha = new JLabel("-");
    private final JLabel etiquetaTotalUnidades = new JLabel("0");

    private ReporteConsumoReserva reporteActual;
    private final JButton botonExportar = new JButton("Exportar CSV");
    private final Consumer<Void> escuchaMecanicos = v -> SwingUtilities.invokeLater(this::cargarMecanicos);

    public FormularioReporteConsumo() {
        this.clienteRepo = new ClienteFileRepo(Path.of("data"));
        this.usuarioRepo = new UsuarioFileRepo(Path.of("data"));
        this.servicio = new ServicioReporteReserva(
                new ReservaFileRepo(Path.of("data")),
                clienteRepo,
                new ServicioFileRepo(Path.of("data")),
                new ConsumoParteFileRepo(Path.of("data")),
                new ParteFileRepo(Path.of("data"))
        );

        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        construirUI();
        cargarClientes();
        cargarMecanicos();
        configurarFechasIniciales();
        TemaNeoBlue.estilizar(this);
        estilizarCombos();
        GestorEventosSistema.suscribirMecanicos(escuchaMecanicos);
    }

    private void construirUI() {
        JPanel panelFiltros = new JPanel(new GridBagLayout());
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;
        gbc.gridx = 0; gbc.gridy = fila;
        panelFiltros.add(new JLabel("ID reserva:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelFiltros.add(campoReservaId, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton btnBuscar = new JButton("Buscar");
        panelFiltros.add(btnBuscar, gbc);
        gbc.gridx = 3;
        botonExportar.setEnabled(false);
        panelFiltros.add(botonExportar, gbc);

        fila++;
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        panelFiltros.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelFiltros.add(comboCliente, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        panelFiltros.add(new JLabel("Mecánico:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        panelFiltros.add(comboMecanico, gbc);

        fila++;
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0;
        panelFiltros.add(new JLabel("Desde:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelFiltros.add(SelectorFechaPopup.adjuntar(spDesde), gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        panelFiltros.add(new JLabel("Hasta:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        panelFiltros.add(SelectorFechaPopup.adjuntar(spHasta), gbc);

        fila++;
        gbc.gridx = 0; gbc.gridy = fila; gbc.gridwidth = 4; gbc.weightx = 0;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnVerDetalle = new JButton("Ver detalle");
        panelBotones.add(btnFiltrar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnVerDetalle);
        panelFiltros.add(panelBotones, gbc);

        add(panelFiltros, BorderLayout.NORTH);

        tablaResultados.setRowHeight(22);
        tablaResultados.setFillsViewportHeight(true);
        tablaResultados.setAutoCreateRowSorter(true);
        JScrollPane scrollResultados = new JScrollPane(tablaResultados);
        scrollResultados.setBorder(BorderFactory.createTitledBorder("Reservas encontradas"));

        JPanel panelDetalle = new JPanel(new BorderLayout());
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Detalle de consumo"));
        panelDetalle.setPreferredSize(new Dimension(200, 220));

        JPanel cabecera = new JPanel(new GridBagLayout());
        GridBagConstraints h = new GridBagConstraints();
        h.insets = new Insets(2,4,2,4);
        h.anchor = GridBagConstraints.WEST;
        h.gridx = 0; h.gridy = 0;
        cabecera.add(new JLabel("Cliente:"), h);
        h.gridx = 1; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        cabecera.add(etiquetaCliente, h);
        h.gridx = 0; h.gridy = 1; h.weightx = 0; h.fill = GridBagConstraints.NONE;
        cabecera.add(new JLabel("Servicio:"), h);
        h.gridx = 1; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        cabecera.add(etiquetaServicio, h);
        h.gridx = 0; h.gridy = 2; h.weightx = 0; h.fill = GridBagConstraints.NONE;
        cabecera.add(new JLabel("Fecha:"), h);
        h.gridx = 1; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        cabecera.add(etiquetaFecha, h);
        h.gridx = 0; h.gridy = 3; h.weightx = 0; h.fill = GridBagConstraints.NONE;
        cabecera.add(new JLabel("Total unidades:"), h);
        h.gridx = 1; h.weightx = 1; h.fill = GridBagConstraints.HORIZONTAL;
        cabecera.add(etiquetaTotalUnidades, h);

        panelDetalle.add(cabecera, BorderLayout.NORTH);

        tablaDetalle.setRowHeight(22);
        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        panelDetalle.add(scrollDetalle, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollResultados, panelDetalle);
        split.setResizeWeight(0.55);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscarPorId());
        btnFiltrar.addActionListener(e -> filtrarReservas());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnVerDetalle.addActionListener(e -> verDetalleSeleccionado());
        botonExportar.addActionListener(e -> exportar());
        tablaResultados.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) verDetalleSeleccionado();
            }
        });
    }

    private void cargarClientes() {
        comboCliente.removeAllItems();
        comboCliente.addItem(new ItemCliente(null, "Todos"));
        for (Cliente c : clienteRepo.findAll()) {
            comboCliente.addItem(new ItemCliente(c.getId(), c.getNombre()));
        }
    }

    private void cargarMecanicos() {
        Object seleccionado = comboMecanico.getSelectedItem();
        comboMecanico.removeAllItems();
        comboMecanico.addItem("Todos");
        for (Usuario u : usuarioRepo.findAll()) {
            if (u.getRol() == RolUsuario.MECANICO) {
                comboMecanico.addItem(u.getUsername());
            }
        }
        comboMecanico.setSelectedItem(seleccionado);
    }

    private void configurarFechasIniciales() {
        LocalDate hoy = LocalDate.now();
        spDesde.setValue(java.util.Date.from(hoy.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spHasta.setValue(java.util.Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void buscarPorId() {
        String id = campoReservaId.getText() == null ? "" : campoReservaId.getText().trim();
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

    private void filtrarReservas() {
        ItemCliente clienteSel = (ItemCliente) comboCliente.getSelectedItem();
        String clienteId = (clienteSel == null || clienteSel.id == null || "Todos".equalsIgnoreCase(clienteSel.nombre)) ? null : clienteSel.id;
        String mecanicoSel = comboMecanico.getSelectedItem() != null && !"Todos".equalsIgnoreCase(comboMecanico.getSelectedItem().toString())
                ? comboMecanico.getSelectedItem().toString()
                : null;
        LocalDate desde = toLocal((java.util.Date) spDesde.getValue());
        LocalDate hasta = toLocal((java.util.Date) spHasta.getValue());
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            LocalDate tmp = desde; desde = hasta; hasta = tmp;
            spDesde.setValue(toDate(desde));
            spHasta.setValue(toDate(hasta));
        }
        List<ResumenReservaReporte> resultados = servicio.buscarReservas(clienteId, mecanicoSel, desde, hasta);
        modeloResultados.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (ResumenReservaReporte r : resultados) {
            String fecha = r.getFecha() == null ? "" : r.getFecha().format(fmt);
            modeloResultados.addRow(new Object[]{r.getReservaId(), fecha, r.getClienteNombre(), r.getMecanicoNombre(), r.getServicioNombre()});
        }
        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron reservas con los filtros seleccionados");
            tablaResultados.clearSelection();
        } else {
            tablaResultados.clearSelection();
            int viewRow = 0;
            if (tablaResultados.getRowCount() > 0) {
                if (tablaResultados.getRowSorter() != null) {
                    viewRow = tablaResultados.convertRowIndexToView(0);
                }
                tablaResultados.setRowSelectionInterval(viewRow, viewRow);
                tablaResultados.scrollRectToVisible(tablaResultados.getCellRect(viewRow, 0, true));
            }
        }
    }

    private void verDetalleSeleccionado() {
        int fila = tablaResultados.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una reserva de la tabla");
            return;
        }
        String reservaId = String.valueOf(modeloResultados.getValueAt(fila, 0));
        campoReservaId.setText(reservaId);
        buscarPorId();
    }

    private void cargarEnPantalla(ReporteConsumoReserva r) {
        etiquetaCliente.setText(r.getClienteNombre() == null ? "" : r.getClienteNombre());
        etiquetaServicio.setText(r.getServicioNombre() == null ? "" : r.getServicioNombre());
        etiquetaFecha.setText(r.getFechaReserva() == null ? "" : r.getFechaReserva().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        etiquetaTotalUnidades.setText(String.valueOf(r.getTotalUnidades()));
        modeloDetalle.setRowCount(0);
        List<DetalleConsumoReserva> detalles = r.getDetalles();
        for (DetalleConsumoReserva d : detalles) {
            modeloDetalle.addRow(new Object[]{d.getParteId(), d.getNombreParte(), d.getCantidad()});
        }
    }

    private void limpiarFiltros() {
        campoReservaId.setText("");
        comboCliente.setSelectedIndex(0);
        comboMecanico.setSelectedIndex(0);
        configurarFechasIniciales();
        modeloResultados.setRowCount(0);
        limpiarPantalla();
        botonExportar.setEnabled(false);
    }

    private void limpiarPantalla() {
        etiquetaCliente.setText("-");
        etiquetaServicio.setText("-");
        etiquetaFecha.setText("-");
        etiquetaTotalUnidades.setText("0");
        modeloDetalle.setRowCount(0);
        reporteActual = null;
    }

    private void exportar() {
        if (reporteActual == null) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte CSV");
        fc.setSelectedFile(new java.io.File("reporte_consumo_reserva_" + reporteActual.getReservaId() + ".csv"));
        fc.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        fc.setAcceptAllFileFilterUsed(true);
        TemaNeoBlue.estilizar(fc);
        SwingUtilities.updateComponentTreeUI(fc);
        int r = fc.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        java.io.File destino = fc.getSelectedFile();
        if (destino != null && !destino.getName().toLowerCase().endsWith(".csv")) {
            destino = new java.io.File(destino.getAbsolutePath() + ".csv");
        }
        try (BufferedWriter w = java.nio.file.Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8)) {
            escribirLinea(w, "Reserva", "Cliente", "Servicio", "Fecha", "ParteId", "NombreParte", "Cantidad", "TotalUnidades");
            String fecha = reporteActual.getFechaReserva() == null ? "" : reporteActual.getFechaReserva().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            escribirLinea(w,
                    reporteActual.getReservaId(),
                    reporteActual.getClienteNombre(),
                    reporteActual.getServicioNombre(),
                    fecha,
                    "",
                    "TOTAL",
                    String.valueOf(reporteActual.getTotalUnidades()),
                    String.valueOf(reporteActual.getTotalUnidades()));
            for (DetalleConsumoReserva d : reporteActual.getDetalles()) {
                escribirLinea(w,
                        reporteActual.getReservaId(),
                        reporteActual.getClienteNombre(),
                        reporteActual.getServicioNombre(),
                        fecha,
                        d.getParteId(),
                        d.getNombreParte(),
                        String.valueOf(d.getCantidad()),
                        "");
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

    private void escribirLinea(BufferedWriter w, String... valores) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) sb.append(';');
            sb.append(escape(valores[i]));
        }
        w.write(sb.toString());
        w.newLine();
    }

    private static LocalDate toLocal(java.util.Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static java.util.Date toDate(LocalDate d) {
        return java.util.Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static class ItemCliente {
        final String id;
        final String nombre;
        ItemCliente(String id, String nombre) {
            this.id = id;
            this.nombre = nombre == null ? "" : nombre;
        }
        @Override public String toString() { return nombre; }
    }

    private void estilizarCombos() {
        Color fondo = TemaNeoBlue.BG;
        Color texto = TemaNeoBlue.TXT;
        Color borde = new Color(120, 150, 220, 140);
        EstiloCombos.aplicar(comboCliente, fondo, texto, TemaNeoBlue.ACCENT, Color.WHITE);
        EstiloCombos.aplicar(comboMecanico, fondo, texto, TemaNeoBlue.ACCENT, Color.WHITE);
        comboCliente.setOpaque(true);
        comboMecanico.setOpaque(true);
        comboCliente.setBackground(fondo);
        comboMecanico.setBackground(fondo);
        comboCliente.setBorder(BorderFactory.createLineBorder(borde, 1, true));
        comboMecanico.setBorder(BorderFactory.createLineBorder(borde, 1, true));
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        GestorEventosSistema.desuscribirMecanicos(escuchaMecanicos);
    }
}
