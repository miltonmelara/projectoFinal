package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.SecurityContext;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios;

public class InicioSesion extends JFrame {
    private final UsuarioRepo repositorioUsuarios;

    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(20);
    private final JButton botonEntrar = new JButton("Entrar");
    private final JButton botonSalir = new JButton("Salir");

    private JLabel lblTitulo, lblUsuario, lblContrasena;
    private JPanel panelRaiz, panelFormulario, panelBotones;

    public InicioSesion(Path carpetaDatos) {
        // Tema antes de construir UI
        TemaNeoBlue.aplicar();

        this.repositorioUsuarios = new UsuarioFileRepo(carpetaDatos);
        crearUsuariosSiFaltan();
        configurarVentana();
        configurarEventos();

        TemaNeoBlue.estilizar(this.getContentPane());
        setLocationRelativeTo(null);
    }

    private void configurarVentana() {
        setTitle("Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 260);
        setResizable(false);

        panelRaiz = new JPanel(new BorderLayout(12, 12));
        panelRaiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panelRaiz.setOpaque(true);
        panelRaiz.setBackground(TemaNeoBlue.BG);

        lblTitulo = new JLabel("Iniciar sesión", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(TemaNeoBlue.TXT);
        panelRaiz.add(lblTitulo, BorderLayout.NORTH);

        panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setOpaque(true);
        panelFormulario.setBackground(TemaNeoBlue.BG_ALT);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Columna 0: labels
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        lblUsuario = new JLabel("Usuario:");
        lblUsuario.setForeground(TemaNeoBlue.TXT);
        gbc.gridy = 0;
        panelFormulario.add(lblUsuario, gbc);

        lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setForeground(TemaNeoBlue.TXT);
        gbc.gridy = 1;
        panelFormulario.add(lblContrasena, gbc);

        // Columna 1: campos
        Dimension campoSize = new Dimension(220, 36);

        estilizarCampoTexto(campoUsuario);
        campoUsuario.setPreferredSize(campoSize);
        campoUsuario.setMinimumSize(campoSize);
        campoUsuario.setColumns(20);

        estilizarCampoPassword(campoContrasena);
        campoContrasena.setPreferredSize(campoSize);
        campoContrasena.setMinimumSize(campoSize);
        campoContrasena.setColumns(20);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridy = 0;
        panelFormulario.add(campoUsuario, gbc);

        gbc.gridy = 1;
        panelFormulario.add(campoContrasena, gbc);

        // Botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelBotones.setOpaque(true);
        panelBotones.setBackground(TemaNeoBlue.BG_ALT);

        estilizarBotonSecundario(botonSalir);
        estilizarBotonPrimario(botonEntrar);
        panelBotones.add(botonSalir);
        panelBotones.add(botonEntrar);

        panelRaiz.add(panelFormulario, BorderLayout.CENTER);
        panelRaiz.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(panelRaiz);
        getRootPane().setDefaultButton(botonEntrar);
    }

    private volatile boolean procesandoLogin = false;

    private void configurarEventos() {
        botonEntrar.addActionListener(e -> intentarInicioSesion());
        botonSalir.addActionListener(e -> System.exit(0));
    }

    private void intentarInicioSesion() {
    if (procesandoLogin) return;
    procesandoLogin = true;
    String usuario = campoUsuario.getText().trim();
    String contrasena = new String(campoContrasena.getPassword());
    if (usuario.isEmpty() || contrasena.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Usuario y contraseña requeridos", "Atención", JOptionPane.WARNING_MESSAGE);
        procesandoLogin = false;
        return;
    }

    com.mycompany.proyectofinalpoo.Usuario encontrado =
            repositorioUsuarios.findByUsername(usuario).orElse(null);

    if (encontrado == null || !contrasena.equals(encontrado.getPassword())) {
        JOptionPane.showMessageDialog(this, "Credenciales inválidas", "Error", JOptionPane.ERROR_MESSAGE);
        procesandoLogin = false;
        return;
    }

    // Guarda el usuario logueado
    com.mycompany.proyectofinalpoo.repo.servicios.SecurityContext.setCurrentUser(encontrado);

    // Intenta abrir el Dashboard y reporta cualquier fallo
    SwingUtilities.invokeLater(() -> {
        try {
            Dashboard panel = new Dashboard();
            panel.setVisible(true);
            dispose(); // cierra el login SOLO si el dashboard se abrió bien
            procesandoLogin = false;
        } catch (Throwable ex) {
            ex.printStackTrace(); // para ver el stacktrace en la consola
            JOptionPane.showMessageDialog(
                this,
                "No se pudo abrir el panel: " + ex.getClass().getSimpleName() + "\n" + String.valueOf(ex.getMessage()),
                "Error al abrir Dashboard",
                JOptionPane.ERROR_MESSAGE
            );
            procesandoLogin = false;
        }
    });
}


    /* ===================== Estilos ===================== */

    private void estilizarCampoTexto(JTextField tf) {
        tf.setOpaque(true);
        tf.setBackground(TemaNeoBlue.SURFACE);
        tf.setForeground(TemaNeoBlue.TXT);
        tf.setCaretColor(TemaNeoBlue.TXT);
        tf.setSelectionColor(new Color(58, 105, 198));
        tf.setSelectedTextColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(120,150,220,120), 1, true),
                new javax.swing.border.EmptyBorder(8,10,8,10)
        ));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void estilizarCampoPassword(JPasswordField pf) {
        pf.setOpaque(true);
        pf.setBackground(TemaNeoBlue.SURFACE);
        pf.setForeground(TemaNeoBlue.TXT);
        pf.setCaretColor(TemaNeoBlue.TXT);
        pf.setSelectionColor(new Color(58, 105, 198));
        pf.setSelectedTextColor(Color.WHITE);
        pf.setEchoChar('•');
        pf.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(120,150,220,120), 1, true),
                new javax.swing.border.EmptyBorder(8,10,8,10)
        ));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void estilizarBotonPrimario(JButton b) {
        b.setBackground(TemaNeoBlue.ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void estilizarBotonSecundario(JButton b) {
        b.setBackground(TemaNeoBlue.SURFACE);
        b.setForeground(TemaNeoBlue.TXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    /* ===================== Semilla ===================== */

    private void crearUsuariosSiFaltan() {
    ServicioUsuarios servicio = new ServicioUsuarios(repositorioUsuarios);
    java.util.List<Usuario> existentes = repositorioUsuarios.findAll();
    java.util.Set<String> nombres = new java.util.HashSet<>();
    for (Usuario u : existentes) {
        if (u.getUsername() != null) nombres.add(u.getUsername().toLowerCase());
    }

    if (!nombres.contains("admin")) servicio.seedAdminDefault("admin", "admin123");
    if (!nombres.contains("carlos rodriguez")) servicio.seedUsuario("Carlos Rodriguez", "1234", RolUsuario.MECANICO);
    if (!nombres.contains("ana perez")) servicio.seedUsuario("Ana Perez", "1234", RolUsuario.MECANICO);
    if (!nombres.contains("maria gonzales")) servicio.seedUsuario("Maria Gonzales", "1234", RolUsuario.MECANICO);
    if (!nombres.contains("luis martinez")) servicio.seedUsuario("Luis Martinez", "1234", RolUsuario.MECANICO);
}

}
