package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.mycompany.proyectofinalpoo.Cliente;
import com.mycompany.proyectofinalpoo.Parte;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.Servicio;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import javax.swing.table.TableRowSorter; 

public class FormularioReserva extends JFrame {
    private ServicioReserva servicioReserva;
    private ClienteRepo clienteRepo;
    private ServicioRepo servicioRepo;
    private ParteRepo parteRepo;
    private ReservaRepo reservaRepo;
    
    private JComboBox<ClienteItem> cmbClientes;
    private JComboBox<ServicioItem> cmbServicios;
    private JTextField txtMecanico;
    private JSpinner spnFecha, spnHora, spnMinutos;
    private JTextArea txtResultado, txtVistaPrevia, txtPartesRequeridas, txtDetalleReserva;
    private JLabel lblCostoTotal, lblDuracionTotal, lblDisponibilidad;
    private JTable tablaMecanicos, tablaReservasExistentes;
    private DefaultTableModel modeloMecanicos, modeloReservasExistentes;
    private JCheckBox chkValidarHorario, chkValidarFinSemana;
    private JComboBox<String> cmbFiltroMecanico, cmbFiltroEstado;
    private JTextField txtFiltroBuscar;
    private JSpinner spnFiltroFecha;
    private TableRowSorter<DefaultTableModel> sorterReservas;
    
    public FormularioReserva(ServicioReserva servicioReserva, ClienteRepo clienteRepo, 
                           ServicioRepo servicioRepo, ParteRepo parteRepo, ReservaRepo reservaRepo) {
        this.servicioReserva = servicioReserva;
        this.clienteRepo = clienteRepo;
        this.servicioRepo = servicioRepo;
        this.parteRepo = parteRepo;
        this.reservaRepo = reservaRepo;
        initComponents();
        cargarDatos();
        configurarValidaciones();
    }
    
    // Constructor compatible con versión anterior
    public FormularioReserva(ServicioReserva servicioReserva, ClienteRepo clienteRepo, ServicioRepo servicioRepo) {
        this.servicioReserva = servicioReserva;
        this.clienteRepo = clienteRepo;
        this.servicioRepo = servicioRepo;
        this.parteRepo = null;
        this.reservaRepo = null;
        initComponents();
        cargarDatos();
        configurarValidaciones();
    }
    
