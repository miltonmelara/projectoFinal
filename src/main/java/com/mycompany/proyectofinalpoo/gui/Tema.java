package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;

public final class Tema {
    public static final Color BARRA_LATERAL = new Color(0x111827);
    public static final Color ENCABEZADO = new Color(0x0B1220);
    public static final Color ACENTO = new Color(0x2563EB);
    public static final Color ACENTO_SUAVE = new Color(37,99,235,60);
    public static final Color TEXTO_CLARO = new Color(0xF9FAFB);

    public static void aplicar() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignored) {}
        Font f = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Label.font", f);
        UIManager.put("Button.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("TitledBorder.font", f.deriveFont(Font.BOLD, 13f));
    }
}
