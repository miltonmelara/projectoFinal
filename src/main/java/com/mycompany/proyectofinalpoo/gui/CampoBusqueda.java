package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;

public class CampoBusqueda extends JTextField {
    public CampoBusqueda() {
        setColumns(22);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        setForeground(new Color(20,24,31));
        setCaretColor(new Color(20,24,31));
        setEnabled(true);
        setEditable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255,255,255,210));
        g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
        super.paintComponent(g);
        g2.dispose();
    }
}
