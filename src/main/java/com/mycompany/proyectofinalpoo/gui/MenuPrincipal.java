package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private ParteRepo parteRepo;

    public MenuPrincipal() {
        TemaNeoBlue.aplicar();
        initRepositorios();
        initComponents();
        TemaNeoBlue.estilizar(this.getContentPane());
    }

    // === Repositorios y servicios ===
    private void initRepositorios() {
        Path dataDir = Path.of("data");
        clienteRepo = new ClienteFileRepo(dataDir);
        parteRepo = new ParteFileRepo(dataDir);
        servicioRepo = new ServicioFileRepo(dataDir);
        reservaRepo = new ReservaFileRepo(dataDir);

        servicioInventario = new ServicioInventario(parteRepo);
        var consumoRepo = new com.mycompany.proyectofinalpoo.repo.file.ConsumoParteFileRepo(dataDir);
        var usuarioRepo = new com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo(dataDir);

        servicioReserva = new ServicioReserva(reservaRepo, servicioRepo, parteRepo, clienteRepo, usuarioRepo, consumoRepo);
        servicioCliente = new ServicioCliente(clienteRepo, reservaRepo, servicioRepo);
    }

    // === Interfaz principal ===
    private void initComponents() {
        setTitle("Sistema de Taller - Menú Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (título)
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(24, 28, 34));
        JLabel lblTitulo = new JLabel("SISTEMA DE MANEJO DE INVENTARIO Y CRM");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(230, 236, 245));
        panelTitulo.add(lblTitulo);

        // Panel central con botones
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(TemaNeoBlue.SURFACE);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Crear botones
        JButton btnInventario = crearBoton("📦  Inventario", new Color(70, 100, 160));
        JButton btnReserva    = crearBoton("🛠️  Crear Reserva", new Color(70, 140, 180));
        JButton btnEstado     = crearBoton("⚙️  Cambiar Estado", new Color(110, 130, 170));
        JButton btnHistorial  = crearBoton("📜  Historial", new Color(90, 110, 160));
        JButton btnSalir      = crearBoton("🚪  Salir", new Color(190, 70, 70));

        // Eventos
        btnInventario.addActionListener(e -> abrirFormularioInventario());
        btnReserva.addActionListener(e -> abrirFormularioReserva());
        btnEstado.addActionListener(e -> abrirFormularioEstado());
        btnHistorial.addActionListener(e -> abrirFormularioHistorial());
        btnSalir.addActionListener(e -> salirConfirmar());

        // Añadir botones
        gbc.gridx = 0; gbc.gridy = 0;
        panelCentral.add(btnInventario, gbc);
        gbc.gridy++;
        panelCentral.add(btnReserva, gbc);
        gbc.gridy++;
        panelCentral.add(btnEstado, gbc);
        gbc.gridy++;
        panelCentral.add(btnHistorial, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 10, 10);
        panelCentral.add(btnSalir, gbc);

        // Panel inferior con información
        JPanel panelInfo = new JPanel();
        panelInfo.setBackground(TemaNeoBlue.SIDEBAR);
        JLabel lblInfo = new JLabel("Desarrollado por Milton Melara y José Daniel Cuá");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(230, 236, 245));
        panelInfo.add(lblInfo);

        add(panelTitulo, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInfo, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // === Métodos de apertura de formularios ===
    private void abrirFormularioInventario() {
        FormularioInventario form = new FormularioInventario(servicioInventario, parteRepo);
        form.setVisible(true);
    }

    private void abrirFormularioReserva() {
        FormularioReserva form = new FormularioReserva(servicioReserva, clienteRepo, servicioRepo, parteRepo, reservaRepo);
        form.setVisible(true);
    }

    private void abrirFormularioEstado() {
        FormularioEstadoReserva form = new FormularioEstadoReserva(servicioReserva, reservaRepo, clienteRepo, servicioRepo);
        form.setVisible(true);
    }

    private void abrirFormularioHistorial() {
        FormularioHistorial form = new FormularioHistorial(servicioCliente, clienteRepo, servicioReserva, reservaRepo);
        form.setVisible(true);
    }

    private void salirConfirmar() {
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");

        int r = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea salir del sistema?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }

    // === Estilo de botones ===
    private JButton crearBoton(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(300, 45));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(colorFondo);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    // === Getters opcionales ===
    public ServicioCliente obtenerServicioCliente() { return servicioCliente; }
    public ServicioReserva obtenerServicioReserva() { return servicioReserva; }

    // === Main ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
