package loginvanabby;


import conexion.ConexionBD;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Interfaz Profesional - Listado de Anticipos (Modo Lectura)
 */
public class TablaAnticipo extends javax.swing.JFrame {

    private Connection cn;
    private javax.swing.JFrame ventanaPadre;

    // Componentes de la UI
    private JTable tablaAnticipos;
    private DefaultTableModel modAnticipos;
    private JComboBox<String> cboBuscar;
    private JTextField txtBuscar;
    private JButton btnBuscar, btnVolver, btnLimpiarFiltro;

    public TablaAnticipo() throws SQLException {
        this(null);
    }

    public TablaAnticipo(javax.swing.JFrame padre) throws SQLException {
        this.ventanaPadre = padre;
        this.cn = ConexionBD.getConexion();
        
        crearInterfazGrafica();
        
        setTitle("Historial de Anticipos - Vanabby");
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa
        
        mostrarAnticipos(0, ""); // Cargar todos por defecto
    }

    // ==========================================================
    // LÓGICA: LISTAR ANTICIPOS (CON JOIN PARA VER NOMBRES)
    // ==========================================================
    public void mostrarAnticipos(int opcionBuscar, String valorBuscado) {
        modAnticipos.setRowCount(0); // Limpiar tabla antes de buscar

        // Consulta mejorada: Trae nombres en lugar de puros IDs
        String sql = "SELECT a.anticipo_id, a.venta_id, "
                   + "CONCAT_WS(' ', c.nombre, c.ap_paterno, c.ap_materno) AS nombre_cliente, "
                   + "a.monto_anticipo, a.monto_restante, "
                   + "a.fecha_registro, a.fecha_limite, "
                   + "ea.nombre_estado AS estado "
                   + "FROM anticipo a "
                   + "LEFT JOIN cliente c ON a.cliente_id = c.cliente_id "
                   + "LEFT JOIN estado_anticipo ea ON a.estado_anticipo_id = ea.estado_anticipo_id";

        // Filtros de Búsqueda
        if (valorBuscado != null && !valorBuscado.trim().isEmpty()) {
            switch (opcionBuscar) {
                case 1 -> sql += " WHERE DATE(a.fecha_registro) = ?"; // Búsqueda por Fecha (YYYY-MM-DD)
                case 2 -> sql += " WHERE CONCAT_WS(' ', c.nombre, c.ap_paterno, c.ap_materno) LIKE ?"; // Búsqueda por Cliente
                case 3 -> sql += " WHERE a.venta_id = ?"; // Búsqueda por Folio de Venta
            }
        }
        
        sql += " ORDER BY a.fecha_registro DESC"; // Los más recientes primero

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            
            // Asignar parámetros de búsqueda
            if (valorBuscado != null && !valorBuscado.trim().isEmpty()) {
                if (opcionBuscar == 2) {
                    ps.setString(1, "%" + valorBuscado + "%"); // LIKE para texto parcial
                } else {
                    ps.setString(1, valorBuscado); // Fecha exacta o ID exacto
                }
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modAnticipos.addRow(new Object[]{
                    rs.getString("anticipo_id"),
                    "#" + rs.getString("venta_id"),
                    rs.getString("nombre_cliente"),
                    "$ " + String.format("%.2f", rs.getDouble("monto_anticipo")),
                    "$ " + String.format("%.2f", rs.getDouble("monto_restante")),
                    rs.getString("fecha_registro"),
                    rs.getString("fecha_limite") != null ? rs.getString("fecha_limite") : "Sin límite",
                    rs.getString("estado")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla de anticipos:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================================
    // CREACIÓN DE INTERFAZ GRÁFICA (LIMPIA Y PROFESIONAL)
    // ==========================================================
    private void crearInterfazGrafica() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(new Color(245, 230, 211));

        // --- HEADER ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(255, 244, 228));
        panelHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlVolverWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10)); 
        pnlVolverWrapper.setOpaque(false);
        btnVolver = new JButton("Volver al Menú");
        btnVolver.setBackground(new Color(255, 51, 51)); btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        btnVolver.setPreferredSize(new Dimension(150, 40));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> { 
            try { new Cajero().setVisible(true); this.dispose(); } catch (Exception ex) {} 
        });
        pnlVolverWrapper.add(btnVolver);

        JLabel lblTitulo = new JLabel("Historial de Anticipos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32)); 
        lblTitulo.setForeground(new Color(255, 102, 51));

        panelHeader.add(pnlVolverWrapper, BorderLayout.WEST);
        panelHeader.add(lblTitulo, BorderLayout.CENTER);
        panelHeader.add(new JLabel(new ImageIcon(getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png"))), BorderLayout.EAST);

        // --- ZONA CENTRAL ---
        JPanel panelCentral = new JPanel(new BorderLayout(0, 20));
        panelCentral.setBackground(new Color(245, 230, 211));
        panelCentral.setBorder(new EmptyBorder(20, 30, 20, 30));

        // PANEL DE BÚSQUEDA
        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelBuscador.setBackground(new Color(255, 244, 228));
        panelBuscador.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2), "Filtros de Búsqueda", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204)));

        cboBuscar = new JComboBox<>(new String[]{"Mostrar Todos", "Fecha de Registro (YYYY-MM-DD)", "Nombre del Cliente", "Folio de Venta (#)"});
        cboBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboBuscar.setPreferredSize(new Dimension(250, 35));

        txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setPreferredSize(new Dimension(350, 35));

        btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.setBackground(new Color(0, 153, 51)); btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuscar.setPreferredSize(new Dimension(120, 35));
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> mostrarAnticipos(cboBuscar.getSelectedIndex(), txtBuscar.getText().trim()));

