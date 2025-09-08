package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioInventario;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioCliente;

public class MenuPrincipal extends JFrame {
    private ServicioInventario servicioInventario;
    private ServicioReserva servicioReserva;
    private ServicioCliente servicioCliente;
    private ClienteRepo clienteRepo;
    private ServicioRepo servicioRepo;
    private ReservaRepo reservaRepo;
    
    public MenuPrincipal() {
        initRepositorios();
        initComponents();
    }
    
    private void initRepositorios() {
        Path dataDir = Path.of("data");
        clienteRepo = new ClienteFileRepo(dataDir);
        ParteRepo parteRepo = new ParteFileRepo(dataDir);
        servicioRepo = new ServicioFileRepo(dataDir);
        reservaRepo = new ReservaFileRepo(dataDir);
        
        // Inicializar servicios
        servicioInventario = new ServicioInventario(parteRepo);
        servicioReserva = new ServicioReserva(reservaRepo, clienteRepo, servicioRepo, parteRepo);
        servicioCliente = new ServicioCliente(clienteRepo, reservaRepo, servicioRepo);
    }
    
    private void initComponents() {
        setTitle("Sistema de Taller - Menú Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior con título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(51, 102, 153));
        JLabel lblTitulo = new JLabel("SISTEMA DE MANEJO DE INVENTARIO Y CRM");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelTitulo.add(lblTitulo);
        
        // Panel central con botones
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Botones principales
        JButton btnInventario = new JButton("1. Agregar Ítem al Inventario");
        JButton btnReserva = new JButton("2. Crear Nueva Reserva");
        JButton btnEstado = new JButton("3. Cambiar Estado de Reserva");
        JButton btnHistorial = new JButton("4. Consultar Historial de Cliente");
        JButton btnSalir = new JButton("Salir");
        
        // Estilo de botones
        Dimension buttonSize = new Dimension(300, 40);
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        
        btnInventario.setPreferredSize(buttonSize);
        btnInventario.setFont(buttonFont);
        btnReserva.setPreferredSize(buttonSize);
        btnReserva.setFont(buttonFont);
        btnEstado.setPreferredSize(buttonSize);
        btnEstado.setFont(buttonFont);
        btnHistorial.setPreferredSize(buttonSize);
        btnHistorial.setFont(buttonFont);
        btnSalir.setPreferredSize(buttonSize);
        btnSalir.setFont(buttonFont);
        btnSalir.setBackground(new Color(220, 53, 69));
        btnSalir.setForeground(Color.WHITE);
        
        // Eventos de botones
        btnInventario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirFormularioInventario();
            }
        });
        
        btnReserva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirFormularioReserva();
            }
        });
        
        btnEstado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirFormularioEstado();
            }
        });
        
        btnHistorial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirFormularioHistorial();
            }
        });
        
        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int resultado = JOptionPane.showConfirmDialog(
                    MenuPrincipal.this,
                    "¿Está seguro que desea salir?",
                    "Confirmar Salida",
                    JOptionPane.YES_NO_OPTION
                );
                if (resultado == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        
        // Añadir botones al panel
        gbc.gridx = 0; gbc.gridy = 0;
        panelCentral.add(btnInventario, gbc);
        gbc.gridy = 1;
        panelCentral.add(btnReserva, gbc);
        gbc.gridy = 2;
        panelCentral.add(btnEstado, gbc);
        gbc.gridy = 3;
        panelCentral.add(btnHistorial, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 10, 10, 10);
        panelCentral.add(btnSalir, gbc);
        
        // Panel inferior con información
        JPanel panelInfo = new JPanel();
        panelInfo.setBackground(new Color(248, 249, 250));
        JLabel lblInfo = new JLabel("Desarrollado por Milton Melara y Jose Daniel Cuá");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfo.setForeground(Color.GRAY);
        panelInfo.add(lblInfo);
        
        add(panelTitulo, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInfo, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void abrirFormularioInventario() {
        FormularioInventario form = new FormularioInventario(servicioInventario);
        form.setVisible(true);
    }
    
    private void abrirFormularioReserva() {
        FormularioReserva form = new FormularioReserva(servicioReserva, clienteRepo, servicioRepo);
        form.setVisible(true);
    }
    
    private void abrirFormularioEstado() {
        FormularioEstadoReserva form = new FormularioEstadoReserva(servicioReserva, reservaRepo);
        form.setVisible(true);
    }
    
    private void abrirFormularioHistorial() {
        FormularioHistorial form = new FormularioHistorial(servicioCliente, clienteRepo);
        form.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Configurar Look and Feel
        // Configurar Look and Feel
try {
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
        }
    }
} catch (Exception e) {
    // Si hay error, usar el look and feel por defecto
    System.out.println("No se pudo cargar Nimbus Look and Feel");
}
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MenuPrincipal().setVisible(true);
            }
        });
    }
}