package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioCliente;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.HistorialCliente;
import java.time.format.DateTimeFormatter;

public class FormularioHistorial extends JFrame {
    private ServicioCliente servicioCliente;
    private ClienteRepo clienteRepo;
    private JComboBox<ClienteItem> cmbClientes;
    private JTextArea txtHistorial;
    
    public FormularioHistorial(ServicioCliente servicioCliente, ClienteRepo clienteRepo) {
        this.servicioCliente = servicioCliente;
        this.clienteRepo = clienteRepo;
        initComponents();
        cargarClientes();
    }
    
    private void initComponents() {
        setTitle("Historial de Cliente");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Título
        JLabel lblTitulo = new JLabel("Consultar Historial de Cliente");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panelSuperior.add(lblTitulo, gbc);
        
        // Selector de cliente
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panelSuperior.add(new JLabel("Cliente:"), gbc);
        
        gbc.gridx = 1;
        cmbClientes = new JComboBox<>();
        cmbClientes.setPreferredSize(new Dimension(300, 25));
        panelSuperior.add(cmbClientes, gbc);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnConsultar = new JButton("Consultar Historial");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCerrar = new JButton("Cerrar");
        
        btnConsultar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consultarHistorial();
            }
        });
        
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtHistorial.setText("");
            }
        });
        
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panelBotones.add(btnConsultar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrar);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panelSuperior.add(panelBotones, gbc);
        
        // Área de historial
        txtHistorial = new JTextArea(20, 50);
        txtHistorial.setEditable(false);
        txtHistorial.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtHistorial.setBackground(Color.WHITE);
        JScrollPane scrollHistorial = new JScrollPane(txtHistorial);
        scrollHistorial.setBorder(BorderFactory.createTitledBorder("Historial del Cliente"));
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollHistorial, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void cargarClientes() {
        List<Cliente> clientes = clienteRepo.findAll();
        for (Cliente cliente : clientes) {
            cmbClientes.addItem(new ClienteItem(cliente));
        }
    }
    
    private void consultarHistorial() {
        ClienteItem clienteItem = (ClienteItem) cmbClientes.getSelectedItem();
        
        if (clienteItem == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            HistorialCliente historial = servicioCliente.getHistorial(clienteItem.cliente.getId());
            
            StringBuilder sb = new StringBuilder();
            
            // Información del cliente
            Cliente cliente = historial.getCliente();
            sb.append("═══════════════════════════════════════════════════════════════\n");
            sb.append("                    HISTORIAL DE CLIENTE\n");
            sb.append("═══════════════════════════════════════════════════════════════\n\n");
            
            sb.append("INFORMACIÓN DEL CLIENTE:\n");
            sb.append("─────────────────────────────────────────────────────────────\n");
            sb.append("ID: ").append(cliente.getId()).append("\n");
            sb.append("Nombre: ").append(cliente.getNombre()).append("\n");
            sb.append("Contacto: ").append(cliente.getContacto()).append("\n");
            sb.append("Vehículo: ").append(cliente.getMarcaAuto()).append(" ").append(cliente.getModeloAuto());
            sb.append(" (").append(cliente.getAnioAuto()).append(")\n\n");
            
            // Reservas
            List<Reserva> reservas = historial.getReservas();
            sb.append("RESERVAS (").append(reservas.size()).append(" total):\n");
            sb.append("─────────────────────────────────────────────────────────────\n");
            
            if (reservas.isEmpty()) {
                sb.append("No tiene reservas registradas.\n");
            } else {
                for (int i = 0; i < reservas.size(); i++) {
                    Reserva reserva = reservas.get(i);
                    sb.append(String.format("%d. ID: %s\n", i + 1, reserva.getId()));
                    sb.append(String.format("   Fecha: %s\n", reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                    sb.append(String.format("   Estado: %s\n", reserva.getEstado()));
                    sb.append(String.format("   Mecánico: %s\n", reserva.getMecanicoAsignado()));
                    sb.append(String.format("   Servicio ID: %s\n", reserva.getServicioId()));
                    if (i < reservas.size() - 1) sb.append("\n");
                }
            }
            
            sb.append("\n");
            
            // Servicios
            List<Servicio> servicios = historial.getServicios();
            sb.append("SERVICIOS UTILIZADOS (").append(servicios.size()).append(" únicos):\n");
            sb.append("─────────────────────────────────────────────────────────────\n");
            
            if (servicios.isEmpty()) {
                sb.append("No ha utilizado servicios aún.\n");
            } else {
                for (int i = 0; i < servicios.size(); i++) {
                    Servicio servicio = servicios.get(i);
                    sb.append(String.format("%d. %s\n", i + 1, servicio.getNombre()));
                    sb.append(String.format("   Duración: %d minutos\n", servicio.getDuracionMin()));
                    sb.append(String.format("   Precio: $%.2f\n", servicio.getPrecio()));
                    sb.append(String.format("   ID: %s\n", servicio.getId()));
                    if (i < servicios.size() - 1) sb.append("\n");
                }
            }
            
            sb.append("\n═══════════════════════════════════════════════════════════════\n");
            sb.append("Consulta realizada: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            sb.append("═══════════════════════════════════════════════════════════════");
            
            txtHistorial.setText(sb.toString());
            txtHistorial.setCaretPosition(0);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Clase helper para ComboBox
    private static class ClienteItem {
        Cliente cliente;
        
        ClienteItem(Cliente cliente) {
            this.cliente = cliente;
        }
        
        @Override
        public String toString() {
            return cliente.getNombre() + " - " + cliente.getMarcaAuto() + " " + 
                   cliente.getModeloAuto() + " (" + cliente.getAnioAuto() + ")";
        }
    }
}