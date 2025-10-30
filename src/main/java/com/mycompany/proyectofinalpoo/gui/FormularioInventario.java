package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;
import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioInventario;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;


public class FormularioInventario extends JFrame {
    private ServicioInventario servicioInventario;
    private ParteRepo parteRepo;
    private JTextField txtNombre, txtCategoria, txtCantidad, txtPrecio, txtCosto;
    private JTextField txtBuscar, txtStockMinimo;
    private JComboBox<String> cmbFiltroCategoria;
    private JTable tablaPartes;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextArea txtResultado;
    private JButton btnAgregar, btnEditar, btnEliminar, btnActualizar;
    private boolean modoEdicion = false;
    private String idParteEditando = null;
    
    public FormularioInventario(ServicioInventario servicioInventario, ParteRepo parteRepo) {
        this.servicioInventario = servicioInventario;
        this.parteRepo = parteRepo;
        initComponents();
        cargarDatos();
        configurarFiltros();
    }
    
    // Constructor compatible con la versión anterior
    public FormularioInventario(ServicioInventario servicioInventario) {
        this.servicioInventario = servicioInventario;
        this.parteRepo = null; // Se inicializará después si es necesario
        initComponents();
        if (parteRepo != null) {
            cargarDatos();
            configurarFiltros();
        }
    }
    
    private void initComponents() {
        setTitle("Gestión Avanzada de Inventario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Pestaña 1: Agregar/Editar
        JPanel panelFormulario = crearPanelFormulario();
        tabbedPane.addTab("Agregar/Editar Parte", panelFormulario);
        
        // Pestaña 2: Ver Inventario
        JPanel panelInventario = crearPanelInventario();
        tabbedPane.addTab("Ver Inventario", panelInventario);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de resultados en la parte inferior
        txtResultado = new JTextArea(4, 30);
        txtResultado.setEditable(false);
        txtResultado.setBackground(Color.LIGHT_GRAY);
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        add(scrollResultado, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Título
        JLabel lblTitulo = new JLabel("Agregar/Editar Parte del Inventario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Campos del formulario
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panel.add(txtNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        txtCategoria = new JTextField(20);
        panel.add(txtCategoria, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 1;
        txtCantidad = new JTextField(20);
        // Validación en tiempo real para cantidad
        txtCantidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validarCampoNumerico(txtCantidad, "Cantidad");
            }
        });
        panel.add(txtCantidad, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Precio Unitario:"), gbc);
        gbc.gridx = 1;
        txtPrecio = new JTextField(20);
        txtPrecio.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validarCampoDecimal(txtPrecio, "Precio");
            }
        });
        panel.add(txtPrecio, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Costo:"), gbc);
        gbc.gridx = 1;
        txtCosto = new JTextField(20);
        txtCosto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validarCampoDecimal(txtCosto, "Costo");
            }
        });
        panel.add(txtCosto, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnAgregar = new JButton("Agregar Parte");
        btnEditar = new JButton("Actualizar Parte");
        btnEditar.setVisible(false);
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCancelar = new JButton("Cancelar Edición");
        btnCancelar.setVisible(false);
        JButton btnCerrar = new JButton("Cerrar");
        
        // Eventos de botones
        btnAgregar.addActionListener(e -> agregarParte());
        btnEditar.addActionListener(e -> actualizarParte());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnCancelar.addActionListener(e -> cancelarEdicion());
        btnCerrar.addActionListener(e -> dispose());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnCerrar);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);
        
        return panel;
    }
    
    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        panelFiltros.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(15);
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                aplicarFiltros();
            }
        });
        panelFiltros.add(txtBuscar);
        
        panelFiltros.add(new JLabel("Categoría:"));
        cmbFiltroCategoria = new JComboBox<>();
        EstiloCombos.aplicarDarkAzul(cmbFiltroCategoria);
        cmbFiltroCategoria.addItem("Todas");
        cmbFiltroCategoria.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(cmbFiltroCategoria);
        
        panelFiltros.add(new JLabel("Stock mínimo:"));
        txtStockMinimo = new JTextField(5);
        txtStockMinimo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                aplicarFiltros();
            }
        });
        panelFiltros.add(txtStockMinimo);
        
        btnActualizar = new JButton("Actualizar Lista");
        btnActualizar.addActionListener(e -> cargarDatos());
        panelFiltros.add(btnActualizar);
        
        panel.add(panelFiltros, BorderLayout.NORTH);
        
        // Tabla de partes
        String[] columnas = {"ID", "Nombre", "Categoría", "Cantidad", "Precio Unit.", "Costo", "Estado Stock"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 3: return Integer.class;
                    case 4: case 5: return Double.class;
                    default: return String.class;
                }
            }
        };
        
        // Creación de la tabla
