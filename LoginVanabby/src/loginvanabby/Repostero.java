package loginvanabby;


import conexion.ConexionBD;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo de Repostero - Repostería Vanabby (HU-007)
 * Incluye Sistema FEFO automatizado y multiplicación directa de stock.
 */
public class Repostero extends javax.swing.JFrame {

    private Connection cn;
    private int recetaSeleccionadaId = -1;
    private int productoSeleccionadoId = -1;
    private int rendimientoReceta = 1;

    // Componentes de Interfaz
    private JPanel jPanelFondo;
    private JTabbedPane tabbedPane;
    private JTable tablaRecetas, tablaIngredientes, tablaPasos, tablaPedidos;
    private DefaultTableModel modRecetas, modIngredientes, modPasos, modPedidos;
    private JButton btnRegistrar, btnCerrarSesion;
    private JSpinner spnUnidades;
    private JLabel lblRecetaInfo;

    public Repostero() throws SQLException {
        this.cn = ConexionBD.getConexion();
        crearInterfazGrafica();
        configurarUIProfesional();
        
        cargarRecetas();
        cargarPedidosDelDia(); 

        // Listener para la selección de receta
        tablaRecetas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaRecetas.getSelectedRow() != -1) {
                seleccionarReceta();
            }
        });
    }

    private void configurarUIProfesional() {
        setTitle("Panel de Producción - Repostero");
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        btnRegistrar.putClientProperty("JButton.buttonType", "roundRect");
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));

        estilizarTabla(tablaRecetas);
        estilizarTabla(tablaIngredientes);
        estilizarTabla(tablaPasos);
        estilizarTabla(tablaPedidos);
        
        spnUnidades.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(255, 102, 51));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setOpaque(false);
        tabla.setSelectionBackground(new Color(255, 204, 153));
        tabla.setSelectionForeground(Color.BLACK);
    }

    private void cargarRecetas() {
        modRecetas.setRowCount(0);
        String sql = "SELECT r.receta_id, p.producto_id, p.nombre, r.rendimiento, r.tiempo_estimado_min "
                   + "FROM receta r JOIN producto p ON r.producto_id = p.producto_id";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modRecetas.addRow(new Object[]{
                    rs.getInt("receta_id"),
                    rs.getInt("producto_id"),
                    rs.getString("nombre"),
                    rs.getInt("rendimiento") + " unidades",
                    rs.getInt("tiempo_estimado_min") + " min"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recetas: " + e.getMessage());
        }
    }

    private void cargarPedidosDelDia() {
        modPedidos.setRowCount(0);
        String sql = "SELECT v.venta_id, p.nombre, dv.cantidad, v.fecha_entrega_programada, ev.nombre_estado "
                   + "FROM venta v "
                   + "JOIN detalle_venta dv ON v.venta_id = dv.venta_id "
                   + "JOIN producto p ON dv.producto_id = p.producto_id "
                   + "JOIN estado_venta ev ON v.estado_venta_id = ev.estado_venta_id "
                   + "WHERE ev.nombre_estado IN ('PENDIENTE', 'EN_PREPARACION') "
                   + "ORDER BY v.fecha_entrega_programada ASC"; 
        
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modPedidos.addRow(new Object[]{
                    rs.getInt("venta_id"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    rs.getString("fecha_entrega_programada"),
                    rs.getString("nombre_estado")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + e.getMessage());
        }
    }

    private void seleccionarReceta() {
        int filaVisual = tablaRecetas.getSelectedRow();
        if (filaVisual == -1) return;

        int filaModel = tablaRecetas.convertRowIndexToModel(filaVisual);

        recetaSeleccionadaId = (int) modRecetas.getValueAt(filaModel, 0);
        productoSeleccionadoId = (int) modRecetas.getValueAt(filaModel, 1);
        
        String nombre = modRecetas.getValueAt(filaModel, 2).toString();
        String rendStr = modRecetas.getValueAt(filaModel, 3).toString().split(" ")[0];
        rendimientoReceta = Integer.parseInt(rendStr);

        lblRecetaInfo.setText("Receta: " + nombre + " (Rinde " + rendimientoReceta + " uds)");
        spnUnidades.setValue(rendimientoReceta); 

        cargarDetallesReceta(recetaSeleccionadaId);
    }

    private void cargarDetallesReceta(int idReceta) {
        modIngredientes.setRowCount(0);
        modPasos.setRowCount(0);

        String sqlIng = "SELECT i.nombre, ri.cantidad, u.simbolo "
                      + "FROM receta_ingrediente ri "
                      + "JOIN ingrediente i ON ri.ingrediente_id = i.ingrediente_id "
                      + "JOIN unidad_medida u ON i.unidad_medida_id = u.unidad_medida_id "
                      + "WHERE ri.receta_id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlIng)) {
            ps.setInt(1, idReceta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modIngredientes.addRow(new Object[]{
                    rs.getString("nombre"),
                    rs.getDouble("cantidad") + " " + rs.getString("simbolo")
                });
            }
        } catch (SQLException e) {}

        String sqlPasos = "SELECT numero_paso, descripcion FROM paso_receta WHERE receta_id = ? ORDER BY numero_paso ASC";
        try (PreparedStatement ps = cn.prepareStatement(sqlPasos)) {
            ps.setInt(1, idReceta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modPasos.addRow(new Object[]{
                    "Paso " + rs.getInt("numero_paso"),
                    rs.getString("descripcion")
                });
            }
        } catch (SQLException e) {}
    }

    // =========================================================================
    // LÓGICA DE PRODUCCIÓN CORREGIDA: MULTIPLICACIÓN DIRECTA Y FEFO
    // =========================================================================
    private void registrarProduccion() {
        if (recetaSeleccionadaId == -1 || productoSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una receta válida primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int unidadesProducidas = (int) spnUnidades.getValue();
        
        // CORRECCIÓN: Multiplicación directa. Si piden 6, se multiplica por 6 exactamente.
        double multiplicador = (double) unidadesProducidas;

        int confirmar = JOptionPane.showConfirmDialog(this, 
            "¿Confirmas la producción de " + unidadesProducidas + " unidades?\nSe descontarán los ingredientes automáticamente (" + unidadesProducidas + "x).", 
            "Confirmar Producción", JOptionPane.YES_NO_OPTION);
            
        if (confirmar != JOptionPane.YES_OPTION) return;

        try {
            cn.setAutoCommit(false); 

            // PASO 1: VERIFICAR QUE HAYA STOCK SUFICIENTE DE TODOS LOS INGREDIENTES ANTES DE DESCONTAR
            String sqlIngReq = "SELECT i.nombre, ri.ingrediente_id, ri.cantidad FROM receta_ingrediente ri JOIN ingrediente i ON ri.ingrediente_id = i.ingrediente_id WHERE ri.receta_id = ?";
            PreparedStatement psReq = cn.prepareStatement(sqlIngReq);
            psReq.setInt(1, recetaSeleccionadaId);
            ResultSet rsReq = psReq.executeQuery();

            ArrayList<IngredienteRequerido> requeridos = new ArrayList<>();
            StringBuilder faltantesMsg = new StringBuilder();
            boolean hayFaltantes = false;

            PreparedStatement psCheckStock = cn.prepareStatement("SELECT COALESCE(SUM(cantidad), 0) FROM lote_inventario WHERE ingrediente_id = ? AND cantidad > 0");

            while (rsReq.next()) {
                int idIngrediente = rsReq.getInt("ingrediente_id");
                String nombreIng = rsReq.getString("nombre");
                double cantidadBase = rsReq.getDouble("cantidad");
                
                // MULTIPLICACIÓN DIRECTA Y EXACTA
                double cantidadNecesaria = cantidadBase * multiplicador;

                psCheckStock.setInt(1, idIngrediente);
                ResultSet rsStock = psCheckStock.executeQuery();
                double stockDisponible = 0;
                if (rsStock.next()) {
                    stockDisponible = rsStock.getDouble(1);
                }
                rsStock.close();

                if (stockDisponible < cantidadNecesaria) {
                    hayFaltantes = true;
                    faltantesMsg.append("• ").append(nombreIng).append(" (Se necesitan: ").append(String.format("%.2f", cantidadNecesaria))
                                .append(", Faltan: ").append(String.format("%.2f", cantidadNecesaria - stockDisponible)).append(")\n");
                } else {
                    requeridos.add(new IngredienteRequerido(idIngrediente, cantidadNecesaria));
                }
            }
            psCheckStock.close();
            rsReq.close();
            psReq.close();

            // Si falta algo, bloqueamos la transacción y avisamos al usuario
            if (hayFaltantes) {
                cn.rollback();
                cn.setAutoCommit(true);
                JOptionPane.showMessageDialog(this, "No se puede producir. Faltan los siguientes ingredientes en el inventario:\n\n" + faltantesMsg.toString(), "Stock Insuficiente", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // PASO 2: DESCONTAR INGREDIENTES POR FEFO (Ordenando por fecha de caducidad)
            String sqlLotes = "SELECT lote_id, cantidad FROM lote_inventario WHERE ingrediente_id = ? AND cantidad > 0 ORDER BY fecha_caducidad ASC";
            PreparedStatement psLotes = cn.prepareStatement(sqlLotes);
            
            PreparedStatement psUpdateLote = cn.prepareStatement("UPDATE lote_inventario SET cantidad = ? WHERE lote_id = ?");
            PreparedStatement psDeleteLote = cn.prepareStatement("DELETE FROM lote_inventario WHERE lote_id = ?");

            for (IngredienteRequerido req : requeridos) {
                double cantidadRestante = req.cantidadNecesaria;
                
                psLotes.setInt(1, req.idIngrediente);
                ResultSet rsLotes = psLotes.executeQuery();

                while (rsLotes.next() && cantidadRestante > 0) {
                    int idLote = rsLotes.getInt("lote_id");
                    double cantidadEnLote = rsLotes.getDouble("cantidad");

                    if (cantidadEnLote > cantidadRestante) {
                        // El lote tiene más de lo que necesitamos, le restamos la cantidad y terminamos
                        psUpdateLote.setDouble(1, cantidadEnLote - cantidadRestante);
                        psUpdateLote.setInt(2, idLote);
                        psUpdateLote.executeUpdate();
                        cantidadRestante = 0;
                    } else {
                        // Nos acabamos todo el lote. Lo borramos de la base de datos para no dejar basura.
                        psDeleteLote.setInt(1, idLote);
                        psDeleteLote.executeUpdate();
                        cantidadRestante -= cantidadEnLote;
                    }
                }
                rsLotes.close();
            }
            psLotes.close();
            psUpdateLote.close();
            psDeleteLote.close();

            // PASO 3: INYECTAR LOS PRODUCTOS TERMINADOS AL INVENTARIO DEL CAJERO
            String sqlCheckProd = "SELECT producto_id FROM inventario_producto WHERE producto_id = ?";
            PreparedStatement psCheckProd = cn.prepareStatement(sqlCheckProd);
            psCheckProd.setInt(1, productoSeleccionadoId);
            ResultSet rsCheckProd = psCheckProd.executeQuery();
            
            if (rsCheckProd.next()) {
                String sqlUpdateProd = "UPDATE inventario_producto SET stock_disponible = stock_disponible + ? WHERE producto_id = ?";
                PreparedStatement psUpdateProd = cn.prepareStatement(sqlUpdateProd);
                psUpdateProd.setInt(1, unidadesProducidas);
                psUpdateProd.setInt(2, productoSeleccionadoId);
                psUpdateProd.executeUpdate();
                psUpdateProd.close();
            } else {
                String sqlInsertProd = "INSERT INTO inventario_producto (producto_id, stock_disponible, stock_minimo) VALUES (?, ?, 5)";
                PreparedStatement psInsert = cn.prepareStatement(sqlInsertProd);
                psInsert.setInt(1, productoSeleccionadoId);
                psInsert.setInt(2, unidadesProducidas);
                psInsert.executeUpdate();
                psInsert.close();
            }
            rsCheckProd.close();
            psCheckProd.close();

            // Confirmar y aplicar todos los cambios a la base de datos
            cn.commit(); 
            cn.setAutoCommit(true);
            
            // CORRECCIÓN DE MENSAJE FINAL
            JOptionPane.showMessageDialog(this, "Producción de " + unidadesProducidas + " unidades completada satisfactoriamente.", "Producción Exitosa", JOptionPane.INFORMATION_MESSAGE);
            spnUnidades.setValue(rendimientoReceta); // Resetear spinner

        } catch (SQLException e) {
            try { cn.rollback(); cn.setAutoCommit(true); } catch(Exception ex) {}
            JOptionPane.showMessageDialog(this, "Error durante el registro: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void crearInterfazGrafica() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        jPanelFondo = new JPanel(new BorderLayout());
        jPanelFondo.setBackground(new Color(245, 230, 211));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 244, 228));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Área de Producción - Repostero");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(255, 102, 51));

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(255, 51, 51));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.addActionListener(e -> {
            new Login().setVisible(true);
            this.dispose();
        });

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(btnCerrarSesion, BorderLayout.EAST);
        jPanelFondo.add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // PESTAÑA 1
        JPanel panelProduccion = new JPanel(new BorderLayout(10, 10));
        panelProduccion.setBackground(new Color(245, 230, 211));
        panelProduccion.setBorder(new EmptyBorder(15, 15, 15, 15));

        modRecetas = new DefaultTableModel(new String[]{"ID", "ID Prod", "Receta", "Rendimiento", "Tiempo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaRecetas = new JTable(modRecetas);
        tablaRecetas.removeColumn(tablaRecetas.getColumnModel().getColumn(1)); 
        JScrollPane scrollRecetas = new JScrollPane(tablaRecetas);
        scrollRecetas.setBorder(BorderFactory.createTitledBorder("Recetas Disponibles"));
        scrollRecetas.setPreferredSize(new Dimension(400, 0));

        JPanel panelDetalle = new JPanel(new BorderLayout(5, 5));
        panelDetalle.setBackground(new Color(255, 244, 228));
        
        lblRecetaInfo = new JLabel("Seleccione una receta de la lista...");
        lblRecetaInfo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblRecetaInfo.setBorder(new EmptyBorder(10, 10, 10, 10));

        modIngredientes = new DefaultTableModel(new String[]{"Ingrediente", "Cantidad Necesaria"}, 0);
        tablaIngredientes = new JTable(modIngredientes);
        JScrollPane scrollIngredientes = new JScrollPane(tablaIngredientes);
        scrollIngredientes.setBorder(BorderFactory.createTitledBorder("Ingredientes (Para rendimiento base)"));

        modPasos = new DefaultTableModel(new String[]{"Paso", "Instrucción"}, 0);
        tablaPasos = new JTable(modPasos);
        JScrollPane scrollPasos = new JScrollPane(tablaPasos);
        scrollPasos.setBorder(BorderFactory.createTitledBorder("Preparación"));

        JPanel panelCentroTablas = new JPanel(new GridLayout(2, 1, 5, 5));
        panelCentroTablas.add(scrollIngredientes);
        panelCentroTablas.add(scrollPasos);

        JPanel panelAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelAccion.setBackground(new Color(255, 244, 228));
        panelAccion.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblPregunta = new JLabel("¿Cuántas unidades has producido?: ");
        lblPregunta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spnUnidades = new JSpinner();
        spnUnidades.setPreferredSize(new Dimension(80, 30));
        
        btnRegistrar = new JButton("Registrar Producción en Inventario");
        btnRegistrar.setBackground(new Color(0, 153, 51));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrar.addActionListener(e -> registrarProduccion());

        panelAccion.add(lblPregunta);
        panelAccion.add(spnUnidades);
        panelAccion.add(Box.createHorizontalStrut(20));
        panelAccion.add(btnRegistrar);

        panelDetalle.add(lblRecetaInfo, BorderLayout.NORTH);
        panelDetalle.add(panelCentroTablas, BorderLayout.CENTER);
        panelDetalle.add(panelAccion, BorderLayout.SOUTH);

        panelProduccion.add(scrollRecetas, BorderLayout.WEST);
        panelProduccion.add(panelDetalle, BorderLayout.CENTER);

        // PESTAÑA 2
        JPanel panelPedidos = new JPanel(new BorderLayout());
        panelPedidos.setBackground(new Color(245, 230, 211));
        panelPedidos.setBorder(new EmptyBorder(15, 15, 15, 15));

        modPedidos = new DefaultTableModel(new String[]{"ID Venta", "Producto Solicitado", "Cantidad", "Fecha/Hora de Entrega", "Estado"}, 0);
        tablaPedidos = new JTable(modPedidos);
        JScrollPane scrollPedidos = new JScrollPane(tablaPedidos);
        scrollPedidos.setBorder(BorderFactory.createTitledBorder("Agendados (Pendientes y En Preparación)"));

        JButton btnRefrescar = new JButton("Actualizar Pedidos");
        btnRefrescar.addActionListener(e -> cargarPedidosDelDia());
        JPanel pnlRefrescar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlRefrescar.setBackground(new Color(245, 230, 211));
        pnlRefrescar.add(btnRefrescar);

        panelPedidos.add(scrollPedidos, BorderLayout.CENTER);
        panelPedidos.add(pnlRefrescar, BorderLayout.NORTH);

        tabbedPane.addTab("📋 Recetas y Producción", panelProduccion);
        tabbedPane.addTab("⏰ Pedidos Pendientes", panelPedidos); 

        jPanelFondo.add(tabbedPane, BorderLayout.CENTER);
        
        setContentPane(jPanelFondo);
        pack();
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName()) || "Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Repostero.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Repostero().setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(Repostero.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // =========================================================================
    // CLASE DE APOYO INTERNA PARA LA LÓGICA DE DESCUENTO
    // =========================================================================
    class IngredienteRequerido {
        int idIngrediente;
        double cantidadNecesaria;

        public IngredienteRequerido(int idIngrediente, double cantidadNecesaria) {
            this.idIngrediente = idIngrediente;
            this.cantidadNecesaria = cantidadNecesaria;
        }
    }
}