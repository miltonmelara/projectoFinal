package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.CalendarioReservas;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DiaCalendario;

public class VistaCalendario extends JPanel {
    private final ServicioReserva servicioReserva;
    private final JSpinner fechaInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner fechaFin = new JSpinner(new SpinnerDateModel());
    private final JTextField mecanico = new JTextField();
    private final JComboBox<ReservaEstado> estado = new JComboBox<>();
    private final JButton btnAplicar = new JButton("Aplicar");
    private final JButton btnHoy = new JButton("Hoy");
    private final JButton btnMes = new JButton("Mes actual");
    private final DefaultTableModel modelDias = new DefaultTableModel(new Object[]{"Fecha","Total","Programadas","En Progreso","Finalizadas","Entregadas"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tablaDias = new JTable(modelDias);
    private final DefaultTableModel modelDetalle = new DefaultTableModel(new Object[]{"Reserva"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tablaDetalle = new JTable(modelDetalle);

    public VistaCalendario(ServicioReserva servicioReserva) {
        this.servicioReserva = servicioReserva;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel filtros = new JPanel(new GridBagLayout());
filtros.setBorder(BorderFactory.createTitledBorder("Filtros"));
GridBagConstraints gc = new GridBagConstraints();
gc.insets = new Insets(6,6,6,6);
gc.fill = GridBagConstraints.HORIZONTAL;

fechaInicio.setEditor(new JSpinner.DateEditor(fechaInicio,"yyyy-MM-dd"));
fechaFin.setEditor(new JSpinner.DateEditor(fechaFin,"yyyy-MM-dd"));
estado.addItem(null);
for (ReservaEstado e : ReservaEstado.values()) estado.addItem(e);

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
gc.gridx=0; gc.gridy=2; gc.gridwidth=4; filtros.add(acciones, gc);


        tablaDias.setRowHeight(22);
        tablaDias.setFillsViewportHeight(true);
        tablaDetalle.setRowHeight(22);
        tablaDetalle.setFillsViewportHeight(true);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaDias), new JScrollPane(tablaDetalle));
        split.setResizeWeight(0.6);

        add(filtros, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        btnAplicar.addActionListener(e -> refrescar());
        estado.addActionListener(e -> refrescar());
mecanico.addActionListener(e -> refrescar());
        btnHoy.addActionListener(e -> rangoHoy());
        btnMes.addActionListener(e -> rangoMesActual());
        tablaDias.getSelectionModel().addListSelectionListener(e -> cargarDetalleSeleccion());

        LocalDate hoy = LocalDate.now();
        fechaInicio.setValue(Date.from(hoy.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        fechaFin.setValue(Date.from(hoy.withDayOfMonth(hoy.lengthOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        refrescar();
    }

    public void refrescarDesdeExterno() {
    SwingUtilities.invokeLater(this::refrescar);
}

    private void refrescar() {
    LocalDate ini = toLocal((Date) fechaInicio.getValue());
    LocalDate fin = toLocal((Date) fechaFin.getValue());
    if (ini.isAfter(fin)) {
        LocalDate tmp = ini;
        ini = fin;
        fin = tmp;
        fechaInicio.setValue(toDate(ini));
        fechaFin.setValue(toDate(fin));
    }
    String mec = mecanico.getText().trim();
    ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
    CalendarioReservas cal = servicioReserva.generarCalendario(
            ini, fin, mec.isEmpty() ? null : mec, est
    );

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


    private void cargarDetalleSeleccion() {
        int i = tablaDias.getSelectedRow();
        modelDetalle.setRowCount(0);
        if (i < 0) return;
        int m = tablaDias.convertRowIndexToModel(i);
        Object fecha = modelDias.getValueAt(m, 0);
        LocalDate dia = fecha instanceof LocalDate ? (LocalDate) fecha : LocalDate.parse(fecha.toString());
        LocalDate ini = dia;
        LocalDate fin = dia;
        String mec = mecanico.getText().trim();
        ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
        CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, mec.isEmpty()?null:mec, est);
        if (cal == null || cal.obtenerDias() == null) return;
        List<DiaCalendario> dias = cal.obtenerDias();
        if (dias.isEmpty()) return;
        for (Object r : dias.get(0).obtenerReservas()) {
            modelDetalle.addRow(new Object[]{String.valueOf(r)});
        }
    }

    private void rangoHoy() {
        LocalDate d = LocalDate.now();
        fechaInicio.setValue(toDate(d));
        fechaFin.setValue(toDate(d));
        refrescar();
    }

    private void rangoMesActual() {
        LocalDate d = LocalDate.now();
        LocalDate i = d.withDayOfMonth(1);
        LocalDate f = d.withDayOfMonth(d.lengthOfMonth());
        fechaInicio.setValue(toDate(i));
        fechaFin.setValue(toDate(f));
        refrescar();
    }

    private static LocalDate toLocal(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
