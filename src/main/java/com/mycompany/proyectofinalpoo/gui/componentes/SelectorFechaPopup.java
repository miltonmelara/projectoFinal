package com.mycompany.proyectofinalpoo.gui.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mycompany.proyectofinalpoo.gui.TemaNeoBlue;

/**
 * Añade un selector de calendario emergente a un {@link JSpinner} con {@link java.util.Date}.
 */
public final class SelectorFechaPopup {
    private static final DateTimeFormatter MES_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter DIA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private SelectorFechaPopup() {}

    /**
     * Envuelve el spinner con un botón adicional que abre un mini calendario.
     * El panel devuelto debe añadirse al layout en lugar del spinner original.
     */
    public static JComponent adjuntar(JSpinner spinner) {
        if (!(spinner.getEditor() instanceof JSpinner.DateEditor)) {
            spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        }

        JPanel wrapper = new JPanel(new BorderLayout(6, 0));
        wrapper.setOpaque(false);
        wrapper.add(spinner, BorderLayout.CENTER);

        JButton boton = new JButton("Cal");
        boton.setFocusPainted(false);
        boton.setMargin(new Insets(4, 10, 4, 10));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.setBackground(TemaNeoBlue.ACCENT);
        boton.setForeground(Color.WHITE);
        boton.setBorder(BorderFactory.createLineBorder(new Color(120, 150, 220, 120), 1, true));

        DatePopup popup = new DatePopup(spinner);
        boton.addActionListener(e -> {
            if (popup.isVisible()) {
                popup.setVisible(false);
                return;
            }
            popup.actualizarDesdeSpinner();
            Point p = boton.getLocationOnScreen();
            popup.show(boton, 0, boton.getHeight());
        });

        wrapper.add(boton, BorderLayout.EAST);
        return wrapper;
    }

    private static class DatePopup extends JPopupMenu {
        private final JSpinner spinner;
        private YearMonth mesActual;
        private LocalDate seleccionado;
        private final JLabel lblMes = new JLabel("", SwingConstants.CENTER);
        private final JPanel grid = new JPanel(new GridLayout(6,7,4,4));

        DatePopup(JSpinner spinner) {
            this.spinner = spinner;
            setFocusable(false);
            setBorder(BorderFactory.createLineBorder(new Color(80, 100, 150), 1));
            JPanel cont = new JPanel(new BorderLayout(6,6));
            cont.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            cont.setBackground(new Color(20,32,48));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JButton btnPrev = crearBotonNavegacion("◀");
            JButton btnNext = crearBotonNavegacion("▶");
            header.add(btnPrev, BorderLayout.WEST);
            header.add(lblMes, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);
            lblMes.setForeground(TemaNeoBlue.TXT);
            lblMes.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 14f));

            JPanel encabezado = new JPanel(new GridLayout(1,7));
            encabezado.setOpaque(false);
            for (int i = 1; i <= 7; i++) {
                DayOfWeek dow = DayOfWeek.of(i);
                JLabel lbl = new JLabel(dow.getDisplayName(TextStyle.SHORT, new Locale("es","ES")).toUpperCase(), SwingConstants.CENTER);
                lbl.setForeground(TemaNeoBlue.TXT_DIM);
                lbl.setFont(TemaNeoBlue.FONT.deriveFont(Font.BOLD, 11f));
                encabezado.add(lbl);
            }

            grid.setOpaque(false);

            cont.add(header, BorderLayout.NORTH);
            cont.add(encabezado, BorderLayout.CENTER);
            cont.add(grid, BorderLayout.SOUTH);
            add(cont);

            btnPrev.addActionListener(e -> { mesActual = mesActual.minusMonths(1); refrescar(); });
            btnNext.addActionListener(e -> { mesActual = mesActual.plusMonths(1); refrescar(); });