    private void initComponents() {
        setTitle("Crear Reserva - Sistema Avanzado");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Pestaña 1: Crear Reserva
        JPanel panelReserva = crearPanelReserva();
        tabbedPane.addTab("Crear Reserva", panelReserva);
        
        // Pestaña 2: Vista Previa y Validaciones
        JPanel panelPrevia = crearPanelVistaPrevia();
        tabbedPane.addTab("Vista Previa", panelPrevia);
        
        // Pestaña 3: Disponibilidad de Mecánicos
        // Pestaña 3: Ver Reservas Existentes  
JPanel panelReservasExistentes = crearPanelReservasExistentes();
tabbedPane.addTab("Reservas Existentes", panelReservasExistentes);

// Pestaña 4: Disponibilidad de Mecánicos
JPanel panelMecanicos = crearPanelMecanicos();
tabbedPane.addTab("Disponibilidad", panelMecanicos);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de resultados
        txtResultado = new JTextArea(4, 30);
        txtResultado.setEditable(false);
        txtResultado.setBackground(Color.LIGHT_GRAY);
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Resultado"));
        add(scrollResultado, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel crearPanelReserva() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Título
        JLabel lblTitulo = new JLabel("Crear Nueva Reserva");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panel.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Cliente
        // Cliente
gbc.gridx = 0; gbc.gridy = 1;
panel.add(new JLabel("Cliente:"), gbc);
gbc.gridx = 1; gbc.gridwidth = 2;

// Panel para ComboBox + botón
JPanel panelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
cmbClientes = new JComboBox<>();
cmbClientes.setPreferredSize(new Dimension(270, 25));
cmbClientes.addActionListener(e -> actualizarVistaPrevia());

JButton btnAgregarCliente = new JButton("+");
btnAgregarCliente.setPreferredSize(new Dimension(30, 25));
btnAgregarCliente.setFont(new Font("Arial", Font.BOLD, 12));
btnAgregarCliente.setToolTipText("Agregar nuevo cliente");
btnAgregarCliente.addActionListener(e -> agregarClienteRapido());

panelCliente.add(cmbClientes);
panelCliente.add(btnAgregarCliente);
panel.add(panelCliente, gbc);
        
        // Servicio
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Servicio:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbServicios = new JComboBox<>();
        cmbServicios.setPreferredSize(new Dimension(300, 25));
        cmbServicios.addActionListener(e -> actualizarVistaPrevia());
        panel.add(cmbServicios, gbc);
        
        // Mecánico
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Mecánico:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtMecanico = new JTextField(20);
        txtMecanico.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                actualizarVistaPrevia();
            }
        });
        panel.add(txtMecanico, gbc);
        
        // Fecha y hora
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        spnFecha = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy");
        spnFecha.setEditor(dateEditor);
        spnFecha.addChangeListener(e -> actualizarVistaPrevia());
        panel.add(spnFecha, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Hora:"), gbc);
        gbc.gridx = 1;
        
        JPanel panelHora = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spnHora = new JSpinner(new SpinnerNumberModel(8, 6, 18, 1));
        spnMinutos = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
        spnHora.addChangeListener(e -> actualizarVistaPrevia());
        spnMinutos.addChangeListener(e -> actualizarVistaPrevia());
        
        panelHora.add(spnHora);
        panelHora.add(new JLabel(":"));
        panelHora.add(spnMinutos);
        panelHora.add(new JLabel(" hrs"));
        panel.add(panelHora, gbc);
        
        // Opciones de validación
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        JPanel panelOpciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelOpciones.setBorder(BorderFactory.createTitledBorder("Validaciones"));
        
        chkValidarHorario = new JCheckBox("Validar horario laboral (6:00-18:00)", true);
        chkValidarFinSemana = new JCheckBox("Restringir fines de semana", true);
        chkValidarHorario.addActionListener(e -> actualizarVistaPrevia());
        chkValidarFinSemana.addActionListener(e -> actualizarVistaPrevia());
        
        panelOpciones.add(chkValidarHorario);
        panelOpciones.add(chkValidarFinSemana);
        panel.add(panelOpciones, gbc);
        
        // Panel de información en tiempo real
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        JPanel panelInfo = new JPanel(new GridLayout(3, 1));
        panelInfo.setBorder(BorderFactory.createTitledBorder("Información"));
        
        lblCostoTotal = new JLabel("Costo Total: Q0.00");
        lblCostoTotal.setFont(new Font("Arial", Font.BOLD, 12));
        lblDuracionTotal = new JLabel("Duración: 0 minutos");
        lblDisponibilidad = new JLabel("Estado: Seleccione servicio");
        
        panelInfo.add(lblCostoTotal);
        panelInfo.add(lblDuracionTotal);
        panelInfo.add(lblDisponibilidad);
        panel.add(panelInfo, gbc);
        
        // Botones
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 3;
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCrear = new JButton("Crear Reserva");
        JButton btnValidar = new JButton("Validar Disponibilidad");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCerrar = new JButton("Cerrar");
        
        btnCrear.addActionListener(e -> crearReserva());
        btnValidar.addActionListener(e -> validarDisponibilidad());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnCerrar.addActionListener(e -> dispose());
        
        panelBotones.add(btnCrear);
        panelBotones.add(btnValidar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrar);
        panel.add(panelBotones, gbc);
        
        return panel;
    }
    
    private JPanel crearPanelVistaPrevia() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Vista previa del servicio
        txtVistaPrevia = new JTextArea(15, 40);
        txtVistaPrevia.setEditable(false);
        txtVistaPrevia.setFont(new Font("Courier New", Font.PLAIN, 12));
        JScrollPane scrollPrevia = new JScrollPane(txtVistaPrevia);
        scrollPrevia.setBorder(BorderFactory.createTitledBorder("Vista Previa del Servicio"));
        panel.add(scrollPrevia, BorderLayout.CENTER);
        
        // Partes requeridas
        txtPartesRequeridas = new JTextArea(8, 40);
        txtPartesRequeridas.setEditable(false);
        txtPartesRequeridas.setFont(new Font("Courier New", Font.PLAIN, 11));
        JScrollPane scrollPartes = new JScrollPane(txtPartesRequeridas);
        scrollPartes.setBorder(BorderFactory.createTitledBorder("Partes Requeridas y Disponibilidad"));
        panel.add(scrollPartes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel crearPanelMecanicos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Información
        JLabel lblInfo = new JLabel("<html><b>Disponibilidad de Mecánicos</b><br>" +
                                  "Basado en reservas existentes para la fecha seleccionada</html>");
        panel.add(lblInfo, BorderLayout.NORTH);
        
        // Tabla de mecánicos
        String[] columnas = {"Mecánico", "Reservas del Día", "Última Reserva", "Estado", "Recomendación"};
        modeloMecanicos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaMecanicos = new JTable(modeloMecanicos);
        tablaMecanicos.setRowHeight(25);
        JScrollPane scrollMecanicos = new JScrollPane(tablaMecanicos);
        scrollMecanicos.setPreferredSize(new Dimension(600, 200));
        panel.add(scrollMecanicos, BorderLayout.CENTER);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnActualizar = new JButton("Actualizar Disponibilidad");
        JButton btnSeleccionar = new JButton("Seleccionar Mecánico");
        
        btnActualizar.addActionListener(e -> actualizarDisponibilidadMecanicos());
        btnSeleccionar.addActionListener(e -> seleccionarMecanico());
        
        panelBotones.add(btnActualizar);
        panelBotones.add(btnSeleccionar);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarDatos() {
        // Cargar clientes
        List<Cliente> clientes = clienteRepo.findAll();
        for (Cliente cliente : clientes) {
            cmbClientes.addItem(new ClienteItem(cliente));
        }
        
        // Cargar servicios
        List<Servicio> servicios = servicioRepo.findAll();
        for (Servicio servicio : servicios) {
            cmbServicios.addItem(new ServicioItem(servicio));
        }
        
        // Configurar fecha inicial (mañana)
        spnFecha.setValue(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
    }
    
    private void configurarValidaciones() {
        actualizarVistaPrevia();
    }
    
    private void actualizarVistaPrevia() {
        ServicioItem servicioItem = (ServicioItem) cmbServicios.getSelectedItem();
        ClienteItem clienteItem = (ClienteItem) cmbClientes.getSelectedItem();
        
        if (servicioItem == null || clienteItem == null) {
            lblCostoTotal.setText("Costo Total: Q0.00");
            lblDuracionTotal.setText("Duración: 0 minutos");
            lblDisponibilidad.setText("Estado: Seleccione cliente y servicio");
            txtVistaPrevia.setText("Seleccione cliente y servicio para ver la vista previa");
            txtPartesRequeridas.setText("");
            return;
        }
        
        Servicio servicio = servicioItem.servicio;
        Cliente cliente = clienteItem.cliente;
        
        // Actualizar información básica
        lblCostoTotal.setText(String.format("Costo Total: Q%.2f", servicio.getPrecio()));
        lblDuracionTotal.setText(String.format("Duración: %d minutos", servicio.getDuracionMin()));
        
        // Validar horario y disponibilidad
        validarHorarioYDisponibilidad();
        
        // Generar vista previa
        StringBuilder preview = new StringBuilder();
        preview.append("═══════════════════════════════════════════════════════════\n");
        preview.append("                    VISTA PREVIA DE RESERVA\n");
        preview.append("═══════════════════════════════════════════════════════════\n\n");
        
        preview.append("CLIENTE:\n");
        preview.append("─────────────────────────────────────────────────────────\n");
        preview.append(String.format("Nombre: %s\n", cliente.getNombre()));
        preview.append(String.format("Contacto: %s\n", cliente.getContacto()));
        preview.append(String.format("Vehículo: %s %s (%d)\n\n", 
                                    cliente.getMarcaAuto(), cliente.getModeloAuto(), cliente.getAnioAuto()));
        
        preview.append("SERVICIO:\n");
        preview.append("─────────────────────────────────────────────────────────\n");
        preview.append(String.format("Servicio: %s\n", servicio.getNombre()));
        preview.append(String.format("Duración: %d minutos\n", servicio.getDuracionMin()));
        preview.append(String.format("Precio: Q%.2f\n\n", servicio.getPrecio()));
        
        preview.append("PROGRAMACIÓN:\n");
        preview.append("─────────────────────────────────────────────────────────\n");
        
        java.util.Date fechaDate = (java.util.Date) spnFecha.getValue();
        LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        int hora = (Integer) spnHora.getValue();
        int minutos = (Integer) spnMinutos.getValue();
        
        preview.append(String.format("Fecha: %s\n", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        preview.append(String.format("Hora: %02d:%02d\n", hora, minutos));
        preview.append(String.format("Mecánico: %s\n", txtMecanico.getText().trim()));
        
        LocalDateTime fechaHora = LocalDateTime.of(fecha, LocalTime.of(hora, minutos));
        LocalDateTime horaFin = fechaHora.plusMinutes(servicio.getDuracionMin());
        preview.append(String.format("Hora estimada de finalización: %s\n", 
                                    horaFin.format(DateTimeFormatter.ofPattern("HH:mm"))));
        
        txtVistaPrevia.setText(preview.toString());
        
        // Actualizar partes requeridas
        actualizarPartesRequeridas(servicio);
    }
    
    private void actualizarPartesRequeridas(Servicio servicio) {
        if (parteRepo == null) {
            txtPartesRequeridas.setText("Información de partes no disponible");
            return;
        }
        
        StringBuilder partes = new StringBuilder();
        partes.append("PARTES REQUERIDAS PARA EL SERVICIO:\n");
        partes.append("═══════════════════════════════════════════════════════════\n\n");
        
        Map<String, Integer> partesRequeridas = servicio.getPartesRequeridas();
        
        if (partesRequeridas.isEmpty()) {
            partes.append("Este servicio no requiere partes específicas.\n");
        } else {
            boolean hayProblemas = false;
            double costoTotalPartes = 0.0;
            
            for (Map.Entry<String, Integer> entry : partesRequeridas.entrySet()) {
                String parteId = entry.getKey();
                int cantidadRequerida = entry.getValue();
                
                parteRepo.findById(parteId).ifPresentOrElse(parte -> {
                    String estado = parte.getCantidad() >= cantidadRequerida ? "✅ DISPONIBLE" : "❌ INSUFICIENTE";
                    partes.append(String.format("• %s\n", parte.getNombre()));
                    partes.append(String.format("  Requerida: %d | Disponible: %d | %s\n", 
                                               cantidadRequerida, parte.getCantidad(), estado));
                    partes.append(String.format("  Costo unitario: Q%.2f\n\n", parte.getCosto()));
                }, () -> {
                    partes.append(String.format("• ID: %s\n", parteId));
                    partes.append("  ❌ PARTE NO ENCONTRADA\n\n");
                });
            }
            
            partes.append("─────────────────────────────────────────────────────────\n");
            partes.append("RESUMEN:\n");
            partes.append(String.format("Total de tipos de partes: %d\n", partesRequeridas.size()));
            
            if (hayProblemas) {
                partes.append("⚠️  ATENCIÓN: Hay problemas con el inventario\n");
            } else {
                partes.append("✅ Todas las partes están disponibles\n");
            }
        }
        
        txtPartesRequeridas.setText(partes.toString());
    }
    
    private void validarHorarioYDisponibilidad() {
        java.util.Date fechaDate = (java.util.Date) spnFecha.getValue();
        LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        int hora = (Integer) spnHora.getValue();
        int minutos = (Integer) spnMinutos.getValue();
        
        StringBuilder estado = new StringBuilder("Estado: ");
        boolean valido = true;
        
        // Validar fin de semana
        if (chkValidarFinSemana.isSelected()) {
            if (fecha.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || 
                fecha.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                estado.append("❌ Fin de semana no permitido");
                valido = false;
            }
        }
        
        // Validar horario laboral
        if (chkValidarHorario.isSelected() && valido) {
            if (hora < 6 || hora > 18) {
                estado.append("❌ Fuera del horario laboral (6:00-18:00)");
                valido = false;
            }
        }
        
        if (valido) {
            estado.append("✅ Horario válido");
        }
        
        lblDisponibilidad.setText(estado.toString());
        lblDisponibilidad.setForeground(valido ? new Color(0, 120, 0) : Color.RED);
    }
    
    private void validarDisponibilidad() {
        if (reservaRepo == null) {
            JOptionPane.showMessageDialog(this, "Función de validación no disponible", 
                                        "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        java.util.Date fechaDate = (java.util.Date) spnFecha.getValue();
        LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        
        List<Reserva> reservasDelDia = reservaRepo.findByFecha(fecha);
        
        String mensaje = String.format("Fecha: %s\nReservas programadas: %d\n\n", 
                                     fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                                     reservasDelDia.size());
        
        if (reservasDelDia.isEmpty()) {
            mensaje += "✅ No hay reservas programadas para este día";
        } else {
            mensaje += "Reservas existentes:\n";
            for (Reserva reserva : reservasDelDia) {
                mensaje += String.format("• %s - Mecánico: %s\n", 
                                       reserva.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")),
                                       reserva.getMecanicoAsignado());
            }
        }
        
        JOptionPane.showMessageDialog(this, mensaje, "Disponibilidad del Día", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void actualizarDisponibilidadMecanicos() {
        if (reservaRepo == null) {
            txtResultado.setText("Función de mecánicos no disponible");
            return;
        }
        
        modeloMecanicos.setRowCount(0);
        java.util.Date fechaDate = (java.util.Date) spnFecha.getValue();
        LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        
        List<Reserva> reservasDelDia = reservaRepo.findByFecha(fecha);
        
        // Agregar mecánicos comunes
        String[] mecanicosComunes = {"Carlos Rodriguez", "Maria Gonzalez", "Luis Martinez", "Ana Perez"};
        
        for (String mecanico : mecanicosComunes) {
            long reservasCount = reservasDelDia.stream()
                .filter(r -> r.getMecanicoAsignado().equals(mecanico))
                .count();
            
            String ultimaReserva = reservasDelDia.stream()
                .filter(r -> r.getMecanicoAsignado().equals(mecanico))
                .map(r -> r.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")))
                .reduce((first, second) -> second)
                .orElse("Ninguna");
            
            String estado = reservasCount == 0 ? "Disponible" : 
                           reservasCount <= 2 ? "Ocupado" : "Sobrecargado";
            String recomendacion = reservasCount == 0 ? "Libre" : 
                     reservasCount <= 2 ? "Disponible" : "Ocupado";
            
            modeloMecanicos.addRow(new Object[]{
                mecanico, reservasCount, ultimaReserva, estado, recomendacion
            });
        }
        
        txtResultado.setText("Disponibilidad de mecánicos actualizada para " + 
                           fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
    
    private void seleccionarMecanico() {
        int filaSeleccionada = tablaMecanicos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un mecánico de la tabla", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String mecanicoSeleccionado = (String) modeloMecanicos.getValueAt(filaSeleccionada, 0);
        txtMecanico.setText(mecanicoSeleccionado);
        actualizarVistaPrevia();
        
        // Cambiar a la pestaña de crear reserva
        ((JTabbedPane) getContentPane().getComponent(0)).setSelectedIndex(0);
        
        txtResultado.setText("Mecánico seleccionado: " + mecanicoSeleccionado);
    }
    
    private void cargarReservasExistentes() {
    if (reservaRepo == null) {
        System.out.println("AVISO: repositorio no disponible");
        return;
    }
    
    modeloReservasExistentes.setRowCount(0);
    List<Reserva> reservas = reservaRepo.findAll();
    System.out.println("Reservas encontradas: " + reservas.size());
    
    for (Reserva reserva : reservas) {
        // Obtener nombres de cliente y servicio
        String nombreCliente = clienteRepo.findById(reserva.getClienteId())
            .map(Cliente::getNombre)
            .orElse("Cliente ID: " + reserva.getClienteId());
        
        String nombreServicio = servicioRepo.findById(reserva.getServicioId())
            .map(Servicio::getNombre)
            .orElse("Servicio ID: " + reserva.getServicioId());
        
        // Agregar fila a la tabla (7 columnas, no 8)
        Object[] fila = {
            reserva.getId(),
            nombreCliente,
            nombreServicio,
            reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            reserva.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")),
            reserva.getMecanicoAsignado(),
            reserva.getEstado()
        };
        modeloReservasExistentes.addRow(fila);
    }
    
    System.out.println("Tabla actualizada con " + reservas.size() + " reservas");
}
    
    
    private void aplicarFiltrosReservas() {
        // Esta función aplicaría filtros a la tabla, similar al FormularioInventario
        // Por simplicidad, no implemento el filtro complejo aquí
        txtResultado.setText("Use 'Cargar Reservas' después de cambiar filtros");
    }
    
    private void limpiarFiltrosReservas() {
        txtFiltroBuscar.setText("");
        cmbFiltroMecanico.setSelectedIndex(0);
        cmbFiltroEstado.setSelectedIndex(0);
        spnFiltroFecha.setValue(new java.util.Date());
        cargarReservasExistentes();
    }
    
    private void verDetalleReserva() {
        System.out.println("VER DETALLE EJECUTANDOSE");
    int filaSeleccionada = tablaReservasExistentes.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    DefaultTableModel modelo = (DefaultTableModel) tablaReservasExistentes.getModel();
    String reservaId = (String) modelo.getValueAt(filaSeleccionada, 0);
    String cliente = (String) modelo.getValueAt(filaSeleccionada, 1);
    String servicio = (String) modelo.getValueAt(filaSeleccionada, 2);
    String fecha = (String) modelo.getValueAt(filaSeleccionada, 3);
    String hora = (String) modelo.getValueAt(filaSeleccionada, 4);
    String mecanico = (String) modelo.getValueAt(filaSeleccionada, 5);
    String estado = (String) modelo.getValueAt(filaSeleccionada, 6);
    
    StringBuilder detalle = new StringBuilder();
    detalle.append("═══════════════════════════════════════════════════════════\n");
    detalle.append("                    DETALLE DE RESERVA\n");
    detalle.append("═══════════════════════════════════════════════════════════\n\n");
    detalle.append(String.format("ID: %s\n", reservaId));
    detalle.append(String.format("Cliente: %s\n", cliente));
    detalle.append(String.format("Servicio: %s\n", servicio));
    detalle.append(String.format("Fecha: %s %s\n", fecha, hora));
    detalle.append(String.format("Mecánico: %s\n", mecanico));
    detalle.append(String.format("Estado: %s\n", estado));
    
    txtDetalleReserva.setText(detalle.toString());
}
    
    private void editarReservaSeleccionada() {
        int filaSeleccionada = tablaReservasExistentes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String reservaId = (String) modeloReservasExistentes.getValueAt(filaSeleccionada, 0);
        
        // Por simplicidad, mostramos mensaje informativo
        // En una implementación completa, cargaríamos los datos en el formulario
        JOptionPane.showMessageDialog(this, 
            "Función de edición en desarrollo.\n" +
            "Para cambiar el estado de la reserva, use:\n" +
            "Menú Principal → Opción 3: Cambiar Estado de Reserva\n\n" +
            "ID de la reserva seleccionada: " + reservaId,
            "Editar Reserva", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void eliminarReservaSeleccionada() {
    int filaSeleccionada = tablaReservasExistentes.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Obtener el modelo actual (puede ser filtrado)
    DefaultTableModel modeloActual = (DefaultTableModel) tablaReservasExistentes.getModel();
    
    String reservaId = (String) modeloActual.getValueAt(filaSeleccionada, 0);
    String cliente = (String) modeloActual.getValueAt(filaSeleccionada, 1);
    String fecha = (String) modeloActual.getValueAt(filaSeleccionada, 3);
    String hora = (String) modeloActual.getValueAt(filaSeleccionada, 4);
    
    int confirmacion = JOptionPane.showConfirmDialog(this,
        "¿Está seguro que desea eliminar esta reserva?\n\n" +
        "Cliente: " + cliente + "\n" +
        "Fecha: " + fecha + " " + hora + "\n" +
        "ID: " + reservaId + "\n\n" +
        "Esta acción no se puede deshacer.",
        "Confirmar Eliminación",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    
    if (confirmacion == JOptionPane.YES_OPTION) {
        try {
            servicioReserva.deleteReserva(reservaId);
            System.out.println("Reserva eliminada: " + cliente + " - " + fecha);
            cargarReservasExistentes(); // Recargar la tabla
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar reserva: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
    
    private void crearReserva() {
        try {
            ClienteItem clienteItem = (ClienteItem) cmbClientes.getSelectedItem();
            ServicioItem servicioItem = (ServicioItem) cmbServicios.getSelectedItem();
            String mecanico = txtMecanico.getText().trim();
            
            if (clienteItem == null || servicioItem == null) {
                JOptionPane.showMessageDialog(this, "Seleccione cliente y servicio", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (mecanico.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el mecánico asignado", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Validar que el mecánico existe en la lista autorizada
String[] mecanicosAutorizados = {"Carlos Rodriguez", "Maria Gonzalez", "Luis Martinez", "Ana Perez"};
boolean mecanicoValido = false;
for (String mecanicoAutorizado : mecanicosAutorizados) {
    if (mecanicoAutorizado.equalsIgnoreCase(mecanico)) {
        mecanicoValido = true;
        break;
    }
}

if (!mecanicoValido) {
    JOptionPane.showMessageDialog(this, 
        "El mecánico '" + mecanico + "' no está registrado en el sistema.\n" +
        "Mecánicos disponibles:\n" +
        "• Carlos Rodriguez\n" +
        "• Maria Gonzalez\n" +
        "• Luis Martinez\n" +
        "• Ana Perez", 
        "Mecánico No Válido", JOptionPane.ERROR_MESSAGE);
    return;
}
            
            // Validar horario si está habilitada la validación
            if (!lblDisponibilidad.getText().contains("✅")) {
                int confirmacion = JOptionPane.showConfirmDialog(this,
                    "El horario seleccionado tiene restricciones.\n¿Desea continuar de todas formas?",
                    "Confirmar Reserva",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            java.util.Date fechaDate = (java.util.Date) spnFecha.getValue();
            LocalDate fecha = fechaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            int hora = (Integer) spnHora.getValue();
            int minutos = (Integer) spnMinutos.getValue();
            
            LocalDateTime fechaHora = LocalDateTime.of(fecha, LocalTime.of(hora, minutos));
            
            Reserva nuevaReserva = servicioReserva.createReserva(
                clienteItem.cliente.getId(),
                servicioItem.servicio.getId(),
                fechaHora,
                mecanico
            );
            
            txtResultado.setText("✅ Reserva creada exitosamente:\n" + 
                               "ID: " + nuevaReserva.getId() + "\n" +
                               "Cliente: " + clienteItem.cliente.getNombre() + "\n" +
                               "Servicio: " + servicioItem.servicio.getNombre() + "\n" +
                               "Fecha: " + fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                               "Mecánico: " + mecanico);
            
            limpiarCampos();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limpiarCampos() {
        if (cmbClientes.getItemCount() > 0) cmbClientes.setSelectedIndex(0);
        if (cmbServicios.getItemCount() > 0) cmbServicios.setSelectedIndex(0);
        txtMecanico.setText("");
        spnFecha.setValue(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        spnHora.setValue(8);
        spnMinutos.setValue(0);
        actualizarVistaPrevia();
    }
    
    private JPanel crearPanelReservasExistentes() {
    JPanel panel = new JPanel(new BorderLayout());
    
    // Panel superior con botón cargar y búsqueda
    JPanel panelSuperior = new JPanel(new FlowLayout());
    
    JButton btnCargar = new JButton("Cargar Reservas");
    btnCargar.addActionListener(e -> cargarReservasExistentes());
    panelSuperior.add(btnCargar);
    
    panelSuperior.add(new JLabel("Buscar:"));
    txtFiltroBuscar = new JTextField(15);
    txtFiltroBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            aplicarFiltrosBusqueda();
        }
    });
    panelSuperior.add(txtFiltroBuscar);
    
    panel.add(panelSuperior, BorderLayout.NORTH);
    
    // Tabla
    String[] cols = {"ID", "Cliente", "Servicio", "Fecha", "Hora", "Mecánico", "Estado"};
    modeloReservasExistentes = new DefaultTableModel(cols, 0);
    tablaReservasExistentes = new JTable(modeloReservasExistentes);
    JScrollPane scroll = new JScrollPane(tablaReservasExistentes);
    panel.add(scroll, BorderLayout.CENTER);
    
    // Botón eliminar abajo
    JButton btnEliminar = new JButton("Eliminar Seleccionada");
    btnEliminar.addActionListener(e -> eliminarReservaSeleccionada());
    panel.add(btnEliminar, BorderLayout.SOUTH);
    
    return panel;
}
    
    // Función corregida para la búsqueda de reservas
private void aplicarFiltrosBusqueda() {
    String textoBuscar = txtFiltroBuscar.getText().toLowerCase().trim();
    
    if (textoBuscar.isEmpty()) {
        // Si no hay texto, mostrar todas las reservas
        cargarReservasExistentes();
        return;
    }
    
    try {
        // Limpiar la tabla actual
        modeloReservasExistentes.setRowCount(0);
        
        if (reservaRepo != null) {
            List<Reserva> todasLasReservas = reservaRepo.findAll();
            
            for (Reserva reserva : todasLasReservas) {
                // Obtener los nombres completos
                String nombreCliente = clienteRepo.findById(reserva.getClienteId())
                    .map(Cliente::getNombre)
                    .orElse("Cliente ID: " + reserva.getClienteId());
                
                String nombreServicio = servicioRepo.findById(reserva.getServicioId())
                    .map(Servicio::getNombre)
                    .orElse("Servicio ID: " + reserva.getServicioId());
                
                // Crear el texto completo para buscar (en minúsculas)
                String textoCompleto = (
                    reserva.getId() + " " + 
                    nombreCliente + " " + 
                    nombreServicio + " " + 
                    reserva.getMecanicoAsignado() + " " + 
                    reserva.getEstado() + " " +
                    reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " +
                    reserva.getFecha().format(DateTimeFormatter.ofPattern("HH:mm"))
                ).toLowerCase();
                
                // Si contiene el texto buscado, agregar a la tabla
                if (textoCompleto.contains(textoBuscar)) {
                    Object[] fila = {
                        reserva.getId(),
                        nombreCliente,
                        nombreServicio,
                        reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        reserva.getFecha().format(DateTimeFormatter.ofPattern("HH:mm")),
                        reserva.getMecanicoAsignado(),
                        reserva.getEstado()
                    };
                    // Agregar directamente al modelo existente
                    modeloReservasExistentes.addRow(fila);
                }
            }
            
            // Notificar a la tabla que los datos cambiaron
            modeloReservasExistentes.fireTableDataChanged();
            
            System.out.println("Búsqueda completada. Texto: '" + textoBuscar + 
                             "', Resultados: " + modeloReservasExistentes.getRowCount());
        }
        
    } catch (Exception e) {
        System.err.println("Error en búsqueda: " + e.getMessage());
        e.printStackTrace();
    }
}

// También necesitas corregir el método que crea el panel de reservas existentes


// Método auxiliar para debug
private void debugBusqueda(String textoBuscar, int resultados) {
    System.out.println("=== DEBUG BÚSQUEDA ===");
    System.out.println("Texto buscado: '" + textoBuscar + "'");
    System.out.println("Resultados encontrados: " + resultados);
    System.out.println("Filas en tabla: " + tablaReservasExistentes.getRowCount());
    System.out.println("Modelo tiene " + modeloReservasExistentes.getRowCount() + " filas");
    System.out.println("======================");
}
    
private void agregarClienteRapido() {
    String nombre = JOptionPane.showInputDialog(this, "Nombre del cliente:");
    if (nombre == null || nombre.trim().isEmpty()) {
        return; // Usuario canceló o no ingresó nada
    }
    
    String contacto = JOptionPane.showInputDialog(this, "Contacto (teléfono/email):");
    if (contacto == null) contacto = "";
    
    String marca = JOptionPane.showInputDialog(this, "Marca del vehículo:");
    if (marca == null) marca = "";
    
    String modelo = JOptionPane.showInputDialog(this, "Modelo del vehículo:");
    if (modelo == null) modelo = "";
    
    String anioStr = JOptionPane.showInputDialog(this, "Año del vehículo:");
    if (anioStr == null) anioStr = "2020";
    
    try {
        int anio = Integer.parseInt(anioStr);
        String id = "CLI_" + System.currentTimeMillis(); // ID único basado en timestamp
        
        Cliente nuevoCliente = new Cliente(id, nombre.trim(), contacto.trim(), 
                                         marca.trim(), modelo.trim(), anio);
        
        // Guardar el cliente usando el repositorio
        clienteRepo.save(nuevoCliente);
        
        // Agregar al ComboBox sin recargar todo
        cmbClientes.addItem(new ClienteItem(nuevoCliente));
        
        // Seleccionar el nuevo cliente
        cmbClientes.setSelectedIndex(cmbClientes.getItemCount() - 1);
        
        // Actualizar vista previa
        actualizarVistaPrevia();
        
        txtResultado.setText("Cliente agregado exitosamente: " + nombre);
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Año debe ser un número válido", 
                                    "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error agregando cliente: " + ex.getMessage(), 
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    
    
    // Clases helper para ComboBox
    private static class ClienteItem {
        Cliente cliente;
        
        ClienteItem(Cliente cliente) {
            this.cliente = cliente;
        }
        
        @Override
        public String toString() {
            return cliente.getNombre() + " (" + cliente.getMarcaAuto() + " " + cliente.getModeloAuto() + ")";
        }
    }
    
    private static class ServicioItem {
        Servicio servicio;
        
        ServicioItem(Servicio servicio) {
            this.servicio = servicio;
        }
        
        @Override
        public String toString() {
            return servicio.getNombre() + " - Q" + servicio.getPrecio() + " (" + servicio.getDuracionMin() + " min)";
        }
    }
}