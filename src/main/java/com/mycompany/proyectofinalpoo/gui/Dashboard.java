package com.mycompany.proyectofinalpoo.gui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
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
import com.mycompany.proyectofinalpoo.util.MigracionIdsReservas;

public class Dashboard extends JFrame {
    private ServicioInventario servicioInventario;
    private ServicioReserva servicioReserva;
    private ServicioCliente servicioCliente;
    private ClienteRepo clienteRepo;
    private ServicioRepo servicioRepo;
    private ReservaRepo reservaRepo;
    private ParteRepo parteRepo;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private VistaClientes vistaClientes;
    private VistaCalendario vistaCalendario;
    private javax.swing.JButton bRep;

    public Dashboard() {
        Tema.aplicar();
        initReposServicios();

        // ✅ Renumera automáticamente los IDs de reservas antiguas
        try {
            MigracionIdsReservas.ejecutarEnCarpeta(java.nio.file.Path.of("data"));
            System.out.println("✅ IDs de reservas renumerados automáticamente al iniciar el sistema.");
        } catch (Exception ex) {
            System.err.println("⚠️ Error al intentar renumerar IDs: " + ex.getMessage());
        }

        setTitle("Sistema de Taller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Tema.ENCABEZADO);

        JLabel titulo = new JLabel("  Panel de Control");
        titulo.setForeground(Tema.TEXTO_CLARO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JPanel derecha = new JPanel(new BorderLayout(8,8));
        derecha.setOpaque(false);
        CampoBusqueda buscador = new CampoBusqueda();
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setFocusPainted(false);
        btnBuscar.setForeground(Tema.TEXTO_CLARO);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        derecha.add(buscador, BorderLayout.CENTER);
        derecha.add(btnBuscar, BorderLayout.EAST);
        btnBuscar.addActionListener(e -> {
            String q = buscador.getText().trim();
            tarjetas.show(contenido, "CLI");
            vistaClientes.aplicarFiltro(q);
        });

        buscador.addActionListener(e -> {
            String q = buscador.getText().trim();
            tarjetas.show(contenido, "CLI");
            vistaClientes.aplicarFiltro(q);
        });

        encabezado.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);

        JPanel lateral = new JPanel();
        lateral.setBackground(Tema.BARRA_LATERAL);
        lateral.setLayout(new GridLayout(0,1,0,8));
        lateral.setBorder(BorderFactory.createEmptyBorder(16,12,16,12));

        ButtonGroup grupo = new ButtonGroup();
        BotonNavegacion bCal = new BotonNavegacion("Calendario");
        BotonNavegacion bCli = new BotonNavegacion("Clientes");
        JButton bRes = botonLateral("Reservas");
        JButton bInv = botonLateral("Inventario");
        JButton bEst = botonLateral("Estados");
        JButton bHis = botonLateral("Historial");
        bRep = botonLateral("Reportes");

        bRes.setFont(bRes.getFont().deriveFont(Font.BOLD, 16f));
        bInv.setFont(bInv.getFont().deriveFont(Font.BOLD, 16f));
        bEst.setFont(bEst.getFont().deriveFont(Font.BOLD, 16f));
        bHis.setFont(bHis.getFont().deriveFont(Font.BOLD, 16f));
        bRep.setFont(bRep.getFont().deriveFont(Font.BOLD, 16f));

        bRep.addActionListener(e -> new VentanaReporteConsumo().setVisible(true));

        grupo.add(bCal);
        grupo.add(bCli);
        lateral.add(bCal);
        lateral.add(bCli);
        lateral.add(bRes);
        lateral.add(bInv);
        lateral.add(bEst);
        lateral.add(bHis);
        lateral.add(bRep);

        vistaCalendario = new VistaCalendario(servicioReserva, parteRepo, servicioRepo);
        vistaClientes = new VistaClientes(servicioCliente, clienteRepo);
        contenido.add(vistaCalendario, "CAL");
        contenido.add(vistaClientes, "CLI");

        bCal.addActionListener(e -> tarjetas.show(contenido, "CAL"));
        bCli.addActionListener(e -> tarjetas.show(contenido, "CLI"));
        bRes.addActionListener(e -> {
            FormularioReserva f = new FormularioReserva(servicioReserva, clienteRepo, servicioRepo, parteRepo, reservaRepo);
            f.setAlGuardar(() -> vistaCalendario.refrescarDesdeExterno());
            f.setVisible(true);
        });

        bInv.addActionListener(e -> new FormularioInventario(servicioInventario, parteRepo).setVisible(true));
        bEst.addActionListener(e -> new FormularioEstadoReserva(servicioReserva, reservaRepo, clienteRepo, servicioRepo).setVisible(true));
        bHis.addActionListener(e -> new FormularioHistorial(servicioCliente, clienteRepo).setVisible(true));

        add(encabezado, BorderLayout.NORTH);
        add(lateral, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        bCal.setSelected(true);
        tarjetas.show(contenido, "CAL");
    }

    private JButton botonLateral(String t) {
        JButton b = new JButton(t);
        b.setFocusPainted(false);
        b.setForeground(Tema.TEXTO_CLARO);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        return b;
    }

    private void initReposServicios() {
        Path dataDir = Path.of("data");
        clienteRepo = new ClienteFileRepo(dataDir);
        parteRepo = new ParteFileRepo(dataDir);
        servicioRepo = new ServicioFileRepo(dataDir);
        reservaRepo = new ReservaFileRepo(dataDir);
        servicioInventario = new ServicioInventario(parteRepo);
        servicioCliente = new ServicioCliente(clienteRepo, reservaRepo, servicioRepo);
        var consumoRepo = new com.mycompany.proyectofinalpoo.repo.file.ConsumoParteFileRepo(Path.of("data"));
        var usuarioRepo = new com.mycompany.proyectofinalpoo.repo.file.UsuarioFileRepo(Path.of("data"));
        servicioReserva = new com.mycompany.proyectofinalpoo.repo.servicios.ServicioReserva(
            reservaRepo,
            servicioRepo,
            parteRepo,
            clienteRepo,
            usuarioRepo,
            consumoRepo
        );
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            java.nio.file.Path carpetaDatos = java.nio.file.Path.of("data");
            new InicioSesion(carpetaDatos).setVisible(true);
        });
    }
}