tablaPartes = new JTable(modeloTabla);
tablaPartes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
tablaPartes.setAutoCreateRowSorter(true);

// === Tema oscuro para la tabla ===
tablaPartes.setFillsViewportHeight(true);
tablaPartes.setRowHeight(26);
tablaPartes.setShowHorizontalLines(true);
tablaPartes.setShowVerticalLines(false);
tablaPartes.setBackground(TemaNeoBlue.SURFACE);
tablaPartes.setForeground(TemaNeoBlue.TXT);
tablaPartes.setSelectionBackground(new Color(70,100,150));
tablaPartes.setSelectionForeground(Color.WHITE);
tablaPartes.setGridColor(new Color(80, 100, 140));

// Encabezado
JTableHeader header = tablaPartes.getTableHeader();
header.setBackground(TemaNeoBlue.SIDEBAR);
header.setForeground(TemaNeoBlue.TXT);
header.setFont(header.getFont().deriveFont(Font.BOLD));
header.setReorderingAllowed(false);

// Renderer oscuro
DefaultTableCellRenderer darkRenderer = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            c.setBackground(TemaNeoBlue.SURFACE);
            c.setForeground(TemaNeoBlue.TXT);
        }
        if (c instanceof JLabel lbl) {
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }
        return c;
    }
};
// Aplica a todas las columnas
for (int i = 0; i < tablaPartes.getColumnModel().getColumnCount(); i++) {
    tablaPartes.getColumnModel().getColumn(i).setCellRenderer(darkRenderer);
}

// === CREA EL SCROLL AQUÍ y ponle el fondo del viewport ===
JScrollPane scroll = new JScrollPane(tablaPartes);
scroll.getViewport().setBackground(TemaNeoBlue.BG_ALT);

