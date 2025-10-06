package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;

public class BotonNavegacion extends JToggleButton {
    public BotonNavegacion(String texto) {
        super(texto);
        setHorizontalAlignment(LEFT);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        setForeground(Tema.TEXTO_CLARO);
        setOpaque(false);
        setRolloverEnabled(true);
        setPreferredSize(new Dimension(220, 46));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base = isSelected() ? Tema.ACENTO : Tema.ACENTO_SUAVE;
        if (getModel().isRollover() && !isSelected()) base = Tema.ACENTO_SUAVE;
        g2.setColor(base);
        g2.fillRoundRect(8, 6, getWidth()-16, getHeight()-12, 12, 12);
        super.paintComponent(g);
        g2.dispose();
    }
}
