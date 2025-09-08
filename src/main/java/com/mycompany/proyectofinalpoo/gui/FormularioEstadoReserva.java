package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import java.time.format.DateTimeFormatter;

public class FormularioEstadoReserva extends JFrame {
    private ServicioReserva servicioReserva;
    private ReservaRepo reservaRepo;
    private JTable tablaReservas;
    private DefaultTableModel modeloTabla;
    private JComboBox<ReservaEstado> cmbEstados;
    private JTextArea txtResultado;
    
    public FormularioEstadoReserva(ServicioReserva servicioReserva, ReservaRepo reservaRepo) {
        this.servicioReserva = servicioReserva;
        this.reservaRepo = reservaRepo;
        initComponents();
        cargarReservas();
    }
    
    private void initComponents() {
        setTitle("Cambiar Estado de Reservas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior
        JPanel panelSuperior = new JPanel(new FlowLayout());
        JLabel lblTitulo = new JLabel("Gestión de Estados de Reservas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panelSuperior.add(lblTitulo);
        
        // Tabla de reservas
        String[] columnas = {"ID", "Cliente ID", "Servicio ID", "Fecha", "Estado", "Mecánico"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTabla = new JScrollPane(tablaReservas);
        scrollTabla.setPreferredSize(new Dimension(600, 200));
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Reservas Existentes"));
        
        // Panel de control
        JPanel panelControl = new JPanel(new FlowLayout());
        panelControl.add(new JLabel("Nuevo Estado:"));
        
        cmbEstados = new JComboBox<>(ReservaEstado.values());
        panelControl.add(cmbEstados);
        
        JButton btnCambiar = new JButton("Cambiar Estado");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnCerrar = new JButton("Cerrar");
        
        btnCambiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarEstado();
            }
        });
        
        btnRefrescar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarReservas();
            }
        });
        
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panelControl.add(btnCambiar);
        panelControl.add(btnRefrescar);
        panelControl.add(btnCerrar);
        
        // Área de resultados
        txtResultado = new JTextArea(4, 30);
        txtResultado.setEditable(false);
        txtResultado.setBackground(Color.LIGHT_GRAY);
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        
        // Layout
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelControl, BorderLayout.NORTH);
        panelInferior.add(scrollResultado, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void cargarReservas() {
        modeloTabla.setRowCount(0);
        List<Reserva> reservas = reservaRepo.findAll();
        
        for (Reserva reserva : reservas) {
            Object[] fila = {
                reserva.getId(),
                reserva.getClienteId(),
                reserva.getServicioId(),
                reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                reserva.getEstado(),
                reserva.getMecanicoAsignado()
            };
            modeloTabla.addRow(fila);
        }
        
        txtResultado.setText("Reservas cargadas: " + reservas.size());
    }
    
    private void cambiarEstado() {
        int filaSeleccionada = tablaReservas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String reservaId = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
            ReservaEstado nuevoEstado = (ReservaEstado) cmbEstados.getSelectedItem();
            ReservaEstado estadoAnterior = (ReservaEstado) modeloTabla.getValueAt(filaSeleccionada, 4);
            
            Reserva reservaActualizada = servicioReserva.changeEstado(reservaId, nuevoEstado);
            
            // Actualizar tabla
            modeloTabla.setValueAt(nuevoEstado, filaSeleccionada, 4);
            
            txtResultado.setText("✓ Estado actualizado exitosamente:\n" +
                               "Reserva ID: " + reservaId + "\n" +
                               "Estado anterior: " + estadoAnterior + "\n" +
                               "Nuevo estado: " + nuevoEstado + "\n" +
                               "Fecha: " + java.time.LocalDateTime.now());
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            // Recargar en caso de error para mantener consistencia
            cargarReservas();
        }
    }
}