            spinner.addChangeListener(new ChangeListener() {
                @Override public void stateChanged(ChangeEvent e) {
                    if (isVisible()) actualizarDesdeSpinner();
                }
            });
        }

        private JButton crearBotonNavegacion(String txt) {
            JButton b = new JButton(txt);
            b.setFocusPainted(false);
            b.setMargin(new Insets(2,6,2,6));
            b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            b.setBackground(new Color(40,60,95));
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createLineBorder(new Color(110,140,200,140),1,true));
            return b;
        }

        private LocalDate minimoPermitido;

        void actualizarDesdeSpinner() {
            Object value = spinner.getValue();
            LocalDate fecha = value instanceof java.util.Date
                    ? Instant.ofEpochMilli(((java.util.Date) value).getTime()).atZone(ZoneId.systemDefault()).toLocalDate()
                    : LocalDate.now();
            actualizarMinimo();
            if (minimoPermitido != null && fecha.isBefore(minimoPermitido)) {
                fecha = minimoPermitido;
                spinner.setValue(java.util.Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            this.seleccionado = fecha;
            this.mesActual = YearMonth.from(fecha);
            refrescar();
        }

        private void actualizarMinimo() {
            minimoPermitido = null;
            if (spinner.getModel() instanceof javax.swing.SpinnerDateModel model) {
                Object start = model.getStart();
                if (start instanceof java.util.Date) {
                    minimoPermitido = ((java.util.Date) start).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }
            }
        }

        private void refrescar() {
            if (mesActual == null) mesActual = YearMonth.now();
            lblMes.setText(capitalizar(MES_FMT.format(mesActual)));
            grid.removeAll();

            LocalDate inicio = mesActual.atDay(1);
            int offset = inicio.getDayOfWeek().getValue() - 1;
            LocalDate cursor = inicio.minusDays(offset);

            for (int i = 0; i < 42; i++) {
                final LocalDate dia = cursor.plusDays(i);
                JButton botonDia = crearBotonDia(dia);
                grid.add(botonDia);
            }

            grid.revalidate();
            grid.repaint();
        }

        private JButton crearBotonDia(LocalDate dia) {
            JButton b = new JButton(String.valueOf(dia.getDayOfMonth()));
            b.setFocusPainted(false);
            b.setMargin(new Insets(4,4,4,4));
            b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createLineBorder(new Color(55,75,110,180), 1));

            boolean esMes = dia.getMonth().equals(mesActual.getMonth());
            boolean esHoy = dia.equals(LocalDate.now());
            boolean esSeleccionado = dia.equals(seleccionado);
            boolean permitido = minimoPermitido == null || !dia.isBefore(minimoPermitido);

            Color fondo = esMes ? new Color(34, 52, 82, 220) : new Color(24, 34, 56, 200);
            Color texto = TemaNeoBlue.TXT;
            if (esSeleccionado) {
                fondo = TemaNeoBlue.ACCENT;
                texto = Color.WHITE;
                b.setBorder(BorderFactory.createLineBorder(new Color(200,230,255,220), 2));
            } else if (esHoy) {
                fondo = new Color(70, 100, 160, 230);
                texto = Color.WHITE;
                b.setBorder(BorderFactory.createLineBorder(new Color(130,170,240,220), 2));
            } else if (!permitido) {
                fondo = new Color(20, 28, 44, 180);
                texto = TemaNeoBlue.TXT_DIM;
                b.setBorder(BorderFactory.createDashedBorder(new Color(80,100,140,150)));
            }

            b.setBackground(fondo);
            b.setForeground(texto);

            if (!permitido) {
                b.setEnabled(false);
                return b;
            }

            b.addActionListener(e -> {
                seleccionado = dia;
                java.util.Date nueva = java.util.Date.from(dia.atStartOfDay(ZoneId.systemDefault()).toInstant());
                spinner.setValue(nueva);
                setVisible(false);
            });

            return b;
        }

        private String capitalizar(String texto) {
            if (texto == null || texto.isBlank()) return texto;
            texto = texto.toLowerCase(new Locale("es","ES"));
            return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
        }
    }
}
