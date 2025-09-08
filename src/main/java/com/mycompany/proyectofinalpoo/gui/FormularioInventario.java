package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioInventario;

public class FormularioInventario extends JFrame {
    private ServicioInventario servicioInventario;
    private JTextField txtNombre, txtCategoria, txtCantidad, txtPrecio, txtCosto;
    private JTextArea txtResultado;
    
    public FormularioInventario(ServicioInventario servicioInventario) {
        this.servicioInventario = servicioInventario;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Agregar Ítem al Inventario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Título
        JLabel lblTitulo = new JLabel("Agregar Nueva Parte al Inventario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelPrincipal.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Campos del formulario
        gbc.gridx = 0; gbc.gridy = 1;
        panelPrincipal.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panelPrincipal.add(txtNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        txtCategoria = new JTextField(20);
        panelPrincipal.add(txtCategoria, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panelPrincipal.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 1;
        txtCantidad = new JTextField(20);
        panelPrincipal.add(txtCantidad, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panelPrincipal.add(new JLabel("Precio Unitario:"), gbc);
        gbc.gridx = 1;
        txtPrecio = new JTextField(20);
        panelPrincipal.add(txtPrecio, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panelPrincipal.add(new JLabel("Costo:"), gbc);
        gbc.gridx = 1;
        txtCosto = new JTextField(20);
        panelPrincipal.add(txtCosto, gbc);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnAgregar = new JButton("Agregar Parte");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCerrar = new JButton("Cerrar");
        
        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarParte();
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
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrar);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
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
    
    private void agregarParte() {
        try {
            String nombre = txtNombre.getText().trim();
            String categoria = txtCategoria.getText().trim();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            double costo = Double.parseDouble(txtCosto.getText().trim());
            
            Parte nuevaParte = servicioInventario.addParte(nombre, categoria, cantidad, precio, costo);
            
            txtResultado.setText("✓ Parte agregada exitosamente:\n" + nuevaParte.toString());
            limpiarCampos();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: Verifique que cantidad, precio y costo sean números válidos", 
                                        "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limpiarCampos() {
        txtNombre.setText("");
        txtCategoria.setText("");
        txtCantidad.setText("");
        txtPrecio.setText("");
        txtCosto.setText("");
        txtNombre.requestFocus();
    }
}