package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.mycompany.proyectofinalpoo.Reserva;
import com.mycompany.proyectofinalpoo.ReservaEstado;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.CalendarioReservas;
import com.mycompany.proyectofinalpoo.repo.servicios.dto.DiaCalendario;
import com.mycompany.proyectofinalpoo.gui.EstiloCombos;
import com.mycompany.proyectofinalpoo.gui.componentes.SelectorFechaPopup;

public class VistaCalendario extends JPanel {
    private final ServicioReserva servicioReserva;
    private final com.mycompany.proyectofinalpoo.repo.ParteRepo parteRepo;
    private final com.mycompany.proyectofinalpoo.repo.ServicioRepo servicioRepo;

    private final JSpinner fechaInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner fechaFin = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> mecanico = new JComboBox<>();
    private final JComboBox<ReservaEstado> estado = new JComboBox<>();
    private final JButton btnAplicar = new JButton("Aplicar");
    private final JButton btnHoy = new JButton("Hoy");
    private final JButton btnMes = new JButton("Mes actual");
    private final JButton btnCerrar = new JButton("Cerrar reserva");
    private final JButton btnLimpiarAgenda = new JButton("Limpiar filtros");
    private final JButton btnLimpiarCalendario = new JButton("Limpiar filtros");

    private final DefaultTableModel modelDias = new DefaultTableModel(new Object[]{"Fecha","Total","Programadas","En Progreso","Finalizadas","Entregadas"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tablaDias = new JTable(modelDias);
    private final DefaultTableModel modelDetalle = new DefaultTableModel(new Object[]{"Fecha","Cliente","Mecánico","Servicio","Estado","ID"},0){ public boolean isCellEditable(int r,int c){return false;} };
    private final JTable tablaDetalle = new JTable(modelDetalle);

    private final JComboBox<String> calendarioMecanico = new JComboBox<>();
    private final JComboBox<ReservaEstado> calendarioEstado = new JComboBox<>();
    private CardLayout vistasLayout;
    private JPanel panelVistas;
    private JToggleButton tabAgenda;
    private JToggleButton tabCalendario;
    private JLabel lblMesActual;
    private JPanel gridCalendario;
    private YearMonth mesActual = YearMonth.now();
    private final Locale LOCALE_ES = new Locale("es", "ES");
    private final DateTimeFormatter MES_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_ES);
    private final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private boolean cambiandoVista = false;
    private static final int MAX_RESERVAS_POR_CELDA = 3;

    private boolean actualizandoOpciones = false;

    public VistaCalendario(ServicioReserva servicioReserva,
                           com.mycompany.proyectofinalpoo.repo.ParteRepo parteRepo,
                           com.mycompany.proyectofinalpoo.repo.ServicioRepo servicioRepo) {
        this.servicioReserva = servicioReserva;
        this.parteRepo = parteRepo;
        this.servicioRepo = servicioRepo;

        java.util.Date hoyDate = java.util.Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (fechaInicio.getModel() instanceof javax.swing.SpinnerDateModel modelIni) {
            modelIni.setStart(hoyDate);
            if (modelIni.getValue() instanceof java.util.Date val && val.before(hoyDate)) {
                fechaInicio.setValue(hoyDate);
            }
        }
        if (fechaFin.getModel() instanceof javax.swing.SpinnerDateModel modelFin) {
            modelFin.setStart(hoyDate);
            if (modelFin.getValue() instanceof java.util.Date val && val.before(hoyDate)) {
                fechaFin.setValue(hoyDate);
            }
        }

        setOpaque(false);
        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        tabAgenda = crearTabToggle("Agenda");
        tabCalendario = crearTabToggle("Calendario");

        ButtonGroup grupoTabs = new ButtonGroup();
        grupoTabs.add(tabAgenda);
        grupoTabs.add(tabCalendario);

        JPanel panelTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelTabs.setOpaque(false);
        panelTabs.add(tabAgenda);
        panelTabs.add(tabCalendario);
        add(panelTabs, BorderLayout.NORTH);

        tabAgenda.addActionListener(e -> mostrarVista("AGENDA"));
        tabCalendario.addActionListener(e -> mostrarVista("CALENDARIO"));

        vistasLayout = new CardLayout();
        panelVistas = new JPanel(vistasLayout);
        panelVistas.setOpaque(false);
        panelVistas.add(crearPanelAgenda(), "AGENDA");
        panelVistas.add(crearPanelCalendario(), "CALENDARIO");
        add(panelVistas, BorderLayout.CENTER);

        tabAgenda.setSelected(true);
        mostrarVista("AGENDA");

        LocalDate hoy = LocalDate.now();
        LocalDate inicioAg = hoy;
        LocalDate finAg = hoy.withDayOfMonth(hoy.lengthOfMonth());
        fechaInicio.setValue(java.util.Date.from(inicioAg.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        fechaFin.setValue(java.util.Date.from(finAg.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        asegurarOpcionesMecanicos();
        refrescar();
        refrescarCalendario();
    }

    private JPanel crearPanelAgenda() {
        JPanel contenedor = new JPanel(new BorderLayout(10,10));
        contenedor.setOpaque(false);

        TemaNeoBlue.CardPanel filtros = new TemaNeoBlue.CardPanel();
        filtros.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        fechaInicio.setEditor(new JSpinner.DateEditor(fechaInicio,"yyyy-MM-dd"));
        fechaFin.setEditor(new JSpinner.DateEditor(fechaFin,"yyyy-MM-dd"));
        estado.removeAllItems();
        estado.addItem(null);
        for (ReservaEstado e : ReservaEstado.values()) estado.addItem(e);

        mecanico.setPreferredSize(new Dimension(300, 32));
        estado.setPreferredSize(new Dimension(260, 32));
        EstiloCombos.aplicarDarkAzul(mecanico);
        EstiloCombos.aplicarDarkAzul(estado);

        gc.gridx=0; gc.gridy=0; filtros.add(new JLabel("Inicio"), gc);
        gc.gridx=1; gc.gridy=0; filtros.add(SelectorFechaPopup.adjuntar(fechaInicio), gc);
        gc.gridx=2; gc.gridy=0; filtros.add(new JLabel("Fin"), gc);
        gc.gridx=3; gc.gridy=0; filtros.add(SelectorFechaPopup.adjuntar(fechaFin), gc);

        gc.gridx=0; gc.gridy=1; filtros.add(new JLabel("Mecánico"), gc);
        gc.gridx=1; gc.gridy=1; filtros.add(mecanico, gc);
        gc.gridx=2; gc.gridy=1; filtros.add(new JLabel("Estado"), gc);
        gc.gridx=3; gc.gridy=1; filtros.add(estado, gc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnAplicar);
        acciones.add(btnHoy);
        acciones.add(btnMes);
        acciones.add(btnCerrar);
        acciones.add(btnLimpiarAgenda);
        gc.gridx=0; gc.gridy=2; gc.gridwidth=4; filtros.add(acciones, gc);

        tablaDias.setRowHeight(22);
        tablaDias.setFillsViewportHeight(true);
        tablaDetalle.setRowHeight(22);
        tablaDetalle.setFillsViewportHeight(true);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaDias), new JScrollPane(tablaDetalle));
        split.setResizeWeight(0.6);

        contenedor.add(filtros, BorderLayout.NORTH);
        TemaNeoBlue.CardPanel cardTabla = new TemaNeoBlue.CardPanel();
        cardTabla.setLayout(new BorderLayout());
        cardTabla.add(split, BorderLayout.CENTER);
        contenedor.add(cardTabla, BorderLayout.CENTER);

        btnAplicar.addActionListener(e -> { asegurarOpcionesMecanicos(); refrescar(); });
        estado.addActionListener(e -> { if (!actualizandoOpciones) { asegurarOpcionesMecanicos(); refrescar(); } });
        mecanico.addActionListener(e -> { if (!actualizandoOpciones) refrescar(); });
        btnHoy.addActionListener(e -> rangoHoy());
        btnMes.addActionListener(e -> rangoMesActual());
        btnCerrar.addActionListener(e -> cerrarSeleccionada());
        tablaDias.getSelectionModel().addListSelectionListener(e -> cargarDetalleSeleccion());
        btnLimpiarAgenda.addActionListener(e -> limpiarFiltrosAgenda());

        return contenedor;
    }

    private JToggleButton crearTabToggle(String texto) {
        JToggleButton toggle = new JToggleButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean selected = isSelected();

                int w = getWidth();
                int h = getHeight();
                int base = 12;

                Color bg = selected ? TemaNeoBlue.ACCENT : new Color(45, 68, 102, 180);
                Color border = selected ? new Color(190, 230, 255, 190) : new Color(110, 140, 200, 90);
                Color shadow = new Color(0, 0, 0, 80);

                Polygon tabShape = new Polygon();
                tabShape.addPoint(0, h);
                tabShape.addPoint(0, base);
                tabShape.addPoint(base, 0);
                tabShape.addPoint(w - base, 0);
                tabShape.addPoint(w, base);
                tabShape.addPoint(w, h);

                g2.setColor(shadow);
                Polygon shadowShape = new Polygon();
                for (int i = 0; i < tabShape.npoints; i++) {
                    shadowShape.addPoint(tabShape.xpoints[i], tabShape.ypoints[i] + 3);
                }
                g2.fillPolygon(shadowShape);

                g2.setColor(bg);
                g2.fillPolygon(tabShape);

                g2.setStroke(new BasicStroke(1.4f));
                g2.setColor(border);
                g2.drawPolygon(tabShape);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                int textX = (w - fm.stringWidth(txt)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(Color.WHITE);
                g2.drawString(txt, textX, textY);
                g2.dispose();
            }
        };
        toggle.setUI(new BasicToggleButtonUI());
        toggle.setFocusPainted(false);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setOpaque(false);
        toggle.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 14f));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setMargin(new Insets(10, 28, 10, 28));
        toggle.setHorizontalAlignment(SwingConstants.CENTER);
        toggle.addChangeListener(e -> toggle.repaint());
        return toggle;
    }

