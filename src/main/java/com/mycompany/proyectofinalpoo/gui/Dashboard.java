package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.nio.file.Path;

import com.mycompany.proyectofinalpoo.Usuario;
import com.mycompany.proyectofinalpoo.RolUsuario;
import com.mycompany.proyectofinalpoo.repo.ClienteRepo;
import com.mycompany.proyectofinalpoo.repo.ParteRepo;
import com.mycompany.proyectofinalpoo.repo.ReservaRepo;
import com.mycompany.proyectofinalpoo.repo.ServicioRepo;
import com.mycompany.proyectofinalpoo.repo.file.ClienteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ParteFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ReservaFileRepo;
import com.mycompany.proyectofinalpoo.repo.file.ServicioFileRepo;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioInventario;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva;
import com.mycompany.proyectofinalpoo.repo.servicios.ServicioCliente;
import com.mycompany.proyectofinalpoo.repo.servicios.SecurityContext;
import com.mycompany.proyectofinalpoo.util.MigracionIdsReservas;

public class Dashboard extends JFrame {
    // Servicios / repos
    private ServicioInventario servicioInventario;
    private ServicioReserva servicioReserva;
    private ServicioCliente servicioCliente;
    private ClienteRepo clienteRepo;
    private ServicioRepo servicioRepo;
    private ReservaRepo reservaRepo;
    private ParteRepo parteRepo;

