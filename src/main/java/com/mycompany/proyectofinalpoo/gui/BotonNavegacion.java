package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonNavegacion extends JToggleButton {
    private Color colorNormal = new Color(30,36,46);
    private Color colorHover = new Color(44,52,64);
    private Color colorSeleccionado = new Color(64,73,87);
    private boolean sobre;

    public BotonNavegacion(String texto) {
        super(texto);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setForeground(new Color(238,240,243));
        setFont(getFont().deriveFont(Font.BOLD, 16f));
        setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { sobre = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { sobre = false; repaint(); }
        });
    }

    public void setColores(Color normal, Color hover, Color seleccionado) {
        if (normal != null) colorNormal = normal;
        if (hover != null) colorHover = hover;
        if (seleccionado != null) colorSeleccionado = seleccionado;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base = isSelected() ? colorSeleccionado : (sobre ? colorHover : colorNormal);
        g2.setColor(base);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        super.paintComponent(g2);
        g2.dispose();
    }
}
