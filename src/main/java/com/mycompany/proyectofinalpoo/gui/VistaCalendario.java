package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.CalendarioReservas;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DiaCalendario;
import com.mycompany.proyectofinalpoo.gui.EstiloCombos;

public class VistaCalendario extends JPanel {
    private final ServicioReserva servicioReserva;
    private final com.mycompany.proyectofinalpoo.repo.ParteRepo parteRepo;
    private final com.mycompany.proyectofinalpoo.repo.ServicioRepo servicioRepo;

    private final JSpinner fechaInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner fechaFin = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> mecanico = new JComboBox<>();
    private final JComboBox<ReservaEstado> estado = new JComboBox<>();
    private final JButton btnAplicar = new JButton("Aplicar");
    private final JButton btnHoy = new JButton("Hoy");
    private final JButton btnMes = new JButton("Mes actual");
    private final JButton btnCerrar = new JButton("Cerrar reserva");

    private final DefaultTableModel modelDias = new DefaultTableModel(new Object[]{"Fecha","Total","Programadas","En Progreso","Finalizadas","Entregadas"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tablaDias = new JTable(modelDias);
    private final DefaultTableModel modelDetalle = new DefaultTableModel(new Object[]{"Fecha","Cliente","Mecánico","Servicio","Estado","ID"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tablaDetalle = new JTable(modelDetalle);

    private boolean actualizandoOpciones = false;

    public VistaCalendario(ServicioReserva servicioReserva,
                           com.mycompany.proyectofinalpoo.repo.ParteRepo parteRepo,
                           com.mycompany.proyectofinalpoo.repo.ServicioRepo servicioRepo) {
        this.servicioReserva = servicioReserva;
        this.parteRepo = parteRepo;
        this.servicioRepo = servicioRepo;

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        TemaNeoBlue.CardPanel filtros = new TemaNeoBlue.CardPanel();
        filtros.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        fechaInicio.setEditor(new JSpinner.DateEditor(fechaInicio,"yyyy-MM-dd"));
        fechaFin.setEditor(new JSpinner.DateEditor(fechaFin,"yyyy-MM-dd"));
        estado.addItem(null);
        for (ReservaEstado e : ReservaEstado.values()) estado.addItem(e);

        mecanico.setPreferredSize(new Dimension(300, 32));
        estado.setPreferredSize(new Dimension(260, 32));
        EstiloCombos.aplicarDarkAzul(mecanico);
        EstiloCombos.aplicarDarkAzul(estado);

        gc.gridx=0; gc.gridy=0; filtros.add(new JLabel("Inicio"), gc);
        gc.gridx=1; gc.gridy=0; filtros.add(fechaInicio, gc);
        gc.gridx=2; gc.gridy=0; filtros.add(new JLabel("Fin"), gc);
        gc.gridx=3; gc.gridy=0; filtros.add(fechaFin, gc);

        gc.gridx=0; gc.gridy=1; filtros.add(new JLabel("Mecánico"), gc);
        gc.gridx=1; gc.gridy=1; filtros.add(mecanico, gc);
        gc.gridx=2; gc.gridy=1; filtros.add(new JLabel("Estado"), gc);
        gc.gridx=3; gc.gridy=1; filtros.add(estado, gc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.add(btnAplicar);
        acciones.add(btnHoy);
        acciones.add(btnMes);
        acciones.add(btnCerrar);
        gc.gridx=0; gc.gridy=2; gc.gridwidth=4; filtros.add(acciones, gc);

        tablaDias.setRowHeight(22);
        tablaDias.setFillsViewportHeight(true);
        tablaDetalle.setRowHeight(22);
        tablaDetalle.setFillsViewportHeight(true);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaDias), new JScrollPane(tablaDetalle));
        split.setResizeWeight(0.6);

        add(filtros, BorderLayout.NORTH);
        TemaNeoBlue.CardPanel cardTabla = new TemaNeoBlue.CardPanel();
        cardTabla.setLayout(new BorderLayout());
        cardTabla.add(split, BorderLayout.CENTER);
        add(cardTabla, BorderLayout.CENTER);

        btnAplicar.addActionListener(e -> { asegurarOpcionesMecanicos(); refrescar(); });
        estado.addActionListener(e -> { asegurarOpcionesMecanicos(); refrescar(); });
        mecanico.addActionListener(e -> { if (!actualizandoOpciones) refrescar(); });
        btnHoy.addActionListener(e -> rangoHoy());
        btnMes.addActionListener(e -> rangoMesActual());
        btnCerrar.addActionListener(e -> cerrarSeleccionada());
        tablaDias.getSelectionModel().addListSelectionListener(e -> cargarDetalleSeleccion());

        LocalDate hoy = LocalDate.now();
        fechaInicio.setValue(java.util.Date.from(hoy.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        fechaFin.setValue(java.util.Date.from(hoy.withDayOfMonth(hoy.lengthOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        asegurarOpcionesMecanicos();
        refrescar();
    }

    public void refrescarDesdeExterno() {
        SwingUtilities.invokeLater(() -> { asegurarOpcionesMecanicos(); refrescar(); });
    }

    private void asegurarOpcionesMecanicos() {
        actualizandoOpciones = true;
        try {
            LocalDate ini = toLocal((java.util.Date) fechaInicio.getValue());
            LocalDate fin = toLocal((java.util.Date) fechaFin.getValue());
            if (ini.isAfter(fin)) {
                LocalDate tmp = ini; ini = fin; fin = tmp;
                fechaInicio.setValue(toDate(ini));
                fechaFin.setValue(toDate(fin));
            }
            ReservaEstado est = (ReservaEstado) estado.getSelectedItem();

            Set<String> nombres = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, null, est);
            if (cal != null && cal.obtenerDias() != null) {
                for (DiaCalendario d : cal.obtenerDias()) {
                    if (d.obtenerReservas() == null) continue;
                    for (Reserva r : d.obtenerReservas()) {
                        String mec = r.getMecanicoAsignado();
                        if (mec != null && !mec.isBlank()) nombres.add(mec.trim());
                    }
                }
            }

            String seleccionAnterior = (String) mecanico.getSelectedItem();
            mecanico.removeAllItems();
            mecanico.addItem("Todos");
            for (String n : nombres) mecanico.addItem(n);
            if (seleccionAnterior != null) {
                boolean sigue = seleccionAnterior.equalsIgnoreCase("Todos") || nombres.stream().anyMatch(x -> x.equalsIgnoreCase(seleccionAnterior));
                if (sigue) mecanico.setSelectedItem(seleccionAnterior);
            }
        } finally {
            actualizandoOpciones = false;
        }
    }

    private void refrescar() {
        LocalDate ini = toLocal((java.util.Date) fechaInicio.getValue());
        LocalDate fin = toLocal((java.util.Date) fechaFin.getValue());
        if (ini.isAfter(fin)) {
            LocalDate tmp = ini; ini = fin; fin = tmp;
            fechaInicio.setValue(toDate(ini));
            fechaFin.setValue(toDate(fin));
        }
        String mecSel = (String) mecanico.getSelectedItem();
        String filtroMecanico = (mecSel == null || mecSel.equalsIgnoreCase("Todos")) ? null : mecSel;
        ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
        CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, filtroMecanico, est);

        modelDias.setRowCount(0);
        modelDetalle.setRowCount(0);
        if (cal == null || cal.obtenerDias() == null) return;

        for (DiaCalendario d : cal.obtenerDias()) {
            int n = d.obtenerReservas().size();
            if (n > 0) {
                modelDias.addRow(new Object[]{
                        d.obtenerFecha(),
                        n,
                        d.obtenerTotalProgramadas(),
                        d.obtenerTotalEnProgreso(),
                        d.obtenerTotalFinalizadas(),
                        d.obtenerTotalEntregadas()
                });
            }
        }

        if (modelDias.getRowCount() > 0) {
            tablaDias.getSelectionModel().setSelectionInterval(0, 0);
            cargarDetalleSeleccion();
        }
    }
    
    public void bloquearAFiltroMecanico(String nombreMecanico) {
    if (nombreMecanico == null || nombreMecanico.isBlank()) return;
    // Asume que 'mecanico' es tu JComboBox<String> de filtro
    this.mecanico.removeAllItems();
    this.mecanico.addItem(nombreMecanico);
    this.mecanico.setSelectedItem(nombreMecanico);
    this.mecanico.setEnabled(false); // bloquea el cambio
    refrescar();
}


    private void cargarDetalleSeleccion() {
        int i = tablaDias.getSelectedRow();
        modelDetalle.setRowCount(0);
        if (i < 0) return;
        int m = tablaDias.convertRowIndexToModel(i);
        Object fecha = modelDias.getValueAt(m, 0);
        LocalDate dia = fecha instanceof LocalDate ? (LocalDate) fecha : LocalDate.parse(fecha.toString());
        LocalDate ini = dia;
        LocalDate fin = dia;
        String mecSel = (String) mecanico.getSelectedItem();
        String filtroMecanico = (mecSel == null || mecSel.equalsIgnoreCase("Todos")) ? null : mecSel;
        ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
        CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, filtroMecanico, est);
        if (cal == null || cal.obtenerDias() == null) return;
        List<DiaCalendario> dias = cal.obtenerDias();
        if (dias.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Reserva r : dias.get(0).obtenerReservas()) {
            String fechaTxt = r.getFecha().format(fmt);
            String clienteTxt = servicioReserva.obtenerNombreCliente(r.getClienteId());
            String mecanicoTxt = r.getMecanicoAsignado();
            String servicioTxt = servicioReserva.obtenerNombreServicio(r.getServicioId());
            String estadoTxt = r.getEstado().name();
            String idTxt = r.getId();
            modelDetalle.addRow(new Object[]{ fechaTxt, clienteTxt, mecanicoTxt, servicioTxt, estadoTxt, idTxt });
        }
    }

    private void cerrarSeleccionada() {
        int row = tablaDetalle.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una reserva en el detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String reservaId = String.valueOf(modelDetalle.getValueAt(row, 5));
        Map<String,Integer> sugeridas;
        try {
            sugeridas = servicioReserva.obtenerPartesRequeridasPorReserva(reservaId);
        } catch (Exception ex) {
            sugeridas = Collections.emptyMap();
        }
        Window win = SwingUtilities.getWindowAncestor(this);
        CerrarReservaDialog dlg = new CerrarReservaDialog(win, servicioReserva, parteRepo, reservaId, sugeridas);
        dlg.setVisible(true);
        refrescar();
    }

    private void rangoHoy() {
        LocalDate d = LocalDate.now();
        fechaInicio.setValue(toDate(d));
        fechaFin.setValue(toDate(d));
        asegurarOpcionesMecanicos();
        refrescar();
    }

    private void rangoMesActual() {
        LocalDate d = LocalDate.now();
        LocalDate i = d.withDayOfMonth(1);
        LocalDate f = d.withDayOfMonth(d.lengthOfMonth());
        fechaInicio.setValue(toDate(i));
        fechaFin.setValue(toDate(f));
        asegurarOpcionesMecanicos();
        refrescar();
    }

    private static LocalDate toLocal(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static java.util.Date toDate(LocalDate d) {
        return java.util.Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
