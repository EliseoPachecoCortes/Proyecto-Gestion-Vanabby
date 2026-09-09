package loginvanabby;

import com.toedter.calendar.JDateChooser;
import conexion.ConexionBD;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CorteCaja extends JDialog {

    private Connection cn;
    private String nombreCajero;

    private JTable tablaCorte;
    private DefaultTableModel modCorte;

    private JLabel lblTotalCobrado;
    private JLabel lblNumPagadas;
    private JLabel lblNumPendientes;
    private JLabel lblTotalPendientePorCobrar;
    private JLabel lblFechaActual;

    private JDateChooser dateChooser;
    private JButton btnGenerar;

    // Totales guardados para el ticket
    private double ultimoTotalCobrado = 0;
    private double ultimoTotalPendiente = 0;
    private int ultimoNumPagadas = 0;
    private int ultimoNumPendientes = 0;
    private String ultimaFecha = "";

    // Desglose por método de pago: nombre → {total, cantidad}
    private final Map<String, double[]> desglosePago = new LinkedHashMap<>();

    // Ventas agrupadas por método: nombre → lista de filas
    // Cada fila: [folio, hora, cliente, total, estado]
    private final Map<String, java.util.List<Object[]>> ventasPorMetodo = new LinkedHashMap<>();

    // Íconos por método
    private static final Map<String, String> ICONOS = new LinkedHashMap<>();

    static {
        ICONOS.put("Efectivo", "💵");
        ICONOS.put("Tarjeta de crédito", "💳");
        ICONOS.put("Transferencia", "📲");
        ICONOS.put("Mercado Pago", "💰");
    }

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public CorteCaja(Connection cn, String nombreCajero) {
        this.cn = cn;
        this.nombreCajero = nombreCajero;
        setTitle("Corte de Caja Diario - Vanabby Repostería");
        setModal(true);
        setSize(950, 680);
        setLocationRelativeTo(null);
        setResizable(true);

        crearUI();

        String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        ultimaFecha = hoy;
        lblFechaActual.setText("Corte del día: " + hoy);
        generarCorte(hoy);
    }

    // =========================================================================
    // CONSTRUCCIÓN DE LA INTERFAZ
    // =========================================================================
    private void crearUI() {
        JPanel fondo = new JPanel(new BorderLayout(10, 10));
        fondo.setBackground(new Color(245, 230, 211));
        fondo.setBorder(new EmptyBorder(20, 20, 15, 20));

        fondo.add(crearHeader(), BorderLayout.NORTH);
        fondo.add(crearTabla(), BorderLayout.CENTER);
        fondo.add(crearResumen(), BorderLayout.SOUTH);

        setContentPane(fondo);
    }

    // ── HEADER ────────────────────────────────────────────────────────────────
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(new Color(255, 244, 228));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlTitulo = new JPanel(new GridLayout(2, 1));
        pnlTitulo.setBackground(new Color(255, 244, 228));

        JLabel lblTitulo = new JLabel("Corte de Caja Diario");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(new Color(75, 0, 130));

        lblFechaActual = new JLabel("Corte del día: --");
        lblFechaActual.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFechaActual.setForeground(Color.GRAY);

        pnlTitulo.add(lblTitulo);
        pnlTitulo.add(lblFechaActual);

        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlControles.setBackground(new Color(255, 244, 228));

        JLabel lblEtiqueta = new JLabel("Seleccionar fecha:");
        lblEtiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        dateChooser.setMaxSelectableDate(new Date());
        dateChooser.setDateFormatString("dd MMM yyyy");
        dateChooser.setPreferredSize(new Dimension(145, 34));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnGenerar = new JButton("Generar Corte");
        btnGenerar.setBackground(new Color(75, 0, 130));
        btnGenerar.setForeground(Color.WHITE);
        btnGenerar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerar.setPreferredSize(new Dimension(150, 34));
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerar.setFocusPainted(false);
        btnGenerar.addActionListener(e -> {
            Date seleccionada = dateChooser.getDate();
            if (seleccionada == null) {
                JOptionPane.showMessageDialog(this,
                        "Por favor selecciona una fecha válida.",
                        "Fecha requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String fechaStr = new SimpleDateFormat("yyyy-MM-dd").format(seleccionada);
            ultimaFecha = fechaStr;
            lblFechaActual.setText("Corte del día: " + fechaStr);
            generarCorte(fechaStr);
        });

        JButton btnAceptar = new JButton("Aceptar Corte");
        btnAceptar.setBackground(new Color(0, 140, 60));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.setPreferredSize(new Dimension(155, 34));
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAceptar.setFocusPainted(false);
        btnAceptar.addActionListener(e -> mostrarTicket());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(new Color(150, 150, 150));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setPreferredSize(new Dimension(90, 34));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(e -> dispose());

        pnlControles.add(lblEtiqueta);
        pnlControles.add(dateChooser);
        pnlControles.add(btnGenerar);
        pnlControles.add(btnAceptar);
        pnlControles.add(btnCerrar);

        header.add(pnlTitulo, BorderLayout.WEST);
        header.add(pnlControles, BorderLayout.EAST);

        return header;
    }

    // ── TABLA ─────────────────────────────────────────────────────────────────
    private JScrollPane crearTabla() {
        modCorte = new DefaultTableModel(
                new String[]{"Folio", "Hora", "Cliente", "Artículos", "Total", "Estado", "Método Pago"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tablaCorte = new JTable(modCorte);
        tablaCorte.setRowHeight(30);
        tablaCorte.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaCorte.setSelectionBackground(new Color(220, 200, 255));
        tablaCorte.setGridColor(new Color(220, 210, 230));
        tablaCorte.setShowGrid(true);

        tablaCorte.getTableHeader().setBackground(new Color(75, 0, 130));
        tablaCorte.getTableHeader().setForeground(Color.WHITE);
        tablaCorte.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaCorte.getTableHeader().setReorderingAllowed(false);

        tablaCorte.getColumnModel().getColumn(0).setPreferredWidth(55);
        tablaCorte.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablaCorte.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaCorte.getColumnModel().getColumn(3).setPreferredWidth(270);
        tablaCorte.getColumnModel().getColumn(4).setPreferredWidth(85);
        tablaCorte.getColumnModel().getColumn(5).setPreferredWidth(90);
        tablaCorte.getColumnModel().getColumn(6).setPreferredWidth(130);

        // Renderer Estado
        tablaCorte.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSelected) {
                    String estado = value != null ? value.toString() : "";
                    if (estado.equalsIgnoreCase("PAGADO")) {
                        lbl.setForeground(new Color(0, 130, 50));
                        lbl.setBackground(new Color(220, 255, 225));
                    } else {
                        lbl.setForeground(new Color(180, 60, 0));
                        lbl.setBackground(new Color(255, 240, 210));
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        // Renderer Total
        tablaCorte.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                return lbl;
            }
        });

        // Renderer Método Pago
        tablaCorte.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (!isSelected) {
                    String metodo = value != null ? value.toString() : "";
                    switch (metodo) {
                        case "Efectivo" -> {
                            lbl.setForeground(new Color(0, 130, 50));
                            lbl.setBackground(new Color(230, 255, 230));
                        }
                        case "Tarjeta de crédito" -> {
                            lbl.setForeground(new Color(0, 80, 180));
                            lbl.setBackground(new Color(225, 235, 255));
                        }
                        case "Transferencia" -> {
                            lbl.setForeground(new Color(120, 60, 0));
                            lbl.setBackground(new Color(255, 245, 210));
                        }
                        case "Mercado Pago" -> {
                            lbl.setForeground(new Color(0, 110, 150));
                            lbl.setBackground(new Color(220, 245, 255));
                        }
                        default -> {
                            lbl.setForeground(Color.DARK_GRAY);
                            lbl.setBackground(Color.WHITE);
                        }
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(tablaCorte);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 0, 130), 2),
                "  Ventas registradas en el día seleccionado  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(75, 0, 130)));
        return scroll;
    }

    // ── RESUMEN (4 TARJETAS) ──────────────────────────────────────────────────
    private JPanel crearResumen() {
        JPanel panelResumen = new JPanel(new GridLayout(1, 4, 12, 0));
        panelResumen.setBackground(new Color(245, 230, 211));
        panelResumen.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel card1 = crearTarjeta("TOTAL COBRADO", "$ 0.00", new Color(0, 140, 60));
        JPanel card2 = crearTarjeta("VENTAS PAGADAS", "0", new Color(0, 102, 180));
        JPanel card3 = crearTarjeta("PENDIENTES", "0", new Color(190, 100, 0));
        JPanel card4 = crearTarjeta("POR COBRAR", "$ 0.00", new Color(160, 0, 0));

        lblTotalCobrado = obtenerLabelValor(card1);
        lblNumPagadas = obtenerLabelValor(card2);
        lblNumPendientes = obtenerLabelValor(card3);
        lblTotalPendientePorCobrar = obtenerLabelValor(card4);

        panelResumen.add(card1);
        panelResumen.add(card2);
        panelResumen.add(card3);
        panelResumen.add(card4);
        return panelResumen;
    }

    private JPanel crearTarjeta(String titulo, String valorInicial, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTit.setForeground(Color.WHITE);
        JLabel lblVal = new JLabel(valorInicial, SwingConstants.CENTER);
        lblVal.setFont(new Font("Arial Black", Font.BOLD, 26));
        lblVal.setForeground(Color.WHITE);
        lblVal.setName("valor");
        card.add(lblTit);
        card.add(lblVal);
        return card;
    }

    private JLabel obtenerLabelValor(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "valor".equals(c.getName())) {
                return (JLabel) c;
            }
        }
        return new JLabel();
    }

    // =========================================================================
    // LÓGICA: CONSULTA SQL
    // =========================================================================
    private void generarCorte(String fecha) {
        modCorte.setRowCount(0);
        desglosePago.clear();
        ventasPorMetodo.clear();

        new Thread(() -> {

            // Solo ventas PAGADAS ese día (por fecha_pago)
            // La hora que muestra es la hora en que se pagó
            String sqlVentas
                    = "SELECT v.venta_id, "
                    + "TIME(v.fecha_pago) AS hora, "
                    + "CONCAT_WS(' ', c.nombre, c.ap_paterno, c.ap_materno) AS cliente, "
                    + "(SELECT GROUP_CONCAT(CONCAT(dv.cantidad,'x ',p.nombre) SEPARATOR ', ') "
                    + " FROM detalle_venta dv JOIN producto p ON dv.producto_id = p.producto_id "
                    + " WHERE dv.venta_id = v.venta_id) AS articulos, "
                    + "v.total, ev.nombre_estado, mp.nombre_metodo "
                    + "FROM venta v "
                    + "LEFT JOIN cliente      c  ON v.cliente_id      = c.cliente_id "
                    + "LEFT JOIN estado_venta ev ON v.estado_venta_id = ev.estado_venta_id "
                    + "LEFT JOIN metodo_pago  mp ON v.metodo_pago_id  = mp.metodo_pago_id "
                    + "WHERE ev.nombre_estado = 'PAGADO' "
                    + "AND DATE(v.fecha_pago) = ? "
                    + "ORDER BY mp.metodo_pago_id, v.fecha_pago ASC";

            // ✅ Totales: solo pagadas ese día por fecha_pago
            // ✅ Pendientes ya no se muestran → siempre 0
            String sqlTotales
                    = "SELECT "
                    + "COALESCE(SUM(v.total), 0) AS total_cobrado, "
                    + "0                         AS total_pendiente, "
                    + "COUNT(*)                  AS num_pagadas, "
                    + "0                         AS num_pendientes "
                    + "FROM venta v "
                    + "LEFT JOIN estado_venta ev ON v.estado_venta_id = ev.estado_venta_id "
                    + "WHERE ev.nombre_estado = 'PAGADO' "
                    + "AND DATE(v.fecha_pago) = ?";

            // ✅ Desglose por método: solo pagadas ese día por fecha_pago
            String sqlDesglose
                    = "SELECT mp.nombre_metodo, SUM(v.total) AS total_metodo, COUNT(*) AS cantidad "
                    + "FROM venta v "
                    + "LEFT JOIN metodo_pago  mp ON v.metodo_pago_id  = mp.metodo_pago_id "
                    + "LEFT JOIN estado_venta ev ON v.estado_venta_id = ev.estado_venta_id "
                    + "WHERE ev.nombre_estado = 'PAGADO' "
                    + "AND DATE(v.fecha_pago) = ? "
                    + "GROUP BY mp.metodo_pago_id, mp.nombre_metodo "
                    + "ORDER BY mp.metodo_pago_id";

            try (
                    PreparedStatement psVentas = cn.prepareStatement(sqlVentas); PreparedStatement psTotales = cn.prepareStatement(sqlTotales); PreparedStatement psDesglose = cn.prepareStatement(sqlDesglose)) {
                // cada query usa ? una sola vez
                psVentas.setString(1, fecha);
                psTotales.setString(1, fecha);
                psDesglose.setString(1, fecha);

                // ── Llenar tabla principal ────────────────────────────────────
                ResultSet rsVentas = psVentas.executeQuery();
                while (rsVentas.next()) {
                    String articulos = rsVentas.getString("articulos");
                    String metodo = rsVentas.getString("nombre_metodo");
                    String estado = rsVentas.getString("nombre_estado");
                    String folio = rsVentas.getString("venta_id");
                    String hora = rsVentas.getString("hora");
                    String cliente = rsVentas.getString("cliente");
                    double total = rsVentas.getDouble("total");

                    if (metodo == null) {
                        metodo = "Sin método";
                    }

                    modCorte.addRow(new Object[]{
                        folio, hora, cliente,
                        articulos != null ? articulos : "Sin productos",
                        "$ " + String.format("%.2f", total),
                        estado, metodo
                    });

                    ventasPorMetodo
                            .computeIfAbsent(metodo, k -> new java.util.ArrayList<>())
                            .add(new Object[]{folio, hora,
                        articulos != null ? articulos : "Sin productos", total, estado});
                }

                // ── Totales generales ─────────────────────────────────────────
                ResultSet rsTotales = psTotales.executeQuery();
                if (rsTotales.next()) {
                    ultimoTotalCobrado = rsTotales.getDouble("total_cobrado");
                    ultimoTotalPendiente = 0;
                    ultimoNumPagadas = rsTotales.getInt("num_pagadas");
                    ultimoNumPendientes = 0;
                }

                // ── Desglose por método ───────────────────────────────────────
                desglosePago.clear();
                ResultSet rsDesglose = psDesglose.executeQuery();
                while (rsDesglose.next()) {
                    String metodo = rsDesglose.getString("nombre_metodo");
                    double totalMet = rsDesglose.getDouble("total_metodo");
                    int cantidad = rsDesglose.getInt("cantidad");
                    if (metodo == null) {
                        metodo = "Sin método";
                    }
                    desglosePago.put(metodo, new double[]{totalMet, cantidad});
                }

                SwingUtilities.invokeLater(() -> {
                    lblTotalCobrado.setText(String.format("$ %.2f", ultimoTotalCobrado));
                    lblNumPagadas.setText(String.valueOf(ultimoNumPagadas));
                    lblNumPendientes.setText("0");
                    lblTotalPendientePorCobrar.setText("$ 0.00");

                    if (ultimoNumPagadas == 0) {
                        JOptionPane.showMessageDialog(CorteCaja.this,
                                "No se encontraron ventas pagadas para la fecha: " + fecha,
                                "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(CorteCaja.this,
                                "Error al generar el corte:\n" + ex.getMessage(),
                                "Error BD", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    // =========================================================================
    // TICKET — organizado por sección de método de pago
    // =========================================================================
    private void mostrarTicket() {
        if (ultimoNumPagadas == 0 && ultimoNumPendientes == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay datos de corte.\nGenera el corte primero.",
                    "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlgTicket = new JDialog(this, "Ticket - Corte de Caja", true);
        dlgTicket.setSize(460, 700);
        dlgTicket.setLocationRelativeTo(this);
        dlgTicket.setResizable(false);

        JPanel pnlTicket = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        pnlTicket.setLayout(new BoxLayout(pnlTicket, BoxLayout.Y_AXIS));
        pnlTicket.setBorder(new EmptyBorder(22, 30, 22, 30));
        pnlTicket.setBackground(Color.WHITE);

        String horaGenerado = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new Date());

        // ── Encabezado ────────────────────────────────────────────────────────
        pnlTicket.add(lineaCentrada("Vanabby Repostería",
                new Font("Segoe UI", Font.BOLD, 18), new Color(75, 0, 130)));
        pnlTicket.add(lineaCentrada("CORTE DE CAJA DIARIO",
                new Font("Segoe UI", Font.BOLD, 14), Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(4));
        pnlTicket.add(separador());
        pnlTicket.add(Box.createVerticalStrut(6));
        pnlTicket.add(lineaDato("Cajero:", nombreCajero));
        pnlTicket.add(lineaDato("Fecha corte:", ultimaFecha));
        pnlTicket.add(lineaDato("Generado:", horaGenerado));
        pnlTicket.add(Box.createVerticalStrut(10));
        pnlTicket.add(separador());

        // ── SECCIONES POR MÉTODO DE PAGO ─────────────────────────────────────
        for (Map.Entry<String, java.util.List<Object[]>> entry : ventasPorMetodo.entrySet()) {
            String metodo = entry.getKey();
            java.util.List<Object[]> ventas = entry.getValue();
            String icono = ICONOS.getOrDefault(metodo, "💳");

            Color colorMetodo;
            Color colorFondo;
            switch (metodo) {
                case "Efectivo" -> {
                    colorMetodo = new Color(0, 130, 50);
                    colorFondo = new Color(230, 255, 230);
                }
                case "Tarjeta de crédito" -> {
                    colorMetodo = new Color(0, 80, 180);
                    colorFondo = new Color(225, 235, 255);
                }
                case "Transferencia" -> {
                    colorMetodo = new Color(150, 80, 0);
                    colorFondo = new Color(255, 245, 210);
                }
                case "Mercado Pago" -> {
                    colorMetodo = new Color(0, 110, 150);
                    colorFondo = new Color(220, 245, 255);
                }
                default -> {
                    colorMetodo = new Color(80, 80, 80);
                    colorFondo = new Color(245, 245, 245);
                }
            }

            pnlTicket.add(Box.createVerticalStrut(10));

            // Encabezado de sección — fondo de color del método
            JPanel pnlSeccion = new JPanel(new BorderLayout());
            pnlSeccion.setBackground(colorMetodo);
            pnlSeccion.setBorder(new EmptyBorder(6, 10, 6, 10));
            pnlSeccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            JLabel lblSeccion = new JLabel(metodo.toUpperCase());
            lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblSeccion.setForeground(Color.WHITE);
            pnlSeccion.add(lblSeccion, BorderLayout.WEST);
            pnlTicket.add(pnlSeccion);

            // Filas de ventas de este método
            double subtotalMetodo = 0;
            int ventasPagadas = 0;
            int ventasPendientes = 0;

            for (int i = 0; i < ventas.size(); i++) {
                Object[] v = ventas.get(i);
                String folio = v[0].toString();
                String hora = v[1].toString();
                String cliente = v[2].toString();
                double total = (double) v[3];
                String estado = v[4].toString();

                subtotalMetodo += total;
                if (estado.equalsIgnoreCase("PAGADO")) {
                    ventasPagadas++;
                } else {
                    ventasPendientes++;
                }

                JPanel fila = new JPanel(new BorderLayout(6, 0));
                fila.setBackground(i % 2 == 0 ? colorFondo : Color.WHITE);
                fila.setBorder(new EmptyBorder(4, 10, 4, 10));
                fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                // Izquierda: folio + hora + cliente
                JLabel lblIzq = new JLabel(
                        String.format("#%-4s  %s  —  %s", folio, hora,
                                cliente.length() > 22 ? cliente.substring(0, 20) + "…" : cliente));
                lblIzq.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblIzq.setForeground(Color.DARK_GRAY);

                // Derecha: total + estado
                JPanel pnlDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
                pnlDer.setBackground(fila.getBackground());

                JLabel lblTotal = new JLabel("$ " + String.format("%.2f", total));
                lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblTotal.setForeground(colorMetodo);

                JLabel lblEstado = new JLabel(
                        estado.equalsIgnoreCase("PAGADO") ? "PAGADO" : "PENDIENTE");
                lblEstado.setForeground(
                        estado.equalsIgnoreCase("PAGADO") ? new Color(0, 130, 50) : new Color(180, 60, 0));
                lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                pnlDer.add(lblTotal);
                pnlDer.add(lblEstado);

                fila.add(lblIzq, BorderLayout.WEST);
                fila.add(pnlDer, BorderLayout.EAST);
                pnlTicket.add(fila);
            }

            // Subtotal del método
            JPanel pnlSub = new JPanel(new BorderLayout());
            pnlSub.setBackground(new Color(colorMetodo.getRed(),
                    colorMetodo.getGreen(), colorMetodo.getBlue(), 40));
            pnlSub.setBackground(colorFondo);
            pnlSub.setBorder(new EmptyBorder(5, 10, 5, 10));
            pnlSub.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel lblSubInfo = new JLabel(
                    String.format("  %d venta%s  | PAG: %d  |  PEND: %d",
                            ventas.size(), ventas.size() != 1 ? "s" : "",
                            ventasPagadas, ventasPendientes));
            lblSubInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSubInfo.setForeground(colorMetodo);

            JLabel lblSubTotal = new JLabel("SUBTOTAL: $ " + String.format("%.2f", subtotalMetodo));
            lblSubTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSubTotal.setForeground(colorMetodo);

            pnlSub.add(lblSubInfo, BorderLayout.WEST);
            pnlSub.add(lblSubTotal, BorderLayout.EAST);
            pnlTicket.add(pnlSub);
        }

        // ── TOTALES GENERALES ─────────────────────────────────────────────────
        pnlTicket.add(Box.createVerticalStrut(12));
        pnlTicket.add(separador());
        pnlTicket.add(Box.createVerticalStrut(8));
        pnlTicket.add(lineaCentrada("RESUMEN GENERAL",
                new Font("Segoe UI", Font.BOLD, 13), new Color(75, 0, 130)));
        pnlTicket.add(Box.createVerticalStrut(6));
        pnlTicket.add(lineaTotal("Ventas pagadas:", String.valueOf(ultimoNumPagadas), new Color(0, 102, 180)));
        pnlTicket.add(lineaTotal("Ventas pendientes:", String.valueOf(ultimoNumPendientes), new Color(190, 100, 0)));
        pnlTicket.add(Box.createVerticalStrut(6));
        pnlTicket.add(lineaTotal("TOTAL COBRADO:", String.format("$ %.2f", ultimoTotalCobrado), new Color(0, 140, 60)));
        pnlTicket.add(lineaTotal("POR COBRAR:", String.format("$ %.2f", ultimoTotalPendiente), new Color(160, 0, 0)));

        // ── Pie ───────────────────────────────────────────────────────────────
        pnlTicket.add(Box.createVerticalStrut(14));
        pnlTicket.add(separador());
        pnlTicket.add(Box.createVerticalStrut(8));
        pnlTicket.add(lineaCentrada("Firma del cajero: ___________________",
                new Font("Segoe UI", Font.PLAIN, 12), Color.BLACK));
        pnlTicket.add(Box.createVerticalStrut(4));
        pnlTicket.add(lineaCentrada("Vanabby Repostería © 2025",
                new Font("Segoe UI", Font.ITALIC, 11), Color.GRAY));

        // ── Botones ───────────────────────────────────────────────────────────
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        pnlBotones.setBackground(Color.WHITE);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBackground(new Color(75, 0, 130));
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnImprimir.setFocusPainted(false);
        btnImprimir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnImprimir.addActionListener(e -> imprimirTicket(pnlTicket));

        JButton btnCerrar2 = new JButton("Cerrar");
        btnCerrar2.setBackground(new Color(150, 150, 150));
        btnCerrar2.setForeground(Color.WHITE);
        btnCerrar2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar2.setFocusPainted(false);
        btnCerrar2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar2.addActionListener(e -> dlgTicket.dispose());

        pnlBotones.add(btnImprimir);
        pnlBotones.add(btnCerrar2);

        JScrollPane scroll = new JScrollPane(pnlTicket);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.WHITE);
        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(pnlBotones, BorderLayout.SOUTH);

        dlgTicket.setContentPane(contenedor);
        dlgTicket.setVisible(true);
    }

    // =========================================================================
    // HELPERS TICKET
    // =========================================================================
    private JLabel lineaCentrada(String texto, Font fuente, Color color) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(fuente);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return lbl;
    }

    private JPanel lineaDato(String clave, String valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(Color.WHITE);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lblC = new JLabel(clave);
        lblC.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblV = new JLabel(valor);
        lblV.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fila.add(lblC, BorderLayout.WEST);
        fila.add(lblV, BorderLayout.EAST);
        return fila;
    }

    private JPanel lineaTotal(String etiqueta, String valor, Color color) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(Color.WHITE);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lblE = new JLabel(etiqueta);
        lblE.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblE.setForeground(color);
        JLabel lblV = new JLabel(valor);
        lblV.setFont(new Font("Arial Black", Font.BOLD, 14));
        lblV.setForeground(color);
        fila.add(lblE, BorderLayout.WEST);
        fila.add(lblV, BorderLayout.EAST);
        return fila;
    }

    private JLabel separador() {
        JLabel sep = new JLabel("─────────────────────────────────");
        sep.setFont(new Font("Monospaced", Font.PLAIN, 12));
        sep.setForeground(Color.LIGHT_GRAY);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return sep;
    }

    // =========================================================================
    // IMPRESIÓN
    // =========================================================================
    private void imprimirTicket(JPanel panel) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Corte de Caja - Vanabby");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scaleX = pageFormat.getImageableWidth() / panel.getWidth();
            double scaleY = pageFormat.getImageableHeight() / panel.getHeight();
            double scale = Math.min(scaleX, scaleY);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.scale(scale, scale);
            panel.printAll(g2);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al imprimir:\n" + ex.getMessage(),
                        "Error impresión", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
