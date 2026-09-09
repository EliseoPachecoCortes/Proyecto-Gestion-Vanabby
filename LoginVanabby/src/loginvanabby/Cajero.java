package loginvanabby;

import conexion.ConexionBD;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class Cajero extends javax.swing.JFrame {

    private Connection cn;
    private String nombreUsuarioActual;

    private JTable tablaVentas, tablaDetalle;
    private DefaultTableModel modVentas, modDetalle;
    private JComboBox<String> cboBuscar;
    private JTextField txtBuscar;
    private JLabel lblGranTotal, lblClienteInfo;
    private JButton btnRefrescar, btnEliminar, btnPagar, btnAnticipos, btnBuscar,
            btnVolverCaja, btnCerrarSesion, btnCorteCaja;

    public Cajero() throws SQLException {
        this("Cajero(a) General");
    }

    public Cajero(String nombreUsuario) throws SQLException {
        this.cn = ConexionBD.getConexion();
        this.nombreUsuarioActual = nombreUsuario;

        crearInterfazGrafica();
        configurarEventos();
        mostrarVentas(0, null);
    }

    private void configurarEventos() {
        tablaVentas.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                int filaVisual = tablaVentas.getSelectedRow();
                if (filaVisual >= 0) {
                    int filaModelo = tablaVentas.convertRowIndexToModel(filaVisual);

                    int ventaId = Integer.parseInt(modVentas.getValueAt(filaModelo, 0).toString());
                    String fecha = modVentas.getValueAt(filaModelo, 1).toString();
                    String cliente = modVentas.getValueAt(filaModelo, 3).toString();
                    String estado = modVentas.getValueAt(filaModelo, 5).toString();
                    String total = modVentas.getValueAt(filaModelo, 6).toString();

                    lblClienteInfo.setText("Pedido de: " + cliente + " | Fecha: " + fecha + " | Estado: " + estado);
                    lblGranTotal.setText(total);

                    if (estado.equalsIgnoreCase("Pagado")) {
                        lblGranTotal.setForeground(new Color(0, 153, 51));
                    } else {
                        lblGranTotal.setForeground(new Color(204, 0, 0));
                    }

                    mostrarDetalle(ventaId);
                }
            }
        });
    }

    // =========================================================================
    // LÓGICA: MOSTRAR VENTAS
    // =========================================================================
    public void mostrarVentas(int opcionBuscar, String valor) {
        modVentas.setRowCount(0);
        new Thread(() -> {
            String sql = "SELECT v.venta_id, "
                    + "v.fecha_venta, "
                    + "CONCAT_WS(' ', e.nombre, e.ap_paterno, e.ap_materno) AS nombre_empleado, "
                    + "CONCAT_WS(' ', c.nombre, c.ap_paterno, c.ap_materno) AS nombre_cliente, "
                    + "(SELECT GROUP_CONCAT(CONCAT(dv.cantidad, 'x ', p.nombre) SEPARATOR ', ') "
                    + " FROM detalle_venta dv JOIN producto p ON dv.producto_id = p.producto_id "
                    + " WHERE dv.venta_id = v.venta_id) AS articulos, "
                    + "ev.nombre_estado AS estado_venta, "
                    + "v.total "
                    + "FROM venta v "
                    + "LEFT JOIN cliente c ON v.cliente_id = c.cliente_id "
                    + "LEFT JOIN empleado e ON v.empleado_id = e.empleado_id "
                    + "LEFT JOIN estado_venta ev ON v.estado_venta_id = ev.estado_venta_id";

            if (valor != null && !valor.trim().isEmpty()) {
                switch (opcionBuscar) {
                    case 1 ->
                        sql += " WHERE CONCAT_WS(' ', c.nombre, c.ap_paterno, c.ap_materno) LIKE ?";
                    case 2 ->
                        sql += " WHERE DATE(v.fecha_venta) = ?";
                    case 3 ->
                        sql += " WHERE ev.nombre_estado = ?";
                }
            }
            sql += " ORDER BY v.fecha_venta DESC";

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                if (valor != null && !valor.trim().isEmpty() && opcionBuscar >= 1 && opcionBuscar <= 3) {
                    if (opcionBuscar == 1) {
                        ps.setString(1, "%" + valor + "%");
                    } else {
                        ps.setString(1, valor);
                    }
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    modVentas.addRow(new Object[]{
                        rs.getString("venta_id"),
                        rs.getString("fecha_venta"),
                        rs.getString("nombre_empleado"),
                        rs.getString("nombre_cliente"),
                        rs.getString("articulos") != null ? rs.getString("articulos") : "Sin productos",
                        rs.getString("estado_venta"),
                        "$ " + rs.getString("total")
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Error de BD: " + e.getMessage()));
            }
        }).start();
    }

    public void mostrarDetalle(int ventaId) {
        modDetalle.setRowCount(0);
        new Thread(() -> {
            String sql = "SELECT p.nombre, dv.cantidad, dv.precio_unitario, dv.subtotal "
                    + "FROM detalle_venta dv JOIN producto p ON dv.producto_id = p.producto_id "
                    + "WHERE dv.venta_id = ?";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, ventaId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    modDetalle.addRow(new Object[]{
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        "$ " + rs.getDouble("precio_unitario"),
                        "$ " + rs.getDouble("subtotal")
                    });
                }
            } catch (Exception e) {
                /* silencioso */ }
        }).start();
    }

    private void liquidarPago() {
        int filaVisual = tablaVentas.getSelectedRow();
        if (filaVisual == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido de la lista superior para cobrar.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVisual);
        String estadoActual = modVentas.getValueAt(filaModelo, 5).toString();

        if (estadoActual.equalsIgnoreCase("Pagado")) {
            JOptionPane.showMessageDialog(this, "Esta venta ya está marcada como PAGADA.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String nombreCliente = modVentas.getValueAt(filaModelo, 3).toString();
        String total = modVentas.getValueAt(filaModelo, 6).toString();

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Confirmas recibir el pago de " + total + " y liquidar el pedido de " + nombreCliente + "?",
                "Liquidar Pago", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                int ventaId = Integer.parseInt(modVentas.getValueAt(filaModelo, 0).toString());
                cn.setAutoCommit(false);

                // ✅ Actualiza estado + registra fecha_pago
                String sqlVenta = "UPDATE venta SET estado_venta_id = "
                        + "(SELECT estado_venta_id FROM estado_venta WHERE nombre_estado = 'PAGADO'), "
                        + "fecha_pago = NOW() "
                        + "WHERE venta_id = ?";
                try (PreparedStatement psVenta = cn.prepareStatement(sqlVenta)) {
                    psVenta.setInt(1, ventaId);
                    psVenta.executeUpdate();
                }

                // ✅ 'LIQUIDADO' es el estado correcto en estado_anticipo
                String sqlAnticipo = "UPDATE anticipo SET estado_anticipo_id = "
                        + "(SELECT estado_anticipo_id FROM estado_anticipo WHERE nombre_estado = 'LIQUIDADO'), "
                        + "monto_restante = 0 WHERE venta_id = ?";
                try (PreparedStatement psAnticipo = cn.prepareStatement(sqlAnticipo)) {
                    psAnticipo.setInt(1, ventaId);
                    psAnticipo.executeUpdate();
                }

                cn.commit();
                cn.setAutoCommit(true);

                JOptionPane.showMessageDialog(this,
                        "Pago liquidado correctamente.\nLa Venta y sus Anticipos fueron marcados como 'PAGADO'.",
                        "Pago Exitoso", JOptionPane.INFORMATION_MESSAGE);
                mostrarVentas(0, null);
                lblGranTotal.setText("$ 0.00");
                lblClienteInfo.setText("Seleccione un pedido...");
                modDetalle.setRowCount(0);

            } catch (Exception e) {
                try {
                    cn.rollback();
                    cn.setAutoCommit(true);
                } catch (Exception ex) {
                    /* */ }
                JOptionPane.showMessageDialog(this, "Error al procesar el pago: " + e.getMessage(),
                        "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void eliminarVenta() {
        int filaVisual = tablaVentas.getSelectedRow();
        if (filaVisual == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione la venta que desea cancelar/eliminar.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaVentas.convertRowIndexToModel(filaVisual);
        int ventaId = Integer.parseInt(modVentas.getValueAt(filaModelo, 0).toString());

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está absolutamente seguro que desea cancelar y ELIMINAR la venta #" + ventaId
                + "?\nEsta acción no se puede deshacer.",
                "Peligro: Eliminar Venta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                cn.setAutoCommit(false);
                try (PreparedStatement deleteAnticipos = cn.prepareStatement(
                        "DELETE FROM anticipo WHERE venta_id = ?")) {
                    deleteAnticipos.setInt(1, ventaId);
                    deleteAnticipos.executeUpdate();
                }
                int ventasEliminadas;
                try (PreparedStatement deleteVenta = cn.prepareStatement(
                        "DELETE FROM venta WHERE venta_id = ?")) {
                    deleteVenta.setInt(1, ventaId);
                    ventasEliminadas = deleteVenta.executeUpdate();
                }
                cn.commit();

                if (ventasEliminadas > 0) {
                    JOptionPane.showMessageDialog(this, "Venta eliminada del sistema exitosamente.");
                    mostrarVentas(0, null);
                    modDetalle.setRowCount(0);
                    lblGranTotal.setText("$ 0.00");
                    lblClienteInfo.setText("Seleccione un pedido...");
                }
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException ex) {
                    /* */ }
                JOptionPane.showMessageDialog(this, "Error al eliminar la venta: " + e.getMessage(),
                        "Error BD", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    cn.setAutoCommit(true);
                } catch (SQLException ex) {
                    /* */ }
            }
        }
    }

    // =========================================================================
    // NUEVO: obtiene nombre completo del cajero desde la BD
    // =========================================================================
    private String obtenerNombreCompleto() {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT CONCAT_WS(' ', nombre, ap_paterno, ap_materno) AS nombre_completo "
                + "FROM empleado WHERE nombre_usuario = ?")) {
            ps.setString(1, loginvanabby.Login.usuarioActual);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre_completo");
            }
        } catch (Exception e) {
            /* silencioso */ }
        return loginvanabby.Login.usuarioActual; // fallback: username
    }

    // =========================================================================
    // UI
    // =========================================================================
    private void crearInterfazGrafica() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Módulo de Pagos y Cobros - Vanabby");
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(new Color(245, 230, 211));

        // ── HEADER ──────────────────────────────────────────────────────────
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(255, 244, 228));
        panelHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel pnlBienvenida = new JPanel(new GridLayout(2, 1));
        pnlBienvenida.setBackground(new Color(255, 244, 228));
        JLabel lblHola = new JLabel("Historial y Gestión de Pagos");
        lblHola.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHola.setForeground(new Color(255, 102, 51));
        JLabel lblInst = new JLabel("Aquí puedes liquidar pagos, revisar detalles de pedidos y gestionar anticipos.");
        lblInst.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInst.setForeground(Color.GRAY);
        pnlBienvenida.add(lblHola);
        pnlBienvenida.add(lblInst);

        JPanel pnlBotonesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBotonesHeader.setOpaque(false);

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(255, 51, 51));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrarSesion.setPreferredSize(new Dimension(140, 45));
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.addActionListener(e -> {
            new Login().setVisible(true);
            this.dispose();
        });

        btnVolverCaja = new JButton("Ir a Caja Principal");
        btnVolverCaja.setBackground(new Color(0, 102, 204));
        btnVolverCaja.setForeground(Color.WHITE);
        btnVolverCaja.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolverCaja.setPreferredSize(new Dimension(180, 45));
        btnVolverCaja.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolverCaja.addActionListener(e -> {
            new Registro_Venta().setVisible(true);
            this.dispose();
        });

        pnlBotonesHeader.add(btnCerrarSesion);
        pnlBotonesHeader.add(btnVolverCaja);

        panelHeader.add(pnlBienvenida, BorderLayout.CENTER);
        panelHeader.add(pnlBotonesHeader, BorderLayout.EAST);

        // ── SIDEBAR ─────────────────────────────────────────────────────────
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBackground(new Color(255, 244, 228));
        panelMenu.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelMenu.setPreferredSize(new Dimension(220, 0));

        JLabel lblLogo = new JLabel(new ImageIcon(
                getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png")));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMenu.add(lblLogo);
        panelMenu.add(Box.createVerticalStrut(30));

        btnPagar = crearBotonMenu("Liquidar Pago", new Color(0, 153, 51));
        btnPagar.addActionListener(e -> liquidarPago());

        btnRefrescar = crearBotonMenu("Actualizar Lista", new Color(100, 100, 100));
        btnRefrescar.addActionListener(e -> mostrarVentas(0, null));

        btnAnticipos = crearBotonMenu("Historial Anticipos", new Color(255, 153, 0));
        btnAnticipos.addActionListener(e -> {
            try {
                new TablaAnticipo().setVisible(true);
                this.dispose();
            } catch (Exception ex) {
                /* */ }
        });

        // ── Botón Corte de Caja — ahora pasa el nombre completo del cajero ──
        btnCorteCaja = crearBotonMenu("Corte de Caja", new Color(75, 0, 130));
        btnCorteCaja.addActionListener(e -> {
            String nombreCajero = obtenerNombreCompleto();
            new CorteCaja(cn, nombreCajero).setVisible(true);
        });

        btnEliminar = crearBotonMenu("Cancelar Venta", new Color(204, 0, 0));
        btnEliminar.addActionListener(e -> eliminarVenta());

        panelMenu.add(btnPagar);
        panelMenu.add(Box.createVerticalStrut(15));
        panelMenu.add(btnRefrescar);
        panelMenu.add(Box.createVerticalStrut(15));
        panelMenu.add(btnAnticipos);
        panelMenu.add(Box.createVerticalStrut(15));
        panelMenu.add(btnCorteCaja);
        panelMenu.add(Box.createVerticalStrut(40));
        panelMenu.add(btnEliminar);

        // ── ZONA CENTRAL ────────────────────────────────────────────────────
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBackground(new Color(245, 230, 211));
        panelCentral.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelBuscador.setBackground(new Color(255, 244, 228));
        panelBuscador.setBorder(BorderFactory.createTitledBorder("Filtros de Búsqueda"));
        cboBuscar = new JComboBox<>(new String[]{"Mostrar Todos", "Nombre del Cliente", "Fecha Venta", "Estado Pago"});
        txtBuscar = new JTextField(25);
        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 102, 51));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.addActionListener(e -> mostrarVentas(cboBuscar.getSelectedIndex(), txtBuscar.getText()));
        panelBuscador.add(cboBuscar);
        panelBuscador.add(txtBuscar);
        panelBuscador.add(btnBuscar);

        modVentas = new DefaultTableModel(
                new String[]{"Folio", "Fecha", "Cajero", "Cliente", "Artículos (Lo que compró)", "Estado", "Total_Oculto"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaVentas = new JTable(modVentas);
        tablaVentas.setRowHeight(30);
        tablaVentas.getTableHeader().setBackground(new Color(255, 102, 51));
        tablaVentas.getTableHeader().setForeground(Color.WHITE);
        tablaVentas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaVentas.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaVentas.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaVentas.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaVentas.getColumnModel().getColumn(4).setPreferredWidth(450);
        tablaVentas.getColumnModel().getColumn(5).setPreferredWidth(100);
        tablaVentas.removeColumn(tablaVentas.getColumnModel().getColumn(6));

        JScrollPane scrollVentas = new JScrollPane(tablaVentas);

        JPanel panelArriba = new JPanel(new BorderLayout());
        panelArriba.add(panelBuscador, BorderLayout.NORTH);
        panelArriba.add(scrollVentas, BorderLayout.CENTER);

        JPanel panelCobro = new JPanel(new BorderLayout(15, 0));
        panelCobro.setBackground(new Color(255, 244, 228));
        panelCobro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 153, 51), 2),
                "Desglose de la Venta", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), new Color(0, 153, 51)));
        panelCobro.setPreferredSize(new Dimension(0, 250));

        modDetalle = new DefaultTableModel(
                new String[]{"Producto", "Cantidad", "Precio Unitario", "Subtotal"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaDetalle = new JTable(modDetalle);
        tablaDetalle.setRowHeight(25);
        tablaDetalle.getTableHeader().setBackground(new Color(50, 50, 50));
        tablaDetalle.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);

        lblClienteInfo = new JLabel("Seleccione un pedido de la lista superior...");
        lblClienteInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClienteInfo.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel panelTablaDetalle = new JPanel(new BorderLayout());
        panelTablaDetalle.setBackground(new Color(255, 244, 228));
        panelTablaDetalle.add(lblClienteInfo, BorderLayout.NORTH);
        panelTablaDetalle.add(scrollDetalle, BorderLayout.CENTER);

        JPanel panelTotal = new JPanel(new GridLayout(2, 1));
        panelTotal.setBackground(new Color(255, 244, 228));
        panelTotal.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTituloTotal = new JLabel("TOTAL A COBRAR", SwingConstants.CENTER);
        lblTituloTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTituloTotal.setForeground(Color.GRAY);

        lblGranTotal = new JLabel("$ 0.00", SwingConstants.CENTER);
        lblGranTotal.setFont(new Font("Arial Black", Font.BOLD, 48));
        lblGranTotal.setForeground(new Color(204, 0, 0));

        panelTotal.add(lblTituloTotal);
        panelTotal.add(lblGranTotal);

        panelCobro.add(panelTablaDetalle, BorderLayout.CENTER);
        panelCobro.add(panelTotal, BorderLayout.EAST);

        panelCentral.add(panelArriba, BorderLayout.CENTER);
        panelCentral.add(panelCobro, BorderLayout.SOUTH);

        panelFondo.add(panelHeader, BorderLayout.NORTH);
        panelFondo.add(panelMenu, BorderLayout.WEST);
        panelFondo.add(panelCentral, BorderLayout.CENTER);

        setContentPane(panelFondo);
        pack();
    }

    private JButton crearBotonMenu(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 45));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName()) || "Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            /* */ }
        EventQueue.invokeLater(() -> {
            try {
                new Cajero().setVisible(true);
            } catch (SQLException ex) {
                /* */ }
        });
    }
}