    private void mostrarVista(String clave) {
        if (vistasLayout == null || panelVistas == null) return;
        if (cambiandoVista) return;
        cambiandoVista = true;
        try {
            vistasLayout.show(panelVistas, clave);
            boolean agenda = "AGENDA".equals(clave);
            if (tabAgenda != null) tabAgenda.setSelected(agenda);
            if (tabCalendario != null) tabCalendario.setSelected(!agenda);
            if (tabAgenda != null) tabAgenda.repaint();
            if (tabCalendario != null) tabCalendario.repaint();
            if (agenda) {
                asegurarOpcionesMecanicos();
                refrescar();
            } else {
                refrescarCalendario();
            }
        } finally {
            cambiandoVista = false;
        }
    }

    private void refrescarCalendario() {
        if (gridCalendario == null) return;
        actualizarEtiquetaMes();

        String mecSel = (String) calendarioMecanico.getSelectedItem();
        String filtroMecanico = (mecSel == null || mecSel.equalsIgnoreCase("Todos")) ? null : mecSel;
        ReservaEstado filtroEstado = (ReservaEstado) calendarioEstado.getSelectedItem();

        LocalDate inicio = mesActual.atDay(1);
        LocalDate fin = mesActual.atEndOfMonth();

        CalendarioReservas cal = servicioReserva.generarCalendario(inicio, fin, filtroMecanico, filtroEstado);
        Map<LocalDate, DiaCalendario> porDia = new HashMap<>();
        if (cal != null && cal.obtenerDias() != null) {
            for (DiaCalendario d : cal.obtenerDias()) {
                porDia.put(d.obtenerFecha(), d);
            }
        }

        gridCalendario.removeAll();

        int offset = inicio.getDayOfWeek().getValue() - 1; // lunes=0
        LocalDate cursor = inicio.minusDays(offset);
        for (int i = 0; i < 42; i++) {
            LocalDate dia = cursor.plusDays(i);
            DiaCalendario datos = porDia.get(dia);
            List<Reserva> reservas = (datos == null) ? Collections.emptyList() : new ArrayList<>(datos.obtenerReservas());
            JPanel celda = crearCeldaCalendario(dia, reservas, dia.getMonth().equals(mesActual.getMonth()));
            gridCalendario.add(celda);
        }

        gridCalendario.revalidate();
        gridCalendario.repaint();
    }

