package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public final class EstiloCombos {
    private EstiloCombos() {}

    public static void aplicar(JComboBox<?> combo, Color fondo, Color texto, Color selBg, Color selFg) {
        if (combo == null) return;

        combo.setForeground(texto);
        combo.setBackground(fondo);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(selBg);
                    setForeground(selFg);
                } else {
                    setBackground(fondo);
                    setForeground(texto);
                }
                return comp;
            }
        });

        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new FlechaBoton(fondo, texto);
            }
        });
    }

    public static void aplicarDarkAzul(JComboBox<?> combo) {
        Color fondo = new Color(24, 28, 34);      // azul oscuro
        Color texto = new Color(230, 236, 245);   // texto claro
        Color selBg = new Color(70, 100, 150);    // azul de selección
        Color selFg = Color.WHITE;
        aplicar(combo, fondo, texto, selBg, selFg);
    }

    // Botón con flecha blanca real (dibujada)
    private static class FlechaBoton extends JButton {
        private final Color fondo;
        private final Color texto;

        public FlechaBoton(Color fondo, Color texto) {
            this.fondo = fondo;
            this.texto = texto;
            setBorder(BorderFactory.createEmptyBorder());
            setFocusable(false);
            setContentAreaFilled(false);
            setOpaque(true);
            setBackground(fondo);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h) / 3;
            int x = w / 2;
            int y = h / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(texto);

            // Dibuja la flecha ▼
            int[] xPoints = {x - size, x, x + size};
            int[] yPoints = {y - size / 2, y + size / 2, y - size / 2};
            g2.fillPolygon(xPoints, yPoints, 3);

            g2.dispose();
        }
    }
}