        btnLimpiarFiltro = new JButton("Limpiar Filtro");
        btnLimpiarFiltro.setBackground(new Color(150, 150, 150)); btnLimpiarFiltro.setForeground(Color.WHITE);
        btnLimpiarFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLimpiarFiltro.setPreferredSize(new Dimension(140, 35));
        btnLimpiarFiltro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarFiltro.addActionListener(e -> {
            cboBuscar.setSelectedIndex(0);
            txtBuscar.setText("");
            mostrarAnticipos(0, "");
        });

        panelBuscador.add(cboBuscar);
        panelBuscador.add(txtBuscar);
        panelBuscador.add(btnBuscar);
        panelBuscador.add(btnLimpiarFiltro);

        // TABLA DE ANTICIPOS
        String[] columnas = {"Folio Anticipo", "Venta", "Cliente", "Abonado", "Resta Pagar", "Fecha Registro", "Fecha Límite", "Estado"};
        modAnticipos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; } // MODO LECTURA TOTAL
        };

        tablaAnticipos = new JTable(modAnticipos);
        tablaAnticipos.setRowHeight(35); // Filas muy amplias
        tablaAnticipos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Diseño del encabezado de la tabla
        tablaAnticipos.getTableHeader().setBackground(new Color(255, 102, 51));
        tablaAnticipos.getTableHeader().setForeground(Color.WHITE);
        tablaAnticipos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tablaAnticipos.getTableHeader().setReorderingAllowed(false);
        tablaAnticipos.setSelectionBackground(new Color(255, 204, 153));
        tablaAnticipos.setSelectionForeground(Color.BLACK);

        JScrollPane scrollTabla = new JScrollPane(tablaAnticipos);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollTabla.getViewport().setBackground(Color.WHITE);

        panelCentral.add(panelBuscador, BorderLayout.NORTH);
        panelCentral.add(scrollTabla, BorderLayout.CENTER);

        panelFondo.add(panelHeader, BorderLayout.NORTH);
        panelFondo.add(panelCentral, BorderLayout.CENTER);

        setContentPane(panelFondo);
        pack();
    }

    public static void main(String args[]) {
        try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } } catch (Exception ex) {}
        EventQueue.invokeLater(() -> {
            try { new TablaAnticipo().setVisible(true); } catch (SQLException ex) {}
        });
    }
}