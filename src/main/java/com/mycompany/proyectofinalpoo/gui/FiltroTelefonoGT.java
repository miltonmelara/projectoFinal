package com.mycompany.proyectofinalpoo.gui;

import javax.swing.text.*;
import java.util.regex.Pattern;

public class FiltroTelefonoGT extends DocumentFilter {
    private static final Pattern REGEX_VALIDO = Pattern.compile("^\\d{4} \\d{4}$");

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;
        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = formatear(limpiar(aplicar(actual, offset, 0, string)));
        if (nuevo.length() <= 9) {
            fb.replace(0, actual.length(), nuevo, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = formatear(limpiar(aplicar(actual, offset, length, text == null ? "" : text)));
        if (nuevo.length() <= 9) {
            fb.replace(0, actual.length(), nuevo, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = formatear(limpiar(aplicar(actual, offset, length, "")));
        fb.replace(0, actual.length(), nuevo, null);
    }

    private static String aplicar(String base, int offset, int length, String txt) {
        StringBuilder sb = new StringBuilder(base);
        sb.replace(offset, offset + length, txt);
        return sb.toString();
    }

    private static String limpiar(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static String formatear(String digitos) {
        if (digitos.length() == 0) return "";
        if (digitos.length() <= 4) return digitos;
        if (digitos.length() > 8) digitos = digitos.substring(0, 8);
        return digitos.substring(0, 4) + " " + digitos.substring(4);
    }

    public static boolean esValido(String texto) {
        return REGEX_VALIDO.matcher(texto == null ? "" : texto).matches();
    }

    public static String soloDigitos(String texto) {
        return limpiar(texto);
    }

    public static String normalizado(String texto) {
        return formatear(limpiar(texto));
    }
}