    private void actualizarEtiquetaMes() {
        if (lblMesActual == null) return;
        String texto = capitalizar(MES_FMT.format(mesActual));
        lblMesActual.setText(texto);
    }

    private JPanel crearCeldaCalendario(LocalDate dia, List<Reserva> reservas, boolean esMesActual) {
        JPanel celda = new JPanel(new BorderLayout());
        celda.setOpaque(true);
        Color fondo = esMesActual ? new Color(34, 57, 94, 210) : new Color(24, 36, 60, 200);
        celda.setBackground(fondo);
        celda.setBorder(BorderFactory.createLineBorder(new Color(40, 62, 100, esMesActual ? 180 : 90), esMesActual ? 1 : 0));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel lblDia = new JLabel(String.valueOf(dia.getDayOfMonth()));
        lblDia.setForeground(esMesActual ? TemaNeoBlue.TXT : TemaNeoBlue.TXT_DIM);
        lblDia.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 14f));
        encabezado.add(lblDia, BorderLayout.WEST);

        if (dia.equals(LocalDate.now())) {
            JLabel lblHoy = new JLabel("Hoy");
            lblHoy.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 10f));
            lblHoy.setForeground(TemaNeoBlue.ACCENT);
            encabezado.add(lblHoy, BorderLayout.EAST);
            celda.setBorder(BorderFactory.createLineBorder(TemaNeoBlue.ACCENT));
        }

        celda.add(encabezado, BorderLayout.NORTH);

        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));

        if (reservas == null || reservas.isEmpty()) {
            JLabel vacio = new JLabel("Sin reservas");
            vacio.setForeground(TemaNeoBlue.TXT_DIM);
            vacio.setFont(TemaNeoBlue.FONT.deriveFont(Font.PLAIN, 10f));
            lista.add(vacio);
        } else {
            int count = 0;
            for (Reserva r : reservas) {
                if (count >= MAX_RESERVAS_POR_CELDA) {
                    JLabel mas = new JLabel("+" + (reservas.size() - count) + " más");
                    mas.setForeground(TemaNeoBlue.TXT_DIM);
                    mas.setFont(TemaNeoBlue.FONT.deriveFont(Font.PLAIN, 10f));
                    mas.setAlignmentX(Component.LEFT_ALIGNMENT);
                    lista.add(mas);
                    break;
                }
                String texto = HORA_FMT.format(r.getFecha()) + " " + servicioReserva.obtenerNombreCliente(r.getClienteId());
                JLabel item = new JLabel(texto);
                item.setForeground(Color.WHITE);
                item.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 10f));
                JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                pill.setOpaque(true);
                pill.setBackground(colorEstado(r.getEstado()));
                pill.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
                pill.add(item);
                pill.setAlignmentX(Component.LEFT_ALIGNMENT);
                lista.add(pill);
                count++;
            }
        }

        celda.add(lista, BorderLayout.CENTER);

        final List<Reserva> reservasDia = reservas == null ? Collections.emptyList() : new ArrayList<>(reservas);
        celda.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    mostrarDetalleDia(dia, reservasDia);
                } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    seleccionarDiaEnAgenda(dia);
                }
            }
        });

        return celda;
    }

    private void mostrarDetalleDia(LocalDate dia, List<Reserva> reservas) {
        String titulo = "Reservas del " + dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        if (reservas == null || reservas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay reservas para este día.", titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Reserva r : reservas) {
            sb.append(HORA_FMT.format(r.getFecha()))
              .append("  ")
              .append(servicioReserva.obtenerNombreCliente(r.getClienteId()))
              .append(" - ")
              .append(servicioReserva.obtenerNombreServicio(r.getServicioId()))
              .append(" [").append(r.getEstado().name()).append("]")
              .append("\n");
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(TemaNeoBlue.BG);
        area.setForeground(TemaNeoBlue.TXT);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(360, 220));
        JOptionPane.showMessageDialog(this, scroll, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void seleccionarDiaEnAgenda(LocalDate dia) {
        fechaInicio.setValue(toDate(dia));
        fechaFin.setValue(toDate(dia));
        mostrarVista("AGENDA");
        seleccionarFilaAgenda(dia);
    }

    private void seleccionarFilaAgenda(LocalDate dia) {
        for (int row = 0; row < modelDias.getRowCount(); row++) {
            Object val = modelDias.getValueAt(row, 0);
            LocalDate fecha = val instanceof LocalDate ? (LocalDate) val : LocalDate.parse(String.valueOf(val));
            if (fecha.equals(dia)) {
                int viewRow = tablaDias.convertRowIndexToView(row);
                tablaDias.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                tablaDias.scrollRectToVisible(tablaDias.getCellRect(viewRow, 0, true));
                cargarDetalleSeleccion();
                return;
            }
        }
    }

    private Color colorEstado(ReservaEstado estado) {
        if (estado == null) return new Color(95, 115, 150);
        return switch (estado) {
            case PROGRAMADA -> new Color(70, 120, 200);
            case EN_PROGRESO -> new Color(245, 166, 35);
            case FINALIZADA -> new Color(60, 160, 120);
            case ENTREGADA -> new Color(140, 110, 200);
            default -> new Color(95, 115, 150);
        };
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        texto = texto.toLowerCase(LOCALE_ES);
        return texto.substring(0,1).toUpperCase(LOCALE_ES) + texto.substring(1);
    }

    private JPanel crearPanelCalendario() {
        JPanel contenedor = new JPanel(new BorderLayout(10,10));
        contenedor.setOpaque(false);

        TemaNeoBlue.CardPanel controles = new TemaNeoBlue.CardPanel();
        controles.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnPrev = new JButton("◀");
        JButton btnNext = new JButton("▶");
        JButton btnHoyCal = new JButton("Hoy");
        btnPrev.addActionListener(e -> { mesActual = mesActual.minusMonths(1); refrescarCalendario(); });
        btnNext.addActionListener(e -> { mesActual = mesActual.plusMonths(1); refrescarCalendario(); });
        btnHoyCal.addActionListener(e -> { mesActual = YearMonth.now(); refrescarCalendario(); });

        lblMesActual = new JLabel();
        lblMesActual.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 18f));
        lblMesActual.setForeground(TemaNeoBlue.TXT);
        lblMesActual.setHorizontalAlignment(SwingConstants.CENTER);

        calendarioMecanico.setPreferredSize(new Dimension(220, 32));
        calendarioEstado.setPreferredSize(new Dimension(220, 32));
        EstiloCombos.aplicarDarkAzul(calendarioMecanico);
        EstiloCombos.aplicarDarkAzul(calendarioEstado);
        calendarioMecanico.addItem("Todos");
        calendarioEstado.addItem(null);
        for (ReservaEstado e : ReservaEstado.values()) calendarioEstado.addItem(e);

        calendarioMecanico.addActionListener(e -> { if (!actualizandoOpciones) refrescarCalendario(); });
        calendarioEstado.addActionListener(e -> { if (!actualizandoOpciones) refrescarCalendario(); });
        btnLimpiarCalendario.addActionListener(e -> limpiarFiltrosCalendario());

        gc.gridx = 0; gc.gridy = 0; controles.add(btnPrev, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; controles.add(lblMesActual, gc);
        gc.gridx = 2; gc.gridy = 0; gc.weightx = 0; controles.add(btnNext, gc);
        gc.gridx = 3; gc.gridy = 0; controles.add(btnHoyCal, gc);

        gc.gridx = 0; gc.gridy = 1; controles.add(new JLabel("Mecánico"), gc);
        gc.gridx = 1; gc.gridy = 1; controles.add(calendarioMecanico, gc);
        gc.gridx = 2; gc.gridy = 1; controles.add(new JLabel("Estado"), gc);
        gc.gridx = 3; gc.gridy = 1; controles.add(calendarioEstado, gc);
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 4; gc.anchor = GridBagConstraints.WEST; controles.add(btnLimpiarCalendario, gc);

        TemaNeoBlue.CardPanel cuerpo = new TemaNeoBlue.CardPanel();
        cuerpo.setLayout(new BorderLayout(8,8));

        JPanel encabezadoDias = new JPanel(new GridLayout(1,7));
        encabezadoDias.setOpaque(false);
        for (int i = 1; i <= 7; i++) {
            DayOfWeek dow = DayOfWeek.of(i);
            JLabel lbl = new JLabel(dow.getDisplayName(TextStyle.SHORT, LOCALE_ES).toUpperCase(LOCALE_ES), SwingConstants.CENTER);
            lbl.setForeground(TemaNeoBlue.TXT_DIM);
            lbl.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 12f));
            encabezadoDias.add(lbl);
        }

        gridCalendario = new JPanel(new GridLayout(6, 7, 8, 8));
        gridCalendario.setOpaque(false);

        cuerpo.add(encabezadoDias, BorderLayout.NORTH);
        cuerpo.add(gridCalendario, BorderLayout.CENTER);

        contenedor.add(controles, BorderLayout.NORTH);
        contenedor.add(cuerpo, BorderLayout.CENTER);

        return contenedor;
    }

    public void refrescarDesdeExterno() {
        SwingUtilities.invokeLater(() -> { asegurarOpcionesMecanicos(); refrescar(); refrescarCalendario(); });
    }

    public void refrescarPorCambio(LocalDate fechaReferencia) {
        SwingUtilities.invokeLater(() -> {
            if (fechaReferencia != null) {
                LocalDate focus = fechaReferencia.isBefore(LocalDate.now()) ? LocalDate.now() : fechaReferencia;
                fechaInicio.setValue(toDate(focus));
                fechaFin.setValue(toDate(focus.withDayOfMonth(focus.lengthOfMonth())));
                mesActual = YearMonth.from(focus);
            }
            asegurarOpcionesMecanicos();
            refrescar();
            refrescarCalendario();
        });
    }

    private void limpiarFiltrosAgenda() {
        LocalDate hoy = LocalDate.now();
        fechaInicio.setValue(toDate(hoy));
        fechaFin.setValue(toDate(hoy.withDayOfMonth(hoy.lengthOfMonth())));
        asegurarOpcionesMecanicos();
        if (mecanico.isEnabled()) mecanico.setSelectedItem("Todos");
        estado.setSelectedItem(null);
        refrescar();
    }

    private void limpiarFiltrosCalendario() {
        mesActual = YearMonth.now();
        if (calendarioMecanico.isEnabled()) calendarioMecanico.setSelectedItem("Todos");
        calendarioEstado.setSelectedItem(null);
        refrescarCalendario();
    }

    private void asegurarOpcionesMecanicos() {
        actualizandoOpciones = true;
        try {
            LocalDate ini = toLocal((java.util.Date) fechaInicio.getValue());
            LocalDate fin = toLocal((java.util.Date) fechaFin.getValue());
            if (ini.isAfter(fin)) {
                LocalDate tmp = ini; ini = fin; fin = tmp;
                fechaInicio.setValue(toDate(ini));
                fechaFin.setValue(toDate(fin));
            }
            ReservaEstado est = (ReservaEstado) estado.getSelectedItem();

            Set<String> nombres = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, null, est);
            if (cal != null && cal.obtenerDias() != null) {
                for (DiaCalendario d : cal.obtenerDias()) {
                    if (d.obtenerReservas() == null) continue;
                    for (Reserva r : d.obtenerReservas()) {
                        String mec = r.getMecanicoAsignado();
                        if (mec != null && !mec.isBlank()) nombres.add(mec.trim());
                    }
                }
            }

            if (mecanico.isEnabled()) {
                String seleccionAnterior = (String) mecanico.getSelectedItem();
                mecanico.removeAllItems();
                mecanico.addItem("Todos");
                for (String n : nombres) mecanico.addItem(n);
                if (seleccionAnterior != null) {
                    boolean sigue = seleccionAnterior.equalsIgnoreCase("Todos") || nombres.stream().anyMatch(x -> x.equalsIgnoreCase(seleccionAnterior));
                    if (sigue) mecanico.setSelectedItem(seleccionAnterior);
                }
            }

            if (calendarioMecanico.isEnabled()) {
                String seleccionCalendario = (String) calendarioMecanico.getSelectedItem();
                calendarioMecanico.removeAllItems();
                calendarioMecanico.addItem("Todos");
                for (String n : nombres) calendarioMecanico.addItem(n);
                if (seleccionCalendario != null) {
                    boolean sigue = seleccionCalendario.equalsIgnoreCase("Todos") || nombres.stream().anyMatch(x -> x.equalsIgnoreCase(seleccionCalendario));
                    if (sigue) calendarioMecanico.setSelectedItem(seleccionCalendario);
                }
                if (calendarioMecanico.getItemCount() == 0) calendarioMecanico.addItem("Todos");
            }
        } finally {
            actualizandoOpciones = false;
        }
    }

    private void refrescar() {
        LocalDate ini = toLocal((java.util.Date) fechaInicio.getValue());
        LocalDate fin = toLocal((java.util.Date) fechaFin.getValue());
        if (ini.isAfter(fin)) {
            LocalDate tmp = ini; ini = fin; fin = tmp;
            fechaInicio.setValue(toDate(ini));
            fechaFin.setValue(toDate(fin));
        }
        String mecSel = (String) mecanico.getSelectedItem();
        String filtroMecanico = (mecSel == null || mecSel.equalsIgnoreCase("Todos")) ? null : mecSel;
        ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
        CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, filtroMecanico, est);

        modelDias.setRowCount(0);
        modelDetalle.setRowCount(0);
        if (cal == null || cal.obtenerDias() == null) return;

        for (DiaCalendario d : cal.obtenerDias()) {
            int n = d.obtenerReservas().size();
            if (n > 0) {
                modelDias.addRow(new Object[]{
                        d.obtenerFecha(),
                        n,
                        d.obtenerTotalProgramadas(),
                        d.obtenerTotalEnProgreso(),
                        d.obtenerTotalFinalizadas(),
                        d.obtenerTotalEntregadas()
                });
            }
        }

        if (modelDias.getRowCount() > 0) {
            tablaDias.getSelectionModel().setSelectionInterval(0, 0);
            cargarDetalleSeleccion();
        }

        refrescarCalendario();
    }
    
    public void bloquearAFiltroMecanico(String nombreMecanico) {
    if (nombreMecanico == null || nombreMecanico.isBlank()) return;
    // Asume que 'mecanico' es tu JComboBox<String> de filtro
    this.mecanico.removeAllItems();
    this.mecanico.addItem(nombreMecanico);
    this.mecanico.setSelectedItem(nombreMecanico);
    this.mecanico.setEnabled(false); // bloquea el cambio
    this.calendarioMecanico.removeAllItems();
    this.calendarioMecanico.addItem(nombreMecanico);
    this.calendarioMecanico.setSelectedItem(nombreMecanico);
    this.calendarioMecanico.setEnabled(false);
    refrescar();
    refrescarCalendario();
}


    private void cargarDetalleSeleccion() {
        int i = tablaDias.getSelectedRow();
        modelDetalle.setRowCount(0);
        if (i < 0) return;
        int m = tablaDias.convertRowIndexToModel(i);
        Object fecha = modelDias.getValueAt(m, 0);
        LocalDate dia = fecha instanceof LocalDate ? (LocalDate) fecha : LocalDate.parse(fecha.toString());
        LocalDate ini = dia;
        LocalDate fin = dia;
        String mecSel = (String) mecanico.getSelectedItem();
        String filtroMecanico = (mecSel == null || mecSel.equalsIgnoreCase("Todos")) ? null : mecSel;
        ReservaEstado est = (ReservaEstado) estado.getSelectedItem();
        CalendarioReservas cal = servicioReserva.generarCalendario(ini, fin, filtroMecanico, est);
        if (cal == null || cal.obtenerDias() == null) return;
        List<DiaCalendario> dias = cal.obtenerDias();
        if (dias.isEmpty()) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Reserva r : dias.get(0).obtenerReservas()) {
            String fechaTxt = r.getFecha().format(fmt);
            String clienteTxt = servicioReserva.obtenerNombreCliente(r.getClienteId());
            String mecanicoTxt = r.getMecanicoAsignado();
            String servicioTxt = servicioReserva.obtenerNombreServicio(r.getServicioId());
            String estadoTxt = r.getEstado().name();
            String idTxt = r.getId();
            modelDetalle.addRow(new Object[]{ fechaTxt, clienteTxt, mecanicoTxt, servicioTxt, estadoTxt, idTxt });
        }
    }

    private void cerrarSeleccionada() {
        int row = tablaDetalle.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una reserva en el detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String reservaId = String.valueOf(modelDetalle.getValueAt(row, 5));
        Map<String,Integer> sugeridas;
        try {
            sugeridas = servicioReserva.obtenerPartesRequeridasPorReserva(reservaId);
        } catch (Exception ex) {
            sugeridas = Collections.emptyMap();
        }
        Window win = SwingUtilities.getWindowAncestor(this);
        CerrarReservaDialog dlg = new CerrarReservaDialog(win, servicioReserva, parteRepo, reservaId, sugeridas);
        dlg.setVisible(true);
        refrescar();
        refrescarCalendario();
    }

    private void rangoHoy() {
        LocalDate d = LocalDate.now();
        fechaInicio.setValue(toDate(d));
        fechaFin.setValue(toDate(d));
        asegurarOpcionesMecanicos();
        refrescar();
    }

    private void rangoMesActual() {
        LocalDate d = LocalDate.now();
        LocalDate i = d;
        LocalDate f = d.withDayOfMonth(d.lengthOfMonth());
        fechaInicio.setValue(toDate(i));
        fechaFin.setValue(toDate(f));
        asegurarOpcionesMecanicos();
        refrescar();
    }

    private static LocalDate toLocal(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static java.util.Date toDate(LocalDate d) {
        return java.util.Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
