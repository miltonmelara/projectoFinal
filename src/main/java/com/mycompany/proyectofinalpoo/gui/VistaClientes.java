package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
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

        TemaNeoBlue.CardPanel form = new TemaNeoBlue.CardPanel();
form.setLayout(new GridLayout(0,2,12,12));
        form.add(new JLabel("Nombre")); form.add(txtNombre);
        form.add(new JLabel("Contacto")); form.add(txtContacto);
        form.add(new JLabel("Marca Auto")); form.add(txtMarca);
        form.add(new JLabel("Modelo Auto")); form.add(txtModelo);
        form.add(new JLabel("Año Auto")); form.add(txtAnio);

        ((AbstractDocument) txtContacto.getDocument()).setDocumentFilter(new FiltroTelefonoGT());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnGuardarCambios = new JButton("Guardar cambios");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnRefrescar = new JButton("Refrescar");
        acciones.add(btnAgregar);
        acciones.add(btnGuardarCambios);
        acciones.add(btnEliminar);
        acciones.add(btnLimpiar);
        acciones.add(btnRefrescar);

        JPanel top = new JPanel(new BorderLayout(8,8));
        JPanel buscar = new JPanel(new BorderLayout(6,6));
        buscar.add(new JLabel("Buscar"), BorderLayout.WEST);
        buscar.add(txtBuscar, BorderLayout.CENTER);
        JButton btnBuscar = new JButton("Filtrar");
        JButton btnQuitarFiltro = new JButton("Quitar filtro");
        JPanel buscarBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buscarBtns.add(btnBuscar);
        buscarBtns.add(btnQuitarFiltro);

        top.add(form, BorderLayout.CENTER);

        TemaNeoBlue.CardPanel barraAcciones = new TemaNeoBlue.CardPanel();
barraAcciones.setLayout(new BorderLayout());
barraAcciones.add(acciones, BorderLayout.WEST);
barraAcciones.add(buscar, BorderLayout.CENTER);
barraAcciones.add(buscarBtns, BorderLayout.EAST);
top.add(barraAcciones, BorderLayout.SOUTH);


        top.setPreferredSize(new Dimension(0, 320));
        add(top, BorderLayout.NORTH);

        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int i = tabla.getSelectedRow();
            if (i >= 0) {
                int m = tabla.convertRowIndexToModel(i);
                idSeleccionado = model.getValueAt(m,0).toString();
                txtNombre.setText(String.valueOf(model.getValueAt(m,1)));
                txtContacto.setText(String.valueOf(model.getValueAt(m,2)));
                txtMarca.setText(String.valueOf(model.getValueAt(m,3)));
                txtModelo.setText(String.valueOf(model.getValueAt(m,4)));
                txtAnio.setText(String.valueOf(model.getValueAt(m,5)));
            }
        });
        TemaNeoBlue.CardPanel cardTabla = new TemaNeoBlue.CardPanel();
cardTabla.setLayout(new BorderLayout());
cardTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);
add(cardTabla, BorderLayout.CENTER);

        cargarTabla(clienteRepo.findAll());

        JButton btnAgregarRef = btnRefrescar;
        btnAgregarRef.addActionListener(e -> cargarTabla(clienteRepo.findAll()));
        btnLimpiar.addActionListener(e -> limpiar());

        btnAgregar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String contacto = txtContacto.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            Integer anio = parseEntero(txtAnio.getText().trim());
            if (nombre.isEmpty() || contacto.isEmpty() || marca.isEmpty() || modelo.isEmpty() || anio == null) {
                JOptionPane.showMessageDialog(this, "Completa todos los campos con valores válidos.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!FiltroTelefonoGT.esValido(contacto)) {
                JOptionPane.showMessageDialog(this, "Teléfono inválido. Usa el formato 1234 5678.", "Contacto", JOptionPane.WARNING_MESSAGE);
                txtContacto.requestFocus();
                return;
            }
            String contactoFmt = FiltroTelefonoGT.normalizado(contacto);
            Cliente c = servicioCliente.crearCliente(new ServicioCliente.NuevoClienteRequest(nombre, contactoFmt, marca, modelo, anio));
            cargarTabla(clienteRepo.findAll());
            seleccionarId(c.getId());
            JOptionPane.showMessageDialog(this, "Cliente agregado.", "OK", JOptionPane.INFORMATION_MESSAGE);
        });

        btnGuardarCambios.addActionListener(e -> {
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
            if (!FiltroTelefonoGT.esValido(contacto)) {
                JOptionPane.showMessageDialog(this, "Teléfono inválido. Usa el formato 1234 5678.", "Contacto", JOptionPane.WARNING_MESSAGE);
                txtContacto.requestFocus();
                return;
            }
            String contactoFmt = FiltroTelefonoGT.normalizado(contacto);
            Cliente c = servicioCliente.actualizarCliente(idSeleccionado, new ServicioCliente.ActualizarClienteRequest(nombre, contactoFmt, marca, modelo, anio));
            cargarTabla(clienteRepo.findAll());
            seleccionarId(c.getId());
            JOptionPane.showMessageDialog(this, "Cambios guardados.", "OK", JOptionPane.INFORMATION_MESSAGE);
        });

        btnEliminar.addActionListener(e -> {
            if (idSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un cliente en la tabla.", "Sin selección", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int r = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            try {
                servicioCliente.eliminarCliente(idSeleccionado);
                cargarTabla(clienteRepo.findAll());
                limpiar();
                JOptionPane.showMessageDialog(this, "Cliente eliminado.", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (RuntimeException ex1) {
                String msg = ex1.getMessage() == null ? "" : ex1.getMessage().toLowerCase();
                if (msg.contains("reservas activas")) {
                    int r2 = JOptionPane.showConfirmDialog(this, "El cliente tiene reservas activas.\n¿Cancelar esas reservas y eliminar de todas formas?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (r2 == JOptionPane.YES_OPTION) {
                        try {
                            servicioCliente.eliminarCliente(idSeleccionado, true);
                            cargarTabla(clienteRepo.findAll());
                            limpiar();
                            JOptionPane.showMessageDialog(this, "Cliente eliminado.", "OK", JOptionPane.INFORMATION_MESSAGE);
                        } catch (RuntimeException ex2) {
                            JOptionPane.showMessageDialog(this, "Error: " + ex2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + ex1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tabla.setRowSorter(sorter);
        btnBuscar.addActionListener(e -> aplicarFiltro(txtBuscar.getText().trim()));
        btnQuitarFiltro.addActionListener(e -> { txtBuscar.setText(""); aplicarFiltro(""); });
    }

    public void aplicarFiltro(String q) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) tabla.getRowSorter();
        if (q == null || q.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            String s = q.toLowerCase();
            sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel,Integer>() {
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
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
        for (Cliente c : data) {
            String tel = FiltroTelefonoGT.normalizado(c.getContacto());
            model.addRow(new Object[]{c.getId(), c.getNombre(), tel, c.getMarcaAuto(), c.getModeloAuto(), c.getAnioAuto()});
        }
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
