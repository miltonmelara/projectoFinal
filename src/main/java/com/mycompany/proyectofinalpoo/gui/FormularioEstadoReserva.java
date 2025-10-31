package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import java.time.LocalDate;
import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import java.time.format.DateTimeFormatter;
import java.awt.Dimension;


public class FormularioEstadoReserva extends JFrame {
    private ServicioReserva servicioReserva;
    private ReservaRepo reservaRepo;
    private ClienteRepo clienteRepo;
    private ServicioRepo servicioRepo;
    private JTable tablaReservas;
    private DefaultTableModel modeloTabla;
    private JComboBox<ReservaEstado> cmbEstados;
    private JTextArea txtResultado;
    private Consumer<LocalDate> onCambioReserva;
    
    // Constructor actualizado para incluir los repositorios adicionales
    public FormularioEstadoReserva(ServicioReserva servicioReserva, ReservaRepo reservaRepo, 
                                         ClienteRepo clienteRepo, ServicioRepo servicioRepo) {
        this.servicioReserva = servicioReserva;
        this.reservaRepo = reservaRepo;
        this.clienteRepo = clienteRepo;
        this.servicioRepo = servicioRepo;
        initComponents();
        cargarReservas();
    }

    public void setOnCambioReserva(Consumer<LocalDate> listener) {
        this.onCambioReserva = listener;
    }
    
    // Constructor compatible con versión anterior (fallback)
    public FormularioEstadoReserva(ServicioReserva servicioReserva, ReservaRepo reservaRepo) {
        this.servicioReserva = servicioReserva;
        this.reservaRepo = reservaRepo;
        this.clienteRepo = null;
        this.servicioRepo = null;
        initComponents();
        cargarReservas();
    }
    
