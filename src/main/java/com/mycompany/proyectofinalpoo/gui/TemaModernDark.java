package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.Arrays;

public final class TemaModernDark {
    public static final Color BG        = new Color(28, 31, 38);
    public static final Color BG_ALT    = new Color(36, 40, 49);
    public static final Color SURFACE   = new Color(44, 50, 61);
    public static final Color ACCENT    = new Color(85, 143, 255);
    public static final Color TXT       = new Color(234, 237, 241);
    public static final Color TXT_MID   = new Color(193, 199, 208);
    public static final Color TXT_DIM   = new Color(150, 157, 168);
    public static final Color LINE      = new Color(60, 66, 78);
    public static final Font  BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static void aplicar() {
        UIManager.put("control", BG);
        UIManager.put("info", BG);
        UIManager.put("nimbusBase", BG);
        UIManager.put("text", TXT);
        UIManager.put("nimbusBlueGrey", BG);
        UIManager.put("nimbusLightBackground", BG);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", TXT);
        UIManager.put("Label.foreground", TXT);
        UIManager.put("Separator.foreground", LINE);
        UIManager.put("TitledBorder.titleColor", TXT_MID);

        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", TXT);
        UIManager.put("Button.focus", LINE);
        UIManager.put("ToggleButton.background", SURFACE);
        UIManager.put("ToggleButton.foreground", TXT);

        UIManager.put("TextField.background", BG_ALT);
        UIManager.put("TextField.foreground", TXT);
        UIManager.put("TextField.caretForeground", TXT);
        UIManager.put("PasswordField.background", BG_ALT);
        UIManager.put("PasswordField.foreground", TXT);
        UIManager.put("FormattedTextField.background", BG_ALT);
        UIManager.put("FormattedTextField.foreground", TXT);
        UIManager.put("TextArea.background", BG_ALT);
        UIManager.put("TextArea.foreground", TXT);

        UIManager.put("ComboBox.background", BG_ALT);
        UIManager.put("ComboBox.foreground", TXT);
        UIManager.put("ComboBox.selectionBackground", SURFACE);
        UIManager.put("ComboBox.selectionForeground", TXT);

        UIManager.put("Spinner.background", BG_ALT);
        UIManager.put("Spinner.foreground", TXT);

        UIManager.put("Table.background", BG);
        UIManager.put("Table.foreground", TXT);
        UIManager.put("Table.gridColor", LINE);
        UIManager.put("Table.selectionBackground", new Color(56, 98, 194));
        UIManager.put("Table.selectionForeground", TXT);
        UIManager.put("TableHeader.background", BG_ALT);
        UIManager.put("TableHeader.foreground", TXT_MID);

        UIManager.put("ScrollPane.background", BG);
        UIManager.put("ScrollBar.thumb", SURFACE);
        UIManager.put("ScrollBar.track", BG);

        UIManager.put("TabbedPane.contentBorderInsets", new Insets(8,8,8,8));

        UIManager.put("ToolTip.background", SURFACE);
        UIManager.put("ToolTip.foreground", TXT);

        UIManager.put("defaultFont", BASE_FONT);

        UIManager.getDefaults().keySet().stream()
            .filter(k -> k != null && k.toString().toLowerCase().contains("font"))
            .forEach(k -> UIManager.put(k, BASE_FONT));
    }

    public static void estilizarRaiz(Container root) {
        if (root == null) return;
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel p) {
                p.setOpaque(true);
                if (p.getBorder() == null) p.setBorder(new EmptyBorder(8,8,8,8));
                p.setBackground(BG);
            }
            if (c instanceof JScrollPane sp) {
                sp.getViewport().setBackground(BG);
                sp.setBorder(BorderFactory.createLineBorder(LINE));
                JScrollBar v = sp.getVerticalScrollBar();
                JScrollBar h = sp.getHorizontalScrollBar();
                for (JScrollBar sb : Arrays.asList(v,h)) {
                    if (sb == null) continue;
                    sb.setUI(new BasicScrollBarUI(){
                        @Override protected void configureScrollBarColors(){ this.thumbColor = SURFACE; this.trackColor = BG; }
                        @Override protected JButton createDecreaseButton(int o){ return botonSB(); }
                        @Override protected JButton createIncreaseButton(int o){ return botonSB(); }
                        private JButton botonSB(){ JButton b = new JButton(); b.setOpaque(false); b.setBorderPainted(false); b.setContentAreaFilled(false); return b; }
                    });
                }
            }
            if (c instanceof JTable t) {
                t.setShowGrid(true);
                t.setGridColor(LINE);
                t.setRowHeight(24);
                t.setBackground(BG);
                t.setForeground(TXT);
                t.setSelectionBackground(new Color(56, 98, 194));
                t.setSelectionForeground(TXT);
                if (t.getTableHeader()!=null){
                    t.getTableHeader().setBackground(BG_ALT);
                    t.getTableHeader().setForeground(TXT_MID);
                    t.getTableHeader().setFont(BASE_FONT.deriveFont(Font.BOLD));
                }
            }
            if (c instanceof JButton b) {
                b.setBackground(SURFACE);
                b.setForeground(TXT);
                b.setBorder(new Redondeado(10, LINE));
                b.setFocusPainted(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            if (c instanceof JToggleButton b) {
                b.setBackground(SURFACE);
                b.setForeground(TXT);
                b.setBorder(new Redondeado(10, LINE));
                b.setFocusPainted(false);
            }
            if (c instanceof JTextField tf) {
                tf.setBackground(BG_ALT);
                tf.setForeground(TXT);
                tf.setCaretColor(TXT);
                tf.setBorder(BorderFactory.createCompoundBorder(new Redondeado(8, LINE), new EmptyBorder(8,10,8,10)));
            }
            if (c instanceof JComboBox<?> cb) {
                cb.setBackground(BG_ALT);
                cb.setForeground(TXT);
                cb.setBorder(new Redondeado(8, LINE));
            }
            if (c instanceof JSpinner sp) {
                sp.setBorder(new Redondeado(8, LINE));
                sp.getEditor().setBackground(BG_ALT);
                sp.getEditor().setForeground(TXT);
            }
            if (c instanceof JSplitPane sp) {
                sp.setBorder(new EmptyBorder(0,0,0,0));
            }
            if (c instanceof JTabbedPane tp) {
                tp.setBackground(BG);
                tp.setForeground(TXT_MID);
                tp.setBorder(new EmptyBorder(4,4,4,4));
            }
            if (c instanceof JComponent jc) {
                jc.setFont(BASE_FONT);
            }
            if (c instanceof Container cont) estilizarRaiz(cont);
        }
    }

    public static class Redondeado extends javax.swing.border.LineBorder {
        private final int radius;
        public Redondeado(int radius, Color color){ super(color, 1, true); this.radius = radius; }
        @Override public Insets getBorderInsets(Component c){ return new Insets(8,10,8,10); }
        @Override public boolean isBorderOpaque(){ return false; }
    }
}
