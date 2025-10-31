package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioCliente;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.gui.componentes.SelectorFechaPopup;

public class FormularioHistorial extends JFrame {
    private final ServicioCliente servicioCliente;
    private final ClienteRepo clienteRepo;
    private final ServicioReserva servicioReserva;
    private final ReservaRepo reservaRepo;

    // Filtros
    private final JComboBox<String> cbCliente = new JComboBox<>();
    private final JComboBox<String> cbMecanico = new JComboBox<>();
    private final JSpinner spInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner spFin = new JSpinner(new SpinnerDateModel());
    private final JButton btnAplicar = new JButton("Consultar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnCerrar = new JButton("Cerrar");

    // Tabla
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Fecha","Cliente","Mecánico","Servicio","Estado","ID"}, 0
    ){ public boolean isCellEditable(int r,int c){ return false; } };
    private final JTable tabla = new JTable(model);

    // Datos base (FINALIZADA o CANCELADA)
    private List<Reserva> base;

    // ---- ctor ----
    public FormularioHistorial(ServicioCliente servicioCliente,
                               ClienteRepo clienteRepo,
                               ServicioReserva servicioReserva,
                               ReservaRepo reservaRepo) {
        this.servicioCliente = servicioCliente;
        this.clienteRepo = clienteRepo;
        this.servicioReserva = servicioReserva;
        this.reservaRepo = reservaRepo;

        setTitle("Historial");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        // Tema oscuro coherente
        TemaNeoBlue.aplicar();
        TemaNeoBlue.estilizar(getContentPane());
        UIManager.put("TitledBorder.titleColor", new Color(230,236,245));

        // ---------- UI ----------
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        // Edición de fechas
        spInicio.setEditor(new JSpinner.DateEditor(spInicio, "yyyy-MM-dd"));
        spFin.setEditor(new JSpinner.DateEditor(spFin, "yyyy-MM-dd"));
        JComponent compInicio = SelectorFechaPopup.adjuntar(spInicio);
        JComponent compFin = SelectorFechaPopup.adjuntar(spFin);

        // Panel de filtros
        TemaNeoBlue.CardPanel filtros = new TemaNeoBlue.CardPanel();
        filtros.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        EstiloCombos.aplicarDarkAzul(cbCliente);
        EstiloCombos.aplicarDarkAzul(cbMecanico);

        gc.gridx=0; gc.gridy=0; filtros.add(new JLabel("Cliente:"), gc);
        gc.gridx=1; gc.gridy=0; filtros.add(cbCliente, gc);
        gc.gridx=2; gc.gridy=0; filtros.add(new JLabel("Mecánico:"), gc);
        gc.gridx=3; gc.gridy=0; filtros.add(cbMecanico, gc);

        gc.gridx=0; gc.gridy=1; filtros.add(new JLabel("Inicio:"), gc);
        gc.gridx=1; gc.gridy=1; filtros.add(compInicio, gc);
        gc.gridx=2; gc.gridy=1; filtros.add(new JLabel("Fin:"), gc);
        gc.gridx=3; gc.gridy=1; filtros.add(compFin, gc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.add(btnAplicar);
        acciones.add(btnLimpiar);
        acciones.add(btnCerrar);
        gc.gridx=0; gc.gridy=2; gc.gridwidth=4; filtros.add(acciones, gc);

        // Tabla en CardPanel para evitar fondo blanco
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        TemaNeoBlue.CardPanel tablaCard = new TemaNeoBlue.CardPanel();
        tablaCard.setLayout(new BorderLayout());
        tablaCard.add(new JScrollPane(tabla), BorderLayout.CENTER);

        root.add(filtros, BorderLayout.NORTH);
        root.add(tablaCard, BorderLayout.CENTER);

        // ---------- Eventos ----------
        btnAplicar.addActionListener(e -> aplicarFiltros());
        btnLimpiar.addActionListener(e -> limpiarFiltros());
        btnCerrar.addActionListener(e -> dispose());

        // ---------- Datos iniciales ----------
        inicializarDatos();
        limpiarFiltros();
    }

    private void inicializarDatos() {
        // Base: FINALIZADA o CANCELADA
        base = reservaRepo.findAll().stream()
                .filter(r -> r.getEstado() == ReservaEstado.FINALIZADA
                          || r.getEstado() == ReservaEstado.CANCELADA)
                .collect(Collectors.toList());

        // Clientes
        cbCliente.removeAllItems();
        cbCliente.addItem("Todos");
        for (Cliente c : clienteRepo.findAll()) cbCliente.addItem(c.getNombre());

        // Mecánicos detectados en base
        Set<String> mecanicos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Reserva r : base) {
            String m = r.getMecanicoAsignado();
            if (m != null && !m.isBlank()) mecanicos.add(m.trim());
        }
        cbMecanico.removeAllItems();
        cbMecanico.addItem("Todos");
        for (String m : mecanicos) cbMecanico.addItem(m);
    }