    // Layout principal
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);

    // Vistas
    private VistaClientes vistaClientes;
    private VistaCalendario vistaCalendario;
    private JPanel vistaReservas;
    private JPanel vistaInventario;
    private JPanel vistaEstados;
    private JPanel vistaHistorial;
    private JPanel vistaReportes;

    // Estilo
    private final Color LATERAL_BG = TemaNeoBlue.SIDEBAR;
    private final Color BTN_NORMAL = TemaNeoBlue.navNormal();
    private final Color BTN_HOVER  = TemaNeoBlue.navHover();
    private final Color BTN_SEL    = TemaNeoBlue.navSel();

    // Botones de navegación
    private BotonNavegacion bCal, bCli, bRes, bInv, bEst, bHis, bRep;

    public Dashboard() {
        // Tema
        TemaNeoBlue.aplicar();
        aplicarTemaGlobal();

        // Servicios
        initReposServicios();
        try { MigracionIdsReservas.ejecutarEnCarpeta(Path.of("data")); } catch (Exception ignored) {}

        setTitle("Sistema de Taller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== ENCABEZADO =====
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Tema.ENCABEZADO);

        JLabel titulo = new JLabel("  Panel de Control");
        titulo.setForeground(Tema.TEXTO_CLARO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        // Centro con buscador (solo ADMIN)
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        centro.setOpaque(false);
        CampoBusqueda buscador = null;
        JButton btnBuscar = null;
        if (esAdmin()) {
            buscador = crearBuscador();
            btnBuscar = new JButton("Buscar");
            btnBuscar.setFocusPainted(false);
            btnBuscar.setForeground(Tema.TEXTO_CLARO);
            btnBuscar.setContentAreaFilled(false);
            btnBuscar.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
            centro.add(buscador);
            centro.add(btnBuscar);
        }

        // Botón Cerrar sesión
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setFocusPainted(false);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(60, 80, 130));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            UIManager.put("OptionPane.yesButtonText", "Sí");
            UIManager.put("OptionPane.noButtonText", "No");
            int r = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas cerrar sesión y volver al inicio?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (r == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new InicioSesion(Path.of("data")).setVisible(true));
            }
        });

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        derecha.setOpaque(false);
        derecha.add(btnLogout);

        encabezado.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(centro, BorderLayout.CENTER);
        encabezado.add(derecha, BorderLayout.EAST);

        // ===== BUSCADOR FUNCIONAL (solo ADMIN) =====
        if (esAdmin() && buscador != null) {
            CampoBusqueda finalBuscador = buscador;
            JButton finalBtnBuscar = btnBuscar;

            Timer debounce = new Timer(250, ev -> {
                String q = finalBuscador.getText().trim();
                tarjetas.show(contenido, "CLI");
                if (vistaClientes != null) vistaClientes.aplicarFiltro(q);
                seleccionar(bCli);
            });
            debounce.setRepeats(false);

            ((AbstractDocument) finalBuscador.getDocument()).addDocumentListener(new DocumentListener() {
                private void restart() { debounce.restart(); }
                @Override public void insertUpdate(DocumentEvent e) { restart(); }
                @Override public void removeUpdate(DocumentEvent e) { restart(); }
                @Override public void changedUpdate(DocumentEvent e) { restart(); }
            });

            if (finalBtnBuscar != null) {
                finalBtnBuscar.addActionListener(e -> {
                    debounce.stop();
                    String q = finalBuscador.getText().trim();
                    tarjetas.show(contenido, "CLI");
                    if (vistaClientes != null) vistaClientes.aplicarFiltro(q);
                    seleccionar(bCli);
                });
            }
        }

        // ===== PANEL LATERAL =====
        JPanel lateral = new JPanel();
        lateral.setBackground(LATERAL_BG);
        lateral.setLayout(new GridLayout(0,1,0,16));
        lateral.setBorder(BorderFactory.createEmptyBorder(16,12,16,12));

        bCal = nuevoBoton("Calendario");
        bCli = nuevoBoton("Clientes");
        bRes = nuevoBoton("Reservas");
        bInv = nuevoBoton("Inventario");
        bEst = nuevoBoton("Estados");
        bHis = nuevoBoton("Historial");
        bRep = nuevoBoton("Reportes");

        lateral.add(bCal);
        lateral.add(bCli);
        lateral.add(bRes);
        lateral.add(bInv);
        lateral.add(bEst);
        lateral.add(bHis);
        lateral.add(bRep);

        // ===== CONTENIDO =====
        vistaCalendario = new VistaCalendario(servicioReserva, parteRepo, servicioRepo);
        if (esAdmin()) {
            vistaClientes = new VistaClientes(servicioCliente, clienteRepo);
        }

        vistaReservas   = crearVistaReservaEmbebida();
        vistaInventario = crearVistaInventarioEmbebida();
        vistaEstados    = crearVistaEstadosEmbebida();
        vistaHistorial  = crearVistaHistorialEmbebida();
        vistaReportes   = crearVistaReportesEmbebida();

        contenido.add(vistaCalendario, "CAL");
        if (esAdmin() && vistaClientes != null) contenido.add(vistaClientes, "CLI");
        contenido.add(vistaReservas,   "RES");
        contenido.add(vistaInventario, "INV");
        contenido.add(vistaEstados,    "EST");
        contenido.add(vistaHistorial,  "HIS");
        contenido.add(vistaReportes,   "REP");

        // ===== NAVEGACIÓN POR ROL =====
        // Siempre visibles para ambos
        bCal.addActionListener(e -> { tarjetas.show(contenido, "CAL"); seleccionar(bCal); });
        bRes.addActionListener(e -> { tarjetas.show(contenido, "RES"); seleccionar(bRes); });
        bHis.addActionListener(e -> { tarjetas.show(contenido, "HIS"); seleccionar(bHis); });

        if (esAdmin()) {
            bCli.addActionListener(e -> { tarjetas.show(contenido, "CLI"); seleccionar(bCli); });
            bInv.addActionListener(e -> { tarjetas.show(contenido, "INV"); seleccionar(bInv); });
            bEst.addActionListener(e -> { tarjetas.show(contenido, "EST"); seleccionar(bEst); });
            bRep.addActionListener(e -> { tarjetas.show(contenido, "REP"); seleccionar(bRep); });

            // Asegurar visibilidad admin
            bCli.setVisible(true);
            bInv.setVisible(true);
            bEst.setVisible(true);
            bRep.setVisible(true);
        } else {
            // Ocultar módulos no permitidos al mecánico
            bCli.setVisible(false);
            bInv.setVisible(false);
            bEst.setVisible(false);
            bRep.setVisible(false);
        }

        add(encabezado, BorderLayout.NORTH);
        add(lateral, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        // Pantalla inicial
        tarjetas.show(contenido, "CAL");
        seleccionar(bCal);

        // Aplicar skin al árbol
        TemaNeoBlue.estilizar(this.getContentPane());
    }

    /* ===================== Helpers de rol ===================== */
    private boolean esAdmin() {
        Usuario u = SecurityContext.getCurrentUser();
        return u != null && u.getRol() == RolUsuario.ADMIN;
    }

    /* ===================== Buscador ===================== */
    private CampoBusqueda crearBuscador() {
        CampoBusqueda buscador = new CampoBusqueda();
        buscador.setOpaque(true);
        buscador.setBackground(TemaNeoBlue.SURFACE);
        buscador.setForeground(TemaNeoBlue.TXT);
        buscador.setCaretColor(TemaNeoBlue.TXT);
        buscador.setSelectionColor(new Color(58,105,198));
        buscador.setSelectedTextColor(Color.WHITE);
        buscador.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(120,150,220,120), 1, true),
            new javax.swing.border.EmptyBorder(8,12,8,12)
        ));
        buscador.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        try {
            java.lang.reflect.Method m = buscador.getClass().getMethod("setPromptForeground", Color.class);
            m.invoke(buscador, new Color(200, 210, 230, 160));
        } catch (Exception ignore) {}
        return buscador;
    }

    /* ===================== Botón lateral ===================== */
    private BotonNavegacion nuevoBoton(String texto) {
        BotonNavegacion b = new BotonNavegacion(texto);
        b.setColores(BTN_NORMAL, BTN_HOVER, BTN_SEL);
        return b;
    }

    /* ===================== Selección segura ===================== */
    private void seleccionar(AbstractButton seleccionado) {
        if (bCal != null) bCal.setSelected(seleccionado == bCal);
        if (bHis != null) bHis.setSelected(seleccionado == bHis);

        if (bCli != null && bCli.isVisible()) bCli.setSelected(seleccionado == bCli);
        if (bRes != null && bRes.isVisible()) bRes.setSelected(seleccionado == bRes);
        if (bInv != null && bInv.isVisible()) bInv.setSelected(seleccionado == bInv);
        if (bEst != null && bEst.isVisible()) bEst.setSelected(seleccionado == bEst);
        if (bRep != null && bRep.isVisible()) bRep.setSelected(seleccionado == bRep);

        if (bCal != null) bCal.repaint();
        if (bHis != null) bHis.repaint();
        if (bCli != null) bCli.repaint();
        if (bRes != null) bRes.repaint();
        if (bInv != null) bInv.repaint();
        if (bEst != null) bEst.repaint();
        if (bRep != null) bRep.repaint();
    }

    /* ===================== Vistas embebidas ===================== */
    private JPanel crearVistaReservaEmbebida() {
        FormularioReserva f = new FormularioReserva(servicioReserva, clienteRepo, servicioRepo, parteRepo, reservaRepo);
        f.setAlGuardar(() -> vistaCalendario.refrescarDesdeExterno());
        JPanel vista = new JPanel(new BorderLayout());
        vista.add(f.getContentPane(), BorderLayout.CENTER);
        return vista;
    }

    private JPanel crearVistaInventarioEmbebida() {
        FormularioInventario f = new FormularioInventario(servicioInventario, parteRepo);
        JPanel vista = new JPanel(new BorderLayout());
        vista.add(f.getContentPane(), BorderLayout.CENTER);
        return vista;
    }

    private JPanel crearVistaEstadosEmbebida() {
        FormularioEstadoReserva f = new FormularioEstadoReserva(servicioReserva, reservaRepo, clienteRepo, servicioRepo);
        JPanel vista = new JPanel(new BorderLayout());
        vista.add(f.getContentPane(), BorderLayout.CENTER);
        return vista;
    }

    private JPanel crearVistaHistorialEmbebida() {
        // Versión nueva que permite todo el historial y filtros
        FormularioHistorial f = new FormularioHistorial(servicioCliente, clienteRepo, servicioReserva, reservaRepo);
        JPanel vista = new JPanel(new BorderLayout());
        vista.add(f.getContentPane(), BorderLayout.CENTER);
        return vista;
    }

    private JPanel crearVistaReportesEmbebida() {
        VentanaReporteConsumo f = new VentanaReporteConsumo();
        JPanel vista = new JPanel(new BorderLayout());
        vista.add(f.getContentPane(), BorderLayout.CENTER);
        return vista;
    }

    /* ===================== Repos/Servicios ===================== */
    private void initReposServicios() {
        Path dataDir = Path.of("data");
        clienteRepo = new ClienteFileRepo(dataDir);
        parteRepo   = new ParteFileRepo(dataDir);
        servicioRepo= new ServicioFileRepo(dataDir);
        reservaRepo = new ReservaFileRepo(dataDir);
        servicioInventario = new ServicioInventario(parteRepo);
        servicioCliente    = new ServicioCliente(clienteRepo, reservaRepo, servicioRepo);
        var consumoRepo = new com.mycompany.proyectofinalpoo.repo.file.ConsumoParteFileRepo(dataDir);
        var usuarioRepo = new com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo(dataDir);
        servicioReserva   = new ServicioReserva(reservaRepo, servicioRepo, parteRepo, clienteRepo, usuarioRepo, consumoRepo);
    }

    /* ===================== UIManager: bordes de TitledBorder ===================== */
    private void aplicarTemaGlobal() {
        UIManager.put("TitledBorder.titleColor", new Color(230, 236, 245));
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(new Color(100, 120, 160)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InicioSesion(Path.of("data")).setVisible(true));
    }
}