// (y luego agregas 'scroll' al panel donde corresponda)

        
        
        
        // Configurar renderizado de colores para stock bajo
        tablaPartes.setDefaultRenderer(Object.class, new StockCellRenderer());
        
        sorter = new TableRowSorter<>(modeloTabla);
        tablaPartes.setRowSorter(sorter);
        
        
        JScrollPane scrollTabla = new JScrollPane(tablaPartes);
        scrollTabla.setPreferredSize(new Dimension(700, 300));
        panel.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout());
        JButton btnEditarSeleccionada = new JButton("Editar Seleccionada");
        JButton btnEliminarSeleccionada = new JButton("Eliminar Seleccionada");
        JButton btnVerDetalles = new JButton("Ver Detalles");
        
        btnEditarSeleccionada.addActionListener(e -> editarParteSeleccionada());
        btnEliminarSeleccionada.addActionListener(e -> eliminarParteSeleccionada());
        btnVerDetalles.addActionListener(e -> verDetallesParte());
        
        panelAcciones.add(btnEditarSeleccionada);
        panelAcciones.add(btnEliminarSeleccionada);
        panelAcciones.add(btnVerDetalles);
        
        panel.add(panelAcciones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarDatos() {
        if (parteRepo == null) {
            txtResultado.setText("Repositorio no disponible. Usando solo función de agregar.");
            return;
        }
        
        modeloTabla.setRowCount(0);
        List<Parte> partes = parteRepo.findAll();
        
        // Cargar categorías para el filtro
        cmbFiltroCategoria.removeAllItems();
        cmbFiltroCategoria.addItem("Todas");
        
        for (Parte parte : partes) {
            String categoria = parte.getCategoria();
            boolean existe = false;
            for (int i = 0; i < cmbFiltroCategoria.getItemCount(); i++) {
                if (cmbFiltroCategoria.getItemAt(i).equals(categoria)) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                cmbFiltroCategoria.addItem(categoria);
            }
            
            // Agregar fila a la tabla
            String estadoStock = parte.verificarBajoStock(5) ? "STOCK BAJO" : "NORMAL";
            Object[] fila = {
                parte.getId(),
                parte.getNombre(),
                parte.getCategoria(),
                parte.getCantidad(),
                parte.getPrecioUnitario(),
                parte.getCosto(),
                estadoStock
            };
            modeloTabla.addRow(fila);
        }
        
        txtResultado.setText("Inventario cargado: " + partes.size() + " partes");
    }
    
    private void configurarFiltros() {
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }
    
    private void aplicarFiltros() {
        if (sorter == null) return;
        
        String textoBuscar = txtBuscar.getText().toLowerCase();
        String categoriaSeleccionada = (String) cmbFiltroCategoria.getSelectedItem();
        String stockMinimoText = txtStockMinimo.getText();
        
        RowFilter<DefaultTableModel, Object> filtro = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Filtro por texto
                if (!textoBuscar.isEmpty()) {
                    String nombre = entry.getStringValue(1).toLowerCase();
                    if (!nombre.contains(textoBuscar)) {
                        return false;
                    }
                }
                
                // Filtro por categoría
                if (!categoriaSeleccionada.equals("Todas")) {
                    String categoria = entry.getStringValue(2);
                    if (!categoria.equals(categoriaSeleccionada)) {
                        return false;
                    }
                }
                
                // Filtro por stock mínimo
                if (!stockMinimoText.isEmpty()) {
                    try {
                        int stockMinimo = Integer.parseInt(stockMinimoText);
                        Integer cantidad = (Integer) entry.getValue(3);
                        if (cantidad > stockMinimo) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar si no es un número válido
                    }
                }
                
                return true;
            }
        };
        
        sorter.setRowFilter(filtro);
    }
    
    private void agregarParte() {
        try {
            String nombre = txtNombre.getText().trim();
            String categoria = txtCategoria.getText().trim();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            double costo = Double.parseDouble(txtCosto.getText().trim());
            
            Parte nuevaParte = servicioInventario.addParte(nombre, categoria, cantidad, precio, costo);
            
            txtResultado.setText("✓ Parte agregada exitosamente:\n" + 
                               "ID: " + nuevaParte.getId() + "\n" +
                               "Nombre: " + nuevaParte.getNombre() + "\n" +
                               "Categoría: " + nuevaParte.getCategoria());
            
            limpiarCampos();
            if (parteRepo != null) {
                cargarDatos();
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: Verifique que cantidad, precio y costo sean números válidos", 
                "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editarParteSeleccionada() {
        int filaSeleccionada = tablaPartes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una parte de la tabla", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaModelo = tablaPartes.convertRowIndexToModel(filaSeleccionada);
        String id = (String) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);
        String categoria = (String) modeloTabla.getValueAt(filaModelo, 2);
        Integer cantidad = (Integer) modeloTabla.getValueAt(filaModelo, 3);
        Double precio = (Double) modeloTabla.getValueAt(filaModelo, 4);
        Double costo = (Double) modeloTabla.getValueAt(filaModelo, 5);
        
        // Cargar datos en el formulario
        txtNombre.setText(nombre);
        txtCategoria.setText(categoria);
        txtCantidad.setText(cantidad.toString());
        txtPrecio.setText(precio.toString());
        txtCosto.setText(costo.toString());
        
        // Cambiar a modo edición
        modoEdicion = true;
        idParteEditando = id;
        btnAgregar.setVisible(false);
        btnEditar.setVisible(true);
        
        txtResultado.setText("Editando parte: " + nombre);
        
        // Cambiar a la pestaña de formulario
        ((JTabbedPane) getContentPane().getComponent(0)).setSelectedIndex(0);
    }
    
    private void actualizarParte() {
        if (!modoEdicion || idParteEditando == null) return;
        
        try {
            String nombre = txtNombre.getText().trim();
            String categoria = txtCategoria.getText().trim();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            double costo = Double.parseDouble(txtCosto.getText().trim());
            
            Parte parteActualizada = new Parte(idParteEditando, nombre, categoria, cantidad, precio, costo);
            servicioInventario.updateParte(parteActualizada);
            
            txtResultado.setText("✓ Parte actualizada exitosamente:\n" + 
                               "ID: " + parteActualizada.getId() + "\n" +
                               "Nombre: " + parteActualizada.getNombre());
            
            cancelarEdicion();
            cargarDatos();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: Verifique que cantidad, precio y costo sean números válidos", 
                "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarParteSeleccionada() {
        int filaSeleccionada = tablaPartes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una parte de la tabla", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaModelo = tablaPartes.convertRowIndexToModel(filaSeleccionada);
        String id = (String) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar la parte: " + nombre + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                servicioInventario.deleteParte(id);
                txtResultado.setText("✓ Parte eliminada: " + nombre);
                cargarDatos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void verDetallesParte() {
        int filaSeleccionada = tablaPartes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una parte de la tabla", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaModelo = tablaPartes.convertRowIndexToModel(filaSeleccionada);
        String id = (String) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);
        String categoria = (String) modeloTabla.getValueAt(filaModelo, 2);
        Integer cantidad = (Integer) modeloTabla.getValueAt(filaModelo, 3);
        Double precio = (Double) modeloTabla.getValueAt(filaModelo, 4);
        Double costo = (Double) modeloTabla.getValueAt(filaModelo, 5);
        
        String detalles = String.format(
            "DETALLES DE LA PARTE\n" +
            "═══════════════════════════════════\n" +
            "ID: %s\n" +
            "Nombre: %s\n" +
            "Categoría: %s\n" +
            "Cantidad en Stock: %d unidades\n" +
            "Precio Unitario: Q%.2f\n" +
"Costo: Q%.2f\n" +
"Valor Total en Stock: Q%.2f\n" +
"Ganancia por Unidad: Q%.2f\n" +
            "Estado del Stock: %s",
            id, nombre, categoria, cantidad, precio, costo,
            cantidad * costo,
            precio - costo,
            cantidad <= 5 ? "⚠️ STOCK BAJO" : "✅ STOCK NORMAL"
        );
        
        JOptionPane.showMessageDialog(this, detalles, "Detalles de la Parte", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cancelarEdicion() {
        modoEdicion = false;
        idParteEditando = null;
        btnAgregar.setVisible(true);
        btnEditar.setVisible(false);
        limpiarCampos();
        txtResultado.setText("Edición cancelada");
    }
    
    private void limpiarCampos() {
        txtNombre.setText("");
        txtCategoria.setText("");
        txtCantidad.setText("");
        txtPrecio.setText("");
        txtCosto.setText("");
        txtNombre.requestFocus();
    }
    
    private void validarCampoNumerico(JTextField campo, String nombreCampo) {
        try {
            if (!campo.getText().trim().isEmpty()) {
                int valor = Integer.parseInt(campo.getText().trim());
                if (valor < 0) {
                    campo.setBackground(new Color(255, 200, 200));
                } else {
                    campo.setBackground(Color.WHITE);
                }
            } else {
                campo.setBackground(Color.WHITE);
            }
        } catch (NumberFormatException e) {
            campo.setBackground(new Color(255, 200, 200));
        }
    }
    
    private void validarCampoDecimal(JTextField campo, String nombreCampo) {
        try {
            if (!campo.getText().trim().isEmpty()) {
                double valor = Double.parseDouble(campo.getText().trim());
                if (valor < 0) {
                    campo.setBackground(new Color(255, 200, 200));
                } else {
                    campo.setBackground(Color.WHITE);
                }
            } else {
                campo.setBackground(Color.WHITE);
            }
        } catch (NumberFormatException e) {
            campo.setBackground(new Color(255, 200, 200));
        }
    }
    
    // Renderer personalizado para colorear filas con stock bajo
    private class StockCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                // Obtener cantidad de la fila
                int modelRow = table.convertRowIndexToModel(row);
                Integer cantidad = (Integer) table.getModel().getValueAt(modelRow, 3);
                
                if (cantidad != null && cantidad <= 5) {
                    c.setBackground(new Color(255, 230, 230)); // Rojo claro para stock bajo
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            
            return c;
        }
    }
}