    private void aplicarFiltros() {
    // Lee selección actual
    String clienteSel = cbCliente.getSelectedItem() == null ? null : cbCliente.getSelectedItem().toString().trim();
    String mecSel     = cbMecanico.getSelectedItem() == null ? null : cbMecanico.getSelectedItem().toString().trim();

    // Convierte fechas de los spinners (con fallback a hoy si vinieran nulas)
    java.util.Date dIniRaw = (java.util.Date) spInicio.getValue();
    java.util.Date dFinRaw = (java.util.Date) spFin.getValue();
    LocalDate ini = dIniRaw == null ? LocalDate.now() : toLocal(dIniRaw);
    LocalDate fin = dFinRaw == null ? LocalDate.now() : toLocal(dFinRaw);

    // Si el rango está invertido, lo corregimos y reflejamos en los spinners
    if (ini.isAfter(fin)) {
        LocalDate tmp = ini;
        ini = fin;
        fin = tmp;
        spInicio.setValue(toDate(ini));
        spFin.setValue(toDate(fin));
    }

    // Normaliza etiquetas "Todos"
    boolean filtraCliente = clienteSel != null && !clienteSel.isBlank() && !"Todos".equalsIgnoreCase(clienteSel);
    boolean filtraMec     = mecSel     != null && !mecSel.isBlank()     && !"Todos".equalsIgnoreCase(mecSel);

    // Filtrado manual (sin Collectors)
    java.util.List<Reserva> filtradas = new java.util.ArrayList<>();
    for (Reserva r : base) { // 'base' debe ser la lista completa de reservas cargadas
        if (r == null || r.getFecha() == null) continue;

        LocalDate d = r.getFecha().toLocalDate();
        if (d.isBefore(ini) || d.isAfter(fin)) continue;

        if (filtraCliente) {
            String nombre = servicioReserva.obtenerNombreCliente(r.getClienteId());
            if (nombre == null || !nombre.equalsIgnoreCase(clienteSel)) continue;
        }

        if (filtraMec) {
            String m = r.getMecanicoAsignado();
            if (m == null || !m.equalsIgnoreCase(mecSel)) continue;
        }

        filtradas.add(r);
    }

    llenarTabla(filtradas);
}


    private void limpiarFiltros() {
        LocalDate hoy = LocalDate.now();
        spInicio.setValue(toDate(hoy.withDayOfMonth(1)));
        spFin.setValue(toDate(hoy.withDayOfMonth(hoy.lengthOfMonth())));
        cbCliente.setSelectedIndex(0);
        cbMecanico.setSelectedIndex(0);
        llenarTabla(base);
    }

    private void llenarTabla(List<Reserva> datos) {
        model.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Reserva r : datos) {
            String fecha = r.getFecha().format(f);
            String cliente = servicioReserva.obtenerNombreCliente(r.getClienteId());
            String mecanico = r.getMecanicoAsignado();
            String servicio = servicioReserva.obtenerNombreServicio(r.getServicioId());
            String estado = r.getEstado().name();
            model.addRow(new Object[]{fecha, cliente, mecanico, servicio, estado, r.getId()});
        }
    }

    // ---- util fechas ----
    private static LocalDate toLocal(java.util.Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    private static java.util.Date toDate(LocalDate d) {
        return java.util.Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
