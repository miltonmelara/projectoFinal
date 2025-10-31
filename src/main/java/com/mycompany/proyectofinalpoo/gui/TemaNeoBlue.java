package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.Arrays;

public final class TemaNeoBlue {
    // Paleta (azules fríos como el mockup)
    public static final Color BG        = new Color(15, 29, 46);   // fondo
    public static final Color BG_ALT    = new Color(22, 38, 64);   // panels
    public static final Color SURFACE   = new Color(28, 47, 78);   // botones/inputs
    public static final Color SURFACE2  = new Color(34, 57, 94);   // tarjetas
    public static final Color SIDEBAR   = new Color(13, 24, 39);   // barra lateral
    public static final Color ACCENT    = new Color(77, 163, 255); // azul acento
    public static final Color ACCENT_SOFT = new Color(92, 117, 255);
    public static final Color TXT       = new Color(228, 234, 246);
    public static final Color TXT_DIM   = new Color(172, 184, 205);
    public static final Color GRID      = new Color(43, 63, 98);
    public static final Font  FONT      = new Font("Segoe UI", Font.PLAIN, 14);

    public static void aplicar() {
        UIManager.put("control", BG);
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TXT);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", TXT);

        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", TXT);
        UIManager.put("ToggleButton.background", SURFACE);
        UIManager.put("ToggleButton.foreground", TXT);

        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TXT);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.foreground", TXT);
        UIManager.put("FormattedTextField.background", SURFACE);
        UIManager.put("FormattedTextField.foreground", TXT);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", TXT);

        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TXT);
        UIManager.put("ComboBox.selectionBackground", ACCENT);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        UIManager.put("Spinner.background", SURFACE);
        UIManager.put("Spinner.foreground", TXT);

        UIManager.put("Table.background", BG);
        UIManager.put("Table.foreground", TXT);
        UIManager.put("Table.gridColor", GRID);
        UIManager.put("Table.selectionBackground", new Color(58, 105, 198));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", BG_ALT);
        UIManager.put("TableHeader.foreground", TXT_DIM);

        UIManager.put("ScrollPane.background", BG);
        UIManager.put("ToolTip.background", SURFACE);
        UIManager.put("ToolTip.foreground", TXT);

        UIManager.put("defaultFont", FONT);
        UIManager.getDefaults().keySet().stream()
            .filter(k -> k != null && k.toString().toLowerCase().contains("font"))
            .forEach(k -> UIManager.put(k, FONT));
    }

    // Tarjeta con look "neo blue" (gradiente y sombra suave)
    public static class CardPanel extends JPanel {
        public CardPanel() { setOpaque(false); setBorder(new EmptyBorder(12,12,12,12)); }
        @Override protected void paintComponent(Graphics g) {
            int r = 16;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // sombra suave
            g2.setColor(new Color(0,0,0,70));
            g2.fillRoundRect(6, 8, getWidth()-12, getHeight()-10, r+8, r+8);
            // gradiente azulado
            GradientPaint gp = new GradientPaint(0,0, SURFACE2, 0, getHeight(), BG_ALT);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth()-6, getHeight()-8, r, r);
            // borde leve
            g2.setColor(new Color(120,160,255,60));
            g2.drawRoundRect(0, 0, getWidth()-6, getHeight()-8, r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Aplica bordes redondeados y colores a toda la jerarquía
    public static void estilizar(Container root) {
        if (root == null) return;
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel p) {
                if (!(p instanceof CardPanel)) {
                    p.setOpaque(true);
                    p.setBackground(BG);
                }
            }
            if (c instanceof JScrollPane sp) {
                sp.getViewport().setBackground(BG);
                sp.setBorder(BorderFactory.createEmptyBorder());
                JScrollBar v = sp.getVerticalScrollBar(), h = sp.getHorizontalScrollBar();
                for (JScrollBar sb : Arrays.asList(v,h)) if (sb != null) {
                    sb.setUI(new BasicScrollBarUI(){
                        @Override protected void configureScrollBarColors(){ this.thumbColor = SURFACE; this.trackColor = BG; }
                        @Override protected JButton createDecreaseButton(int o){ return btn(); }
                        @Override protected JButton createIncreaseButton(int o){ return btn(); }
                        private JButton btn(){ JButton b=new JButton(); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); return b; }
                    });
                }
            }
            if (c instanceof JList<?> list) {
                list.setBackground(BG);
                list.setForeground(TXT);
                list.setSelectionBackground(new Color(58, 105, 198));
                list.setSelectionForeground(Color.WHITE);
                list.setBorder(BorderFactory.createLineBorder(new Color(100, 140, 220, 80), 1, true));
            }
            if (c instanceof JFileChooser chooser) {
                chooser.setBackground(BG);
                chooser.setForeground(TXT);
                chooser.setOpaque(true);
            }
            if (c instanceof JTable t) {
                t.setShowGrid(true);
                t.setGridColor(GRID);
                t.setRowHeight(24);
                if (t.getTableHeader()!=null){
                    t.getTableHeader().setBackground(BG_ALT);
                    t.getTableHeader().setForeground(TXT_DIM);
                    t.getTableHeader().setFont(FONT.deriveFont(Font.BOLD));
                }
            }
            if (c instanceof JButton b) {
                b.setBackground(SURFACE);
                b.setForeground(TXT);
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new Color(100,140,220,80), 1, true),
                        new EmptyBorder(8,14,8,14)
                ));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            if (c instanceof JTextField tf) {
                tf.setBackground(SURFACE);
                tf.setForeground(TXT);
                tf.setCaretColor(TXT);
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(new Color(120,150,220,80), 1, true),
                        new EmptyBorder(8,10,8,10)
                ));
            }
            if (c instanceof JComboBox<?> cb) {
                cb.setBackground(SURFACE);
                cb.setForeground(TXT);
                cb.setBorder(new javax.swing.border.LineBorder(new Color(120,150,220,80), 1, true));
            }
            if (c instanceof JSpinner sp) {
                sp.setBorder(new javax.swing.border.LineBorder(new Color(120,150,220,80), 1, true));
            }
            if (c instanceof JComponent jc) jc.setFont(FONT);
            if (c instanceof Container cont) estilizar(cont);
        }
    }

    // Colores de botones de navegación laterales
    public static Color navNormal(){ return new Color(30, 44, 70); }
    public static Color navHover(){  return new Color(40, 60, 95); }
    public static Color navSel(){    return new Color(52, 86, 140); }
}
