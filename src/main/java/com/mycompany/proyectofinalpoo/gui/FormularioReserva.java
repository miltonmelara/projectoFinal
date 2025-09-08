package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.List;
import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import java.time.format.DateTimeFormatter;

public class FormularioReserva extends JFrame {
    private ServicioReserva servicioReserva;
    private ClienteRepo clienteRepo;
    private ServicioRepo servicioRepo;
    private JComboBox<ClienteItem> cmbClientes;
    private JComboBox<ServicioItem> cmbServicios;
    private JTextField txtMecanico;
    private JSpinner spnDias;
    private JTextArea txtResultado;
    
    public FormularioReserva(ServicioReserva servicioReserva, ClienteRepo clienteRepo, ServicioRepo servicioRepo) {
        this.servicioReserva = servicioReserva;
        this.clienteRepo = clienteRepo;
        this.servicioRepo = servicioRepo;
        initComponents();
        cargarDatos();
    }
    
    private void initComponents() {
        setTitle("Crear Nueva Reserva");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Título
        JLabel lblTitulo = new JLabel("Crear Nueva Reserva");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelPrincipal.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Campos del formulario
        gbc.gridx = 0; gbc.gridy = 1;
        panelPrincipal.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1;
        cmbClientes = new JComboBox<>();
        cmbClientes.setPreferredSize(new Dimension(250, 25));
        panelPrincipal.add(cmbClientes, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(new JLabel("Servicio:"), gbc);
        gbc.gridx = 1;
        cmbServicios = new JComboBox<>();
        cmbServicios.setPreferredSize(new Dimension(250, 25));
        panelPrincipal.add(cmbServicios, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panelPrincipal.add(new JLabel("Mecánico Asignado:"), gbc);
        gbc.gridx = 1;
        txtMecanico = new JTextField(20);
        panelPrincipal.add(txtMecanico, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panelPrincipal.add(new JLabel("Días desde hoy:"), gbc);
        gbc.gridx = 1;
        spnDias = new JSpinner(new SpinnerNumberModel(1, 0, 365, 1));
        panelPrincipal.add(spnDias, gbc);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCrear = new JButton("Crear Reserva");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCerrar = new JButton("Cerrar");
        
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crearReserva();
            }
        });
        
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });
        
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panelBotones.add(btnCrear);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrar);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panelPrincipal.add(panelBotones, gbc);
        
        // Área de resultados
        txtResultado = new JTextArea(5, 30);
        txtResultado.setEditable(false);
        txtResultado.setBackground(Color.LIGHT_GRAY);
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(scrollResultado, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void cargarDatos() {
        // Cargar clientes
        List<Cliente> clientes = clienteRepo.findAll();
        for (Cliente cliente : clientes) {
            cmbClientes.addItem(new ClienteItem(cliente));
        }
        
        // Cargar servicios
        List<Servicio> servicios = servicioRepo.findAll();
        for (Servicio servicio : servicios) {
            cmbServicios.addItem(new ServicioItem(servicio));
        }
    }
    
    private void crearReserva() {
        try {
            ClienteItem clienteItem = (ClienteItem) cmbClientes.getSelectedItem();
            ServicioItem servicioItem = (ServicioItem) cmbServicios.getSelectedItem();
            String mecanico = txtMecanico.getText().trim();
            int dias = (Integer) spnDias.getValue();
            
            if (clienteItem == null || servicioItem == null) {
                JOptionPane.showMessageDialog(this, "Seleccione cliente y servicio", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (mecanico.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el mecánico asignado", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDateTime fecha = LocalDateTime.now().plusDays(dias);
            
            Reserva nuevaReserva = servicioReserva.createReserva(
                clienteItem.cliente.getId(),
                servicioItem.servicio.getId(),
                fecha,
                mecanico
            );
            
            txtResultado.setText("✓ Reserva creada exitosamente:\n" + 
                               "Cliente: " + clienteItem.cliente.getNombre() + "\n" +
                               "Servicio: " + servicioItem.servicio.getNombre() + "\n" +
                               "Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                               "Mecánico: " + mecanico + "\n" +
                               "ID Reserva: " + nuevaReserva.getId());
            
            limpiarCampos();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limpiarCampos() {
        if (cmbClientes.getItemCount() > 0) cmbClientes.setSelectedIndex(0);
        if (cmbServicios.getItemCount() > 0) cmbServicios.setSelectedIndex(0);
        txtMecanico.setText("");
        spnDias.setValue(1);
    }
    
    // Clases helper para ComboBox
    private static class ClienteItem {
        Cliente cliente;
        
        ClienteItem(Cliente cliente) {
            this.cliente = cliente;
        }
        
        @Override
        public String toString() {
            return cliente.getNombre() + " (" + cliente.getMarcaAuto() + " " + cliente.getModeloAuto() + ")";
        }
    }
    
    private static class ServicioItem {
        Servicio servicio;
        
        ServicioItem(Servicio servicio) {
            this.servicio = servicio;
        }
        
        @Override
        public String toString() {
            return servicio.getNombre() + " - $" + servicio.getPrecio() + " (" + servicio.getDuracionMin() + " min)";
        }
    }
}