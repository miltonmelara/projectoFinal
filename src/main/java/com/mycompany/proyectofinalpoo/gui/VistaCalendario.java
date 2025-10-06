package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Fecha","Total","Programadas","En Progreso","Finalizadas","Entregadas"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tabla = new JTable(model);

    public VistaCalendario(ServicioReserva servicioReserva) {
        this.servicioReserva = servicioReserva;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel filtros = new JPanel(new GridLayout(2,5,8,8));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        filtros.add(new JLabel("Inicio"));
        filtros.add(new JLabel("Fin"));
        filtros.add(new JLabel("Mecánico"));
        filtros.add(new JLabel("Estado"));
        filtros.add(new JLabel(""));
        fechaInicio.setEditor(new JSpinner.DateEditor(fechaInicio,"yyyy-MM-dd"));
        fechaFin.setEditor(new JSpinner.DateEditor(fechaFin,"yyyy-MM-dd"));
        estado.addItem(null);
        for (ReservaEstado e : ReservaEstado.values()) estado.addItem(e);
        JButton btnAplicar = new JButton("Aplicar");
        filtros.add(fechaInicio);
        filtros.add(fechaFin);
        filtros.add(mecanico);
        filtros.add(estado);
        filtros.add(btnAplicar);
        add(filtros, BorderLayout.NORTH);
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        btnAplicar.addActionListener(e -> refrescar());
        fechaInicio.setValue(new Date());
        fechaFin.setValue(new Date());
        refrescar();
    }

    private void refrescar() {
        LocalDate ini = ((Date)fechaInicio.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fin = ((Date)fechaFin.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String mec = mecanico.getText().trim();
        ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
        CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, mec.isEmpty()?null:mec, est);
        model.setRowCount(0);
        for (DiaCalendario d : cal.obtenerDias()) {
            model.addRow(new Object[]{d.obtenerFecha(), d.obtenerReservas().size(), d.obtenerTotalProgramadas(), d.obtenerTotalEnProgreso(), d.obtenerTotalFinalizadas(), d.obtenerTotalEntregadas()});
        }
    }
}