    private void initComponents() {
        setTitle("Gestión de Estados de Reservas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior mejorado
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);
        
        // Tabla mejorada con nombres en lugar de IDs
        crearTablaReservas();
        JScrollPane scrollTabla = new JScrollPane(tablaReservas);
        scrollTabla.setPreferredSize(new Dimension(700, 250));
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Reservas Existentes"));
        add(scrollTabla, BorderLayout.CENTER);
        
        // Panel inferior con controles mejorados
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 248, 255));
        
        JLabel lblTitulo = new JLabel("Gestión de Estados de Reservas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(51, 102, 153));
        
        JButton btnActualizar = new JButton("🔄 Actualizar Lista");
        btnActualizar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnActualizar.addActionListener(e -> cargarReservas());
        
        panel.add(lblTitulo);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnActualizar);
        
        return panel;
    }
    
    private void crearTablaReservas() {
        // Columnas actualizadas para mostrar nombres
        String[] columnas = {"ID", "Cliente", "Servicio", "Fecha", "Hora", "Estado", "Mecánico"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaReservas.setRowHeight(25);
        tablaReservas.setGridColor(Color.LIGHT_GRAY);
        
        // Configurar anchos de columnas
        tablaReservas.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        tablaReservas.getColumnModel().getColumn(1).setPreferredWidth(150); // Cliente
        tablaReservas.getColumnModel().getColumn(2).setPreferredWidth(120); // Servicio
        tablaReservas.getColumnModel().getColumn(3).setPreferredWidth(80);  // Fecha
        tablaReservas.getColumnModel().getColumn(4).setPreferredWidth(60);  // Hora
        tablaReservas.getColumnModel().getColumn(5).setPreferredWidth(100); // Estado
        tablaReservas.getColumnModel().getColumn(6).setPreferredWidth(120); // Mecánico
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de controles
        JPanel panelControl = new JPanel(new FlowLayout());
        panelControl.setBorder(BorderFactory.createTitledBorder("Acciones"));
        
        panelControl.add(new JLabel("Nuevo Estado:"));
        cmbEstados = new JComboBox<>(ReservaEstado.values());
        cmbEstados.setPreferredSize(new Dimension(300, 32));
        EstiloCombos.aplicarDarkAzul(cmbEstados);
        panelControl.add(cmbEstados);
        
        // Botones mejorados
        JButton btnCambiarEstado = crearBoton("Cambiar Estado", new Color(40, 167, 69));
        JButton btnEliminarReserva = crearBoton("Eliminar Reserva", new Color(220, 53, 69));
        JButton btnCerrar = crearBoton("Cerrar", new Color(108, 117, 125));
        
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnEliminarReserva.addActionListener(e -> eliminarReserva());
        btnCerrar.addActionListener(e -> dispose());

        panelControl.add(btnCambiarEstado);
        panelControl.add(btnEliminarReserva);
        panelControl.add(btnCerrar);
        
        // Área de resultados mejorada
        txtResultado = new JTextArea(4, 50);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtResultado.setBackground(new Color(248, 248, 248));
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Mensajes del Sistema"));
        
        panel.add(panelControl, BorderLayout.NORTH);
        panel.add(scrollResultado, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createRaisedBevelBorder());
        boton.setPreferredSize(new Dimension(120, 30));
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });
        
        return boton;
    }
    
    private void cargarReservas() {
        modeloTabla.setRowCount(0);
        List<Reserva> reservas = reservaRepo.findAll();
        
        for (Reserva reserva : reservas) {
            // Obtener nombre del cliente
            String nombreCliente = obtenerNombreCliente(reserva.getClienteId());
            
            // Obtener nombre del servicio
            String nombreServicio = obtenerNombreServicio(reserva.getServicioId());
            
            Object[] fila = {
                reserva.getId(),
                nombreCliente,
                nombreServicio,
                reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                reserva.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")),
                reserva.getEstado(),
                reserva.getMecanicoAsignado()
            };
            modeloTabla.addRow(fila);
        }
        
        mostrarMensaje(String.format("✅ %s - Reservas cargadas: %d", 
                                   java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                   reservas.size()), 
                     new Color(0, 120, 0));
    }
    
    private String obtenerNombreCliente(String clienteId) {
        if (clienteRepo != null) {
            return clienteRepo.findById(clienteId)
                    .map(Cliente::getNombre)
                    .orElse("Cliente no encontrado (ID: " + clienteId + ")");
        } else {
            return "Cliente ID: " + clienteId;
        }
    }
    
    private String obtenerNombreServicio(String servicioId) {
        if (servicioRepo != null) {
            return servicioRepo.findById(servicioId)
                    .map(Servicio::getNombre)
                    .orElse("Servicio no encontrado (ID: " + servicioId + ")");
        } else {
            return "Servicio ID: " + servicioId;
        }
    }
    
    private void cambiarEstado() {
        int filaSeleccionada = tablaReservas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione una reserva de la tabla para cambiar su estado", 
                "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String reservaId = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
            String nombreCliente = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            ReservaEstado nuevoEstado = (ReservaEstado) cmbEstados.getSelectedItem();
            ReservaEstado estadoAnterior = (ReservaEstado) modeloTabla.getValueAt(filaSeleccionada, 5);
            
            // Confirmar el cambio
            int confirmacion = JOptionPane.showConfirmDialog(this,
                String.format("<html><b>¿Confirmar cambio de estado?</b><br><br>" +
                             "Reserva: %s<br>" +
                             "Cliente: %s<br>" +
                             "Estado actual: %s<br>" +
                             "Nuevo estado: <b>%s</b></html>",
                             reservaId, nombreCliente, estadoAnterior, nuevoEstado),
                "Confirmar Cambio de Estado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Realizar el cambio
            Reserva reservaActualizada = servicioReserva.changeEstado(reservaId, nuevoEstado);
            
            // Actualizar tabla
            modeloTabla.setValueAt(nuevoEstado, filaSeleccionada, 5);
            
            // Mostrar confirmación
            String mensaje = String.format("✅ %s - Estado actualizado exitosamente:\n" +
                                         "Reserva: %s | Cliente: %s\n" +
                                         "Estado: %s → %s",
                                         java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                         reservaId, nombreCliente, estadoAnterior, nuevoEstado);
            
            mostrarMensaje(mensaje, new Color(0, 120, 0));
            if (onCambioReserva != null && reservaActualizada != null && reservaActualizada.getFecha() != null) {
                onCambioReserva.accept(reservaActualizada.getFecha().toLocalDate());
            }
            
        } catch (Exception ex) {
            String mensajeError = "❌ Error cambiando estado: " + ex.getMessage();
            mostrarMensaje(mensajeError, new Color(220, 53, 69));
            
            JOptionPane.showMessageDialog(this, 
                "Error al cambiar el estado de la reserva:\n" + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            
            // Recargar para mantener consistencia
            cargarReservas();
        }
    }
    
    private void eliminarReserva() {
        int filaSeleccionada = tablaReservas.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione una reserva de la tabla para eliminar", 
                "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String reservaId = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
            String nombreCliente = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String nombreServicio = (String) modeloTabla.getValueAt(filaSeleccionada, 2);
            String fecha = (String) modeloTabla.getValueAt(filaSeleccionada, 3);
            String hora = (String) modeloTabla.getValueAt(filaSeleccionada, 4);
            String estado = modeloTabla.getValueAt(filaSeleccionada, 5).toString();
            
            // Confirmación con información detallada
            int confirmacion = JOptionPane.showConfirmDialog(this,
                String.format("<html><b>¿Está seguro que desea eliminar esta reserva?</b><br><br>" +
                             "<b>ID:</b> %s<br>" +
                             "<b>Cliente:</b> %s<br>" +
                             "<b>Servicio:</b> %s<br>" +
                             "<b>Fecha:</b> %s %s<br>" +
                             "<b>Estado:</b> %s<br><br>" +
                             "<font color='red'>Esta acción no se puede deshacer.</font></html>",
                             reservaId, nombreCliente, nombreServicio, fecha, hora, estado),
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                // Eliminar la reserva
                servicioReserva.deleteReserva(reservaId);
                
                // Mostrar confirmación
                String mensaje = String.format("✅ %s - Reserva eliminada exitosamente:\n" +
                                             "ID: %s | Cliente: %s\n" +
                                             "Servicio: %s | Fecha: %s %s",
                                             java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                             reservaId, nombreCliente, nombreServicio, fecha, hora);
                
                mostrarMensaje(mensaje, new Color(0, 120, 0));
                
                // Recargar la tabla
                cargarReservas();
                if (onCambioReserva != null) {
                    LocalDate fechaLocal = null;
                    try {
                        fechaLocal = pantallaAFecha(fecha);
                    } catch (Exception ignored) {}
                    onCambioReserva.accept(fechaLocal);
                }
            }
            
        } catch (Exception ex) {
            String mensajeError = "❌ Error eliminando reserva: " + ex.getMessage();
            mostrarMensaje(mensajeError, new Color(220, 53, 69));
            
            JOptionPane.showMessageDialog(this, 
                "Error al eliminar la reserva:\n" + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarMensaje(String mensaje, Color color) {
        SwingUtilities.invokeLater(() -> {
            txtResultado.setForeground(color);
            txtResultado.setText(mensaje);
            txtResultado.setCaretPosition(0);
        });
    }

    private LocalDate pantallaAFecha(String texto) {
        if (texto == null) return null;
        try {
            return LocalDate.parse(texto.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }
}
