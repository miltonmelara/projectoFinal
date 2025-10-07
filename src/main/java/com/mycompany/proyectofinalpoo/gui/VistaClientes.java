package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioCliente;

public class VistaClientes extends JPanel {
    private final ServicioCliente servicioCliente;
    private final ClienteRepo clienteRepo;
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtContacto = new JTextField();
    private final JTextField txtMarca = new JTextField();
    private final JTextField txtModelo = new JTextField();
    private final JTextField txtAnio = new JTextField();
    private final JTextField txtBuscar = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Nombre","Contacto","Marca","Modelo","Año"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tabla = new JTable(model);
    private String idSeleccionado = null;

    public VistaClientes(ServicioCliente servicioCliente, ClienteRepo clienteRepo) {
        this.servicioCliente = servicioCliente;
        this.clienteRepo = clienteRepo;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        form.add(new JLabel("Nombre")); form.add(txtNombre);
        form.add(new JLabel("Contacto")); form.add(txtContacto);
        form.add(new JLabel("Marca Auto")); form.add(txtMarca);
        form.add(new JLabel("Modelo Auto")); form.add(txtModelo);
        form.add(new JLabel("Año Auto")); form.add(txtAnio);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");
        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnRefrescar);

        JPanel top = new JPanel(new BorderLayout(8,8));
        JPanel buscar = new JPanel(new BorderLayout(6,6));
        buscar.add(new JLabel("Buscar"), BorderLayout.WEST);
        buscar.add(txtBuscar, BorderLayout.CENTER);
        JButton btnBuscar = new JButton("Filtrar");
        JButton btnLimpiar = new JButton("Limpiar");
        JPanel buscarBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buscarBtns.add(btnBuscar);
        buscarBtns.add(btnLimpiar);
        top.add(form, BorderLayout.CENTER);
        JPanel southTop = new JPanel(new BorderLayout());
        southTop.add(acciones, BorderLayout.WEST);
        southTop.add(buscar, BorderLayout.CENTER);
        southTop.add(buscarBtns, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int i = tabla.getSelectedRow();
            if (i >= 0) {
                int m = tabla.convertRowIndexToModel(i);
                idSeleccionado = model.getValueAt(m,0).toString();
                txtNombre.setText(model.getValueAt(m,1).toString());
                txtContacto.setText(model.getValueAt(m,2).toString());
                txtMarca.setText(model.getValueAt(m,3).toString());
                txtModelo.setText(model.getValueAt(m,4).toString());
                txtAnio.setText(model.getValueAt(m,5).toString());
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarTabla(clienteRepo.findAll());

        btnRefrescar.addActionListener(e -> cargarTabla(clienteRepo.findAll()));
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String contacto = txtContacto.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            Integer anio = parseEntero(txtAnio.getText().trim());
            if (nombre.isEmpty() || contacto.isEmpty() || marca.isEmpty() || modelo.isEmpty() || anio == null) {
                JOptionPane.showMessageDialog(this, "Completa todos los campos con valores válidos.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Cliente c = servicioCliente.crearCliente(new ServicioCliente.NuevoClienteRequest(nombre, contacto, marca, modelo, anio));
            cargarTabla(clienteRepo.findAll());
            seleccionarId(c.getId());
            JOptionPane.showMessageDialog(this, "Cliente creado.", "OK", JOptionPane.INFORMATION_MESSAGE);
        });
        btnActualizar.addActionListener(e -> {
            if (idSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente en la tabla.", "Sin selección", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String nombre = txtNombre.getText().trim();
            String contacto = txtContacto.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            Integer anio = parseEntero(txtAnio.getText().trim());
            if (nombre.isEmpty() || contacto.isEmpty() || marca.isEmpty() || modelo.isEmpty() || anio == null) {
                JOptionPane.showMessageDialog(this, "Completa todos los campos con valores válidos.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Cliente c = servicioCliente.actualizarCliente(idSeleccionado, new ServicioCliente.ActualizarClienteRequest(nombre, contacto, marca, modelo, anio));
            cargarTabla(clienteRepo.findAll());
            seleccionarId(c.getId());
            JOptionPane.showMessageDialog(this, "Cliente actualizado.", "OK", JOptionPane.INFORMATION_MESSAGE);
        });
        btnEliminar.addActionListener(e -> {
            if (idSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente en la tabla.", "Sin selección", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int r = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            servicioCliente.eliminarCliente(idSeleccionado);
            cargarTabla(clienteRepo.findAll());
            limpiar();
            JOptionPane.showMessageDialog(this, "Cliente eliminado.", "OK", JOptionPane.INFORMATION_MESSAGE);
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tabla.setRowSorter(sorter);
        btnBuscar.addActionListener(e -> aplicarFiltro(txtBuscar.getText().trim()));
        btnLimpiar.addActionListener(e -> { txtBuscar.setText(""); aplicarFiltro(""); });
    }

    public void aplicarFiltro(String q) {
    javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter =
            (javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel>) tabla.getRowSorter();
    if (sorter == null) {
        sorter = new javax.swing.table.TableRowSorter<>((javax.swing.table.DefaultTableModel) tabla.getModel());
        tabla.setRowSorter(sorter);
    }
    if (q == null || q.isBlank()) {
        sorter.setRowFilter(null);
    } else {
        String s = q.toLowerCase();
        sorter.setRowFilter(new javax.swing.RowFilter<javax.swing.table.DefaultTableModel,Integer>() {
            public boolean include(Entry<? extends javax.swing.table.DefaultTableModel, ? extends Integer> entry) {
                for (int i=0;i<entry.getModel().getColumnCount();i++) {
                    Object v = entry.getValue(i);
                    if (v!=null && v.toString().toLowerCase().contains(s)) return true;
                }
                return false;
            }
        });
    }
}


    private void cargarTabla(List<Cliente> data) {
        model.setRowCount(0);
        for (Cliente c : data) model.addRow(new Object[]{c.getId(), c.getNombre(), c.getContacto(), c.getMarcaAuto(), c.getModeloAuto(), c.getAnioAuto()});
    }

    private Integer parseEntero(String s) {
        try {
            int v = Integer.parseInt(s);
            if (v < 1900 || v > LocalDate.now().getYear()+1) return null;
            return v;
        } catch(Exception e){ return null; }
    }

    private void limpiar() {
        idSeleccionado = null;
        txtNombre.setText("");
        txtContacto.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAnio.setText("");
        tabla.clearSelection();
    }

    private void seleccionarId(String id) {
        for (int i=0;i<model.getRowCount();i++) if (model.getValueAt(i,0).equals(id)) {
            int v = tabla.convertRowIndexToView(i);
            tabla.getSelectionModel().setSelectionInterval(v,v);
            tabla.scrollRectToVisible(tabla.getCellRect(v,0,true));
            break;
        }
    }
}
