package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.UsuarioRepo;
import com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.SecurityContext;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios;

public class InicioSesion extends JFrame {
    private final UsuarioRepo repositorioUsuarios;
    private final JTextField campoUsuario = new JTextField(16);
    private final JPasswordField campoContrasena = new JPasswordField(16);
    private final JButton botonEntrar = new JButton("Entrar");
    private final JButton botonSalir = new JButton("Salir");

    public InicioSesion(Path carpetaDatos) {
        this.repositorioUsuarios = new UsuarioFileRepo(carpetaDatos);
        crearUsuariosSiFaltan();
        configurarVentana();
        configurarEventos();
    }

    private void configurarVentana() {
        setTitle("Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 220);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Iniciar sesión");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formulario.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        formulario.add(campoUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formulario.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        formulario.add(campoContrasena, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(botonSalir);
        botones.add(botonEntrar);

        panel.add(formulario, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        setContentPane(panel);
        getRootPane().setDefaultButton(botonEntrar);
    }

    private void configurarEventos() {
        botonEntrar.addActionListener(e -> intentarInicioSesion());
        botonSalir.addActionListener(e -> System.exit(0));
        campoContrasena.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarInicioSesion();
            }
        });
    }

    private void intentarInicioSesion() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario y contraseña requeridos", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario encontrado = repositorioUsuarios.findByUsername(usuario).orElse(null);
        if (encontrado == null || !contrasena.equals(encontrado.getPassword())) {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SecurityContext.setCurrentUser(encontrado);
        SwingUtilities.invokeLater(() -> {
            Dashboard panel = new Dashboard();
            panel.setVisible(true);
        });
        dispose();
    }

    private void crearUsuariosSiFaltan() {
    repositorioUsuarios.delete("mecanico1");
    com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios servicio = new com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios(repositorioUsuarios);
    java.util.List<com.mycompany.proyectofinalpoo.Usuario> existentes = repositorioUsuarios.findAll();
    java.util.Set<String> nombres = new java.util.HashSet<>();
    for (com.mycompany.proyectofinalpoo.Usuario u : existentes) {
        if (u.getUsername() != null) nombres.add(u.getUsername().toLowerCase());
    }
    if (!nombres.contains("admin")) servicio.seedAdminDefault("admin", "admin123");
    if (!nombres.contains("carlos rodriguez")) servicio.seedUsuario("Carlos Rodriguez", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
    if (!nombres.contains("ana perez")) servicio.seedUsuario("Ana Perez", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
    if (!nombres.contains("maria gonzales")) servicio.seedUsuario("Maria Gonzales", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
    if (!nombres.contains("luis martinez")) servicio.seedUsuario("Luis Martinez", "1234", com.mycompany.proyectofinalpoo.RolUsuario.MECANICO);
}



}
