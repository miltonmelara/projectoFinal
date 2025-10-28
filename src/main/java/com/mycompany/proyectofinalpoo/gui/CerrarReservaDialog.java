package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.mycompany.proyectofinalpoo.ConsumoParte;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;

public class CerrarReservaDialog extends JDialog {
    private final ServicioReserva servicioReserva;
    private final ParteRepo parteRepo;
    private final String reservaId;

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Parte","Cantidad"},0){
        public boolean isCellEditable(int r,int c){ return c==0 || c==1; }
    };
    private final JTable tabla = new JTable(model);
    private final JComboBox<ReservaEstado> cboEstado = new JComboBox<>(ReservaEstado.values());
    private final JButton btnAgregar = new JButton("Agregar pieza");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnGuardar = new JButton("Guardar y cerrar");
    private final JButton btnCancelar = new JButton("Cancelar");

    public CerrarReservaDialog(Window owner, ServicioReserva servicioReserva, ParteRepo parteRepo, String reservaId, Map<String,Integer> sugeridas) {
        super(owner, "Cerrar reserva", ModalityType.APPLICATION_MODAL);
        this.servicioReserva = servicioReserva;
        this.parteRepo = parteRepo;
        this.reservaId = reservaId;

        setLayout(new BorderLayout(10,10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        top.add(new JLabel("Estado final"));
        top.add(cboEstado);

        tabla.setRowHeight(22);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        acciones.add(btnAgregar);
        acciones.add(btnEliminar);
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);
        add(acciones, BorderLayout.SOUTH);

        if (sugeridas != null) {
            for (Map.Entry<String,Integer> e : sugeridas.entrySet()) {
                String nombre = parteRepo.findById(e.getKey()).map(p -> p.getNombre()).orElse("[" + e.getKey() + "]");
                Integer qty = e.getValue() == null ? 0 : e.getValue();
                model.addRow(new Object[]{nombre, qty});
            }
        }

        btnAgregar.addActionListener(e -> agregarFila());
        btnEliminar.addActionListener(e -> eliminarFila());
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());

        setSize(600, 420);
        setLocationRelativeTo(owner);
    }

    private void agregarFila() {
        java.util.List<String> nombres = new java.util.ArrayList<>();
        for (com.mycompany.proyectofinalpoo.Parte p : parteRepo.findAll()) nombres.add(p.getNombre());
        String sel = (String) JOptionPane.showInputDialog(this, "Parte", "Seleccionar parte", JOptionPane.PLAIN_MESSAGE, null, nombres.toArray(new String[0]), null);
        if (sel != null) model.addRow(new Object[]{sel, 1});
    }

    private void eliminarFila() {
        int i = tabla.getSelectedRow();
        if (i >= 0) model.removeRow(i);
    }

    private void guardar() {
        List<ConsumoParte> consumos = new ArrayList<>();
        for (int i=0;i<model.getRowCount();i++) {
            String nombre = String.valueOf(model.getValueAt(i,0));
            String parteId = null;
            for (com.mycompany.proyectofinalpoo.Parte p : parteRepo.findAll()) {
                if (nombre.equals(p.getNombre())) { parteId = p.getId(); break; }
            }
            if (parteId == null) continue;
            int qty = 0;
            Object v = model.getValueAt(i,1);
            if (v != null) {
                try { qty = Integer.parseInt(String.valueOf(v)); } catch (Exception ex) { qty = 0; }
            }
            if (qty <= 0) continue;
            ConsumoParte c = new ConsumoParte();
            c.setParteId(parteId);
            c.setCantidad(qty);
            consumos.add(c);
        }
        ReservaEstado estadoFinal = (ReservaEstado) cboEstado.getSelectedItem();
        servicioReserva.cerrarReservaConConsumos(reservaId, consumos, estadoFinal);
        dispose();
    }
}
