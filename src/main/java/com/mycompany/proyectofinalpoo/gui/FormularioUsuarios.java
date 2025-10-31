package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioUsuarios;
import com.mycompany.proyectofinalpoo.gui.componentes.GestorEventosSistema;

public class FormularioUsuarios extends JPanel {
    private final ServicioUsuarios servicioUsuarios;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Usuario", "Contraseña"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JLabel lblEstado = new JLabel(" ");

    public FormularioUsuarios(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        construirUI();
        cargarDatos();
    }

    private void construirUI() {
        TemaNeoBlue.aplicar();
        TemaNeoBlue.estilizar(this);
        txtPassword.setEchoChar((char) 0);

        tabla.setRowHeight(24);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) copiarSeleccion();
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Mecánicos registrados"));
        add(scroll, BorderLayout.CENTER);

        JPanel panelEdicion = new JPanel();
        panelEdicion.setOpaque(false);
        panelEdicion.setBorder(BorderFactory.createTitledBorder("Gestión de cuentas"));
        panelEdicion.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panelEdicion.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panelEdicion.add(txtUsuario, gbc);
        gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 1;
        panelEdicion.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panelEdicion.add(txtPassword, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotones.setOpaque(false);
        JButton btnCrear = new JButton("Crear mecánico");
        JButton btnActualizar = new JButton("Actualizar contraseña");
        JButton btnEliminar = new JButton("Eliminar mecánico");
        JButton btnLimpiar = new JButton("Limpiar");
        btnCrear.addActionListener(e -> crearMecanico());
        btnActualizar.addActionListener(e -> actualizarPassword());
        btnEliminar.addActionListener(e -> eliminarMecanico());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        panelBotones.add(btnCrear);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panelEdicion.add(panelBotones, gbc);

        lblEstado.setForeground(Color.WHITE);
        gbc.gridy = 3;
        panelEdicion.add(lblEstado, gbc);

        add(panelEdicion, BorderLayout.EAST);
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        List<Usuario> mecanicos = servicioUsuarios.listarMecanicos();
        for (Usuario u : mecanicos) {
            modeloTabla.addRow(new Object[]{u.getUsername(), u.getPassword()});
        }
    }

    private void copiarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        String usuario = String.valueOf(modeloTabla.getValueAt(fila, 0));
        String password = String.valueOf(modeloTabla.getValueAt(fila, 1));
        txtUsuario.setText(usuario);
        txtPassword.setText(password);
    }

    private void crearMecanico() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarEstado("Debe llenar usuario y contraseña", Color.ORANGE);
            return;
        }
        if (!passwordValida(password)) {
            mostrarEstado("Contraseña insegura: mínimo 6 caracteres, un número y un símbolo", Color.ORANGE);
            return;
        }
        try {
            servicioUsuarios.crearMecanico(usuario, password);
            mostrarEstado("Mecánico creado correctamente", new Color(60,170,90));
            cargarDatos();
            seleccionarUsuario(usuario);
            GestorEventosSistema.notificarCambioMecanicos();
        } catch (Exception ex) {
            mostrarEstado("Error: " + ex.getMessage(), Color.PINK);
        }
    }

    private void eliminarMecanico() {
        String usuario = txtUsuario.getText().trim();
        if (usuario.isEmpty()) {
            mostrarEstado("Selecciona el mecánico que deseas eliminar", Color.ORANGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la cuenta del mecánico '" + usuario + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            boolean eliminado = servicioUsuarios.eliminarMecanico(usuario);
            if (eliminado) {
                mostrarEstado("Cuenta eliminada", new Color(220, 76, 70));
                cargarDatos();
                limpiarCampos();
                GestorEventosSistema.notificarCambioMecanicos();
            } else {
                mostrarEstado("No se encontró el usuario", Color.ORANGE);
            }
        } catch (Exception ex) {
            mostrarEstado("Error: " + ex.getMessage(), Color.PINK);
        }
    }

    private void actualizarPassword() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarEstado("Seleccione un usuario y escriba una contraseña", Color.ORANGE);
            return;
        }
        if (!passwordValida(password)) {
            mostrarEstado("Contraseña insegura: mínimo 6 caracteres, un número y un símbolo", Color.ORANGE);
            return;
        }
        try {
            servicioUsuarios.actualizarPassword(usuario, password);
            mostrarEstado("Contraseña actualizada", new Color(60,170,90));
            cargarDatos();
            seleccionarUsuario(usuario);
            GestorEventosSistema.notificarCambioMecanicos();
        } catch (Exception ex) {
            mostrarEstado("Error: " + ex.getMessage(), Color.PINK);
        }
    }

    private boolean passwordValida(String password) {
        if (password == null || password.length() < 6) return false;
        boolean tieneNumero = false;
        boolean tieneSimbolo = false;
        String simbolos = "@!#$%^&*()_-+=[]{}|;:,.<>?/";
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) tieneNumero = true;
            if (simbolos.indexOf(c) >= 0) tieneSimbolo = true;
        }
        return tieneNumero && tieneSimbolo;
    }

    private void seleccionarUsuario(String usuario) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (String.valueOf(modeloTabla.getValueAt(i,0)).equalsIgnoreCase(usuario)) {
                int viewRow = tabla.convertRowIndexToView(i);
                tabla.setRowSelectionInterval(viewRow, viewRow);
                tabla.scrollRectToVisible(tabla.getCellRect(viewRow, 0, true));
                break;
            }
        }
    }

    private void limpiarCampos() {
        txtUsuario.setText("");
        txtPassword.setText("");
        tabla.clearSelection();
        mostrarEstado(" ", Color.WHITE);
    }

    private void mostrarEstado(String mensaje, Color color) {
        lblEstado.setForeground(color);
        lblEstado.setText(mensaje);
    }
}
