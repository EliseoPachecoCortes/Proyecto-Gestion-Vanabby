package loginvanabby;


import conexion.ConexionBD;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Interfaz principal del Gerente - Repostería Vanabby 
 * Navegación por Dashboard, SKU Alfanumérico Autogenerado y Botones de Retorno.
 */
public class Gerente extends javax.swing.JFrame {

    private Connection cn;
    private JPanel panelContenido;

    public Gerente() throws SQLException {
        this.cn = ConexionBD.getConexion();
        initComponents();
        setTitle("Gerente - Repostería Vanabby");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        verificarAlertas(); 
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(245, 230, 211));

        // ── ENCABEZADO GLOBAL SUPERIOR ──
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(255, 244, 228));
        panelHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Logo y Título
        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLogo.setOpaque(false);
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png"));
            Image logoImg = logoIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            pnlLogo.add(new JLabel(new ImageIcon(logoImg)));
        } catch (Exception e) {}
        
        JLabel lblTituloGeneral = new JLabel("Repostería Vanabby - Panel de Gerencia");
        lblTituloGeneral.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloGeneral.setForeground(new Color(180, 80, 0));
        pnlLogo.add(lblTituloGeneral);

        // Botón Cerrar Sesión
        JButton btnCerrar = new JButton("Cerrar Sesión");
        btnCerrar.setBackground(new Color(220, 50, 50));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> {
            new Login().setVisible(true);
            this.dispose();
        });

        panelHeader.add(pnlLogo, BorderLayout.WEST);
        panelHeader.add(btnCerrar, BorderLayout.EAST);

        // ── Panel contenido (centro) ──
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(new Color(245, 230, 211));

        mostrarInicio(); // Carga los 4 recuadros

        panelPrincipal.add(panelHeader, BorderLayout.NORTH);
        panelPrincipal.add(panelContenido, BorderLayout.CENTER);
        setContentPane(panelPrincipal);
        pack();
    }

    // =====================================================================
    // PANEL DE NAVEGACIÓN DENTRO DE LOS MÓDULOS (BOTÓN ATRÁS)
    // =====================================================================
    private JPanel crearHeaderModulo(String tituloTexto) {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(245, 230, 211));
        
        JLabel lblTitulo = new JLabel(tituloTexto);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(new Color(0, 102, 51));
        lblTitulo.setBorder(new EmptyBorder(15, 20, 15, 0));
        
        JButton btnVolver = crearBotonAccion("⬅ Volver al Menú Principal", new Color(220, 50, 50));
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.addActionListener(e -> mostrarInicio()); // ACCIÓN DE IR ATRÁS
        
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        pnlBtn.setOpaque(false);
        pnlBtn.add(btnVolver);
        
        pnlHeader.add(lblTitulo, BorderLayout.WEST);
        pnlHeader.add(pnlBtn, BorderLayout.EAST);
        
        return pnlHeader;
    }

    // =====================================================================
    // PANEL INICIO (DASHBOARD CON LOS RECUADROS COMO BOTONES)
    // =====================================================================
    private void mostrarInicio() {
        panelContenido.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 230, 211));
        panel.setBorder(new EmptyBorder(40, 80, 40, 80));

        JLabel titulo = new JLabel("Panel de Control Principal");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titulo.setForeground(new Color(0, 153, 0));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Cuadrícula 2x2 para los 4 botones
        JPanel panelTarjetas = new JPanel(new GridLayout(2, 2, 30, 30));
        panelTarjetas.setBackground(new Color(245, 230, 211));

        panelTarjetas.add(crearTarjetaBoton("🧁", "Catálogo de Productos", "Añadir, editar y fijar precios", "catalogo"));
        panelTarjetas.add(crearTarjetaBoton("📦", "Inventario Materia Prima", "Entradas, Ajustes y FEFO", "inventario"));
        panelTarjetas.add(crearTarjetaBoton("⚠️", "Alertas de Stock", "Avisos de productos por agotarse", "alertas"));
        panelTarjetas.add(crearTarjetaBoton("📊", "Reportes Inteligentes", "Ventas y consumos reales", "reportes"));

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(panelTarjetas, BorderLayout.CENTER);

        panelContenido.add(panel);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    // CREA UN BOTÓN GIGANTE QUE PARECE UNA TARJETA
    private JButton crearTarjetaBoton(String icono, String titulo, String desc, String accion) {
        JButton btnCard = new JButton();
        btnCard.setLayout(new BoxLayout(btnCard, BoxLayout.Y_AXIS));
        btnCard.setBackground(new Color(255, 244, 228));
        btnCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 60), 3), 
                new EmptyBorder(20, 20, 20, 20)));
        btnCard.setFocusPainted(false);
        btnCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(80, 40, 0));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblDesc = new JLabel("<html><center>" + desc + "</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDesc.setForeground(Color.DARK_GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnCard.add(Box.createVerticalGlue());
        btnCard.add(lblIcono); 
        btnCard.add(Box.createVerticalStrut(15));
        btnCard.add(lblTitulo); 
        btnCard.add(Box.createVerticalStrut(10));
        btnCard.add(lblDesc);
        btnCard.add(Box.createVerticalGlue());

        // Evento Click
        btnCard.addActionListener(e -> {
            switch (accion) {
                case "catalogo" -> mostrarCatalogo();
                case "inventario" -> mostrarInventario();
                case "alertas" -> mostrarAlertas();
                case "reportes" -> mostrarReportes();
            }
        });

        // Efecto Hover (Cambia de color al pasar el mouse)
        btnCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnCard.setBackground(new Color(255, 230, 200)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnCard.setBackground(new Color(255, 244, 228)); }
        });

        return btnCard;
    }

    // =====================================================================
    // CATÁLOGO DE PRODUCTOS (CON GENERADOR DE SKU)
    // =====================================================================
    private void mostrarCatalogo() {
        panelContenido.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 230, 211));
        
        // AGREGA EL ENCABEZADO CON BOTON ATRÁS
        panel.add(crearHeaderModulo("🧁 Gestión del Catálogo de Productos"), BorderLayout.NORTH);

        DefaultTableModel modeloCatalogo = new DefaultTableModel(new String[]{"SKU", "Nombre", "Descripción", "Categoría", "Precio", "Stock"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloCatalogo);
        tabla.setRowHeight(28);
        tabla.getTableHeader().setBackground(new Color(255, 200, 150));
        cargarCatalogo(modeloCatalogo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotones.setBackground(new Color(245, 230, 211));
        JButton btnNuevo = crearBotonAccion("Nuevo Producto", new Color(0, 160, 60));
        JButton btnEditar = crearBotonAccion("Editar", new Color(200, 140, 0));
        JButton btnEliminar = crearBotonAccion("Eliminar", new Color(200, 50, 50));
        
        btnNuevo.addActionListener(e -> dialogoProducto(null, modeloCatalogo));
        btnEditar.addActionListener(e -> {
            if (tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }
            dialogoProducto(tabla, modeloCatalogo);
        });
        btnEliminar.addActionListener(e -> {
            if (tabla.getSelectedRow() >= 0) {
                String sku = tabla.getValueAt(tabla.getSelectedRow(), 0).toString();
                if (JOptionPane.showConfirmDialog(this, "¿Eliminar producto SKU: " + sku + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        PreparedStatement ps = cn.prepareStatement("DELETE FROM producto WHERE sku = ?");
                        ps.setString(1, sku); ps.executeUpdate(); ps.close();
                        cargarCatalogo(modeloCatalogo);
                    } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage()); }
                }
            }
        });

        panelBotones.add(btnNuevo); panelBotones.add(btnEditar); panelBotones.add(btnEliminar);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        panelContenido.add(panel); panelContenido.revalidate(); panelContenido.repaint();
    }

    private void cargarCatalogo(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        try {
            String sql = "SELECT p.sku, p.nombre, p.descripcion, c.nombre_categoria, p.precio, "
                    + "COALESCE(i.stock_disponible,0) AS stock "
                    + "FROM producto p LEFT JOIN categoria c ON p.categoria_id = c.categoria_id "
                    + "LEFT JOIN inventario_producto i ON p.producto_id = i.producto_id ORDER BY p.nombre";
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("sku"), 
                    rs.getString("nombre"), 
                    rs.getString("descripcion"), 
                    rs.getString("nombre_categoria"), 
                    "$" + rs.getString("precio"), 
                    rs.getString("stock")
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {}
    }

    // GENERADOR DE SKU ALFANUMÉRICO AUTOMÁTICO
    private String generarSKU() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sku = new StringBuilder("VN-");
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            sku.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sku.toString();
    }

    private void dialogoProducto(JTable tabla, DefaultTableModel modelo) {
        JDialog dlg = new JDialog(this, tabla == null ? "Nuevo Producto" : "Editar Producto", true);
        dlg.setSize(520, 460); 
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        dlg.getContentPane().setBackground(new Color(255, 244, 228));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtSKU = new JTextField(15);
        txtSKU.setEditable(false); // Siempre bloqueado porque lo genera el sistema
        
        JTextField txtNombre = new JTextField(15);
        JTextField txtDesc = new JTextField(15);
        JTextField txtPrecio = new JTextField(15);
        JTextField txtTiempo = new JTextField(15);
        JTextField txtStock = new JTextField(15); 
        JComboBox<String> cboCat = new JComboBox<>();

        JTextField txtImagen = new JTextField(10);
        txtImagen.setEditable(false); 
        JButton btnImg = new JButton("Buscar");
        btnImg.setBackground(new Color(210, 140, 100)); btnImg.setForeground(Color.WHITE);
        JPanel pnlImagen = new JPanel(new BorderLayout(5, 0)); pnlImagen.setOpaque(false);
        pnlImagen.add(txtImagen, BorderLayout.CENTER); pnlImagen.add(btnImg, BorderLayout.EAST);

        btnImg.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                txtImagen.setText(file.getName());
                try {
                    java.io.File dest = new java.io.File(new java.io.File("imagenes"), file.getName());
                    java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ex) {}
            }
        });

        try {
            ResultSet rs = cn.prepareStatement("SELECT nombre_categoria FROM categoria ORDER BY nombre_categoria").executeQuery();
            while (rs.next()) cboCat.addItem(rs.getString("nombre_categoria"));
        } catch (Exception e) {}

        if (tabla != null) {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                txtSKU.setText(tabla.getValueAt(fila, 0).toString());
                txtNombre.setText(tabla.getValueAt(fila, 1).toString());
                txtDesc.setText(tabla.getValueAt(fila, 2).toString());
                cboCat.setSelectedItem(tabla.getValueAt(fila, 3).toString());
                txtPrecio.setText(tabla.getValueAt(fila, 4).toString().replace("$", ""));
                txtStock.setText(tabla.getValueAt(fila, 5).toString()); 
                try {
                    PreparedStatement psImg = cn.prepareStatement("SELECT imagen FROM producto WHERE sku = ?");
                    psImg.setString(1, txtSKU.getText()); ResultSet rsImg = psImg.executeQuery();
                    if(rsImg.next()) txtImagen.setText(rsImg.getString("imagen"));
                } catch(Exception ignored){}
            }
        } else {
            // INYECTA EL SKU AUTOMÁTICAMENTE
            txtSKU.setText(generarSKU());
        }

        Component[] controles = {txtSKU, txtNombre, txtDesc, cboCat, txtPrecio, txtTiempo, txtStock, pnlImagen};
        String[] labels = {"SKU:", "Nombre:", "Descripción:", "Categoría:", "Precio ($):", "Tiempo prep (min):", "Stock (Unidades):", "Imagen:"};

        for (int i = 0; i < controles.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            dlg.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            dlg.add(controles[i], gbc);
        }

        JButton btnGuardar = crearBotonAccion("Guardar", new Color(0, 160, 60));
        btnGuardar.addActionListener(e -> {
            try {
                if (txtNombre.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "El Nombre y el Precio son obligatorios."); return;
                }

                String skuFinal = txtSKU.getText().trim();
                String nombre = txtNombre.getText().trim();
                String desc = txtDesc.getText().trim();
                String cat = cboCat.getSelectedItem() != null ? cboCat.getSelectedItem().toString() : "";
                String precio = txtPrecio.getText().trim();
                String tiempo = txtTiempo.getText().trim();
                String stockStr = txtStock.getText().trim(); 
                String imagen = txtImagen.getText().trim();
                
                int stockVal = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);

                PreparedStatement psCat = cn.prepareStatement("SELECT categoria_id FROM categoria WHERE nombre_categoria = ?");
                psCat.setString(1, cat);
                ResultSet rsCat = psCat.executeQuery(); int catId = rsCat.next() ? rsCat.getInt(1) : 1;
                rsCat.close(); psCat.close();

                if (tabla == null) {
                    PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO producto (sku, nombre, descripcion, categoria_id, precio, tiempo_preparacion_min, imagen) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps.setString(1, skuFinal); 
                    ps.setString(2, nombre);
                    ps.setString(3, desc); 
                    ps.setInt(4, catId);
                    ps.setDouble(5, Double.parseDouble(precio));
                    ps.setInt(6, tiempo.isEmpty() ? 0 : Integer.parseInt(tiempo));
                    ps.setString(7, imagen.isEmpty() ? null : imagen);
                    ps.executeUpdate();
                    ps.close();
                } else {
                    PreparedStatement ps = cn.prepareStatement(
                            "UPDATE producto SET nombre=?, descripcion=?, categoria_id=?, precio=?, tiempo_preparacion_min=?, imagen=? WHERE sku=?");
                    ps.setString(1, nombre); ps.setString(2, desc);
                    ps.setInt(3, catId); ps.setDouble(4, Double.parseDouble(precio));
                    ps.setInt(5, tiempo.isEmpty() ? 0 : Integer.parseInt(tiempo));
                    ps.setString(6, imagen.isEmpty() ? null : imagen);
                    ps.setString(7, skuFinal);
                    ps.executeUpdate();
                    ps.close();
                }
                
                // Actualizar inventario de productos
                PreparedStatement psId = cn.prepareStatement("SELECT producto_id FROM producto WHERE sku = ?");
                psId.setString(1, skuFinal);
                ResultSet rsId = psId.executeQuery();
                if (rsId.next()) {
                    int prodId = rsId.getInt(1);
                    PreparedStatement psChk = cn.prepareStatement("SELECT producto_id FROM inventario_producto WHERE producto_id = ?");
                    psChk.setInt(1, prodId);
                    if (psChk.executeQuery().next()) {
                        PreparedStatement pUp = cn.prepareStatement("UPDATE inventario_producto SET stock_disponible = ? WHERE producto_id = ?");
                        pUp.setInt(1, stockVal); pUp.setInt(2, prodId); pUp.executeUpdate();
                    } else {
                        PreparedStatement pIn = cn.prepareStatement("INSERT INTO inventario_producto (producto_id, stock_disponible) VALUES (?, ?)");
                        pIn.setInt(1, prodId); pIn.setInt(2, stockVal); pIn.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(dlg, "Producto guardado correctamente.");
                cargarCatalogo(modelo); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Error BD: " + ex.getMessage()); }
        });

        gbc.gridx = 0; gbc.gridy = controles.length; gbc.gridwidth = 2;
        dlg.add(btnGuardar, gbc); dlg.setVisible(true);
    }

    // =====================================================================
    // HU-008: INVENTARIO (AJUSTE AUTOMATIZADO Y FEFO ESTRICTO)
    // =====================================================================
    private void mostrarInventario() {
        panelContenido.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 230, 211));
        
        // AGREGA EL ENCABEZADO CON BOTON ATRÁS
        panel.add(crearHeaderModulo("📦 Gestión de Inventario (FEFO)"), BorderLayout.NORTH);

        DefaultTableModel modeloInv = new DefaultTableModel(new String[]{"ID", "Ingrediente", "Stock Físico", "Unidad", "Lote a caducar", "Estado"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloInv);
        tabla.setRowHeight(28); tabla.getTableHeader().setBackground(new Color(255, 200, 150));
        cargarInventario(modeloInv);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotones.setBackground(new Color(245, 230, 211));

        JButton btnEntrada = crearBotonAccion("Entrada (Registrar Lote)", new Color(0, 160, 60));
        JButton btnAjuste = crearBotonAccion("Ajuste (Corrección / Merma)", new Color(200, 140, 0));
        JButton btnNuevo = crearBotonAccion("Nuevo Ingrediente", new Color(0, 100, 180));
        
        btnEntrada.addActionListener(e -> {
            if (tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Selecciona un ingrediente."); return; }
            dialogoMovimiento(tabla, modeloInv, "ENTRADA");
        });
        btnAjuste.addActionListener(e -> {
            if (tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Selecciona un ingrediente."); return; }
            dialogoMovimiento(tabla, modeloInv, "AJUSTE");
        });
        btnNuevo.addActionListener(e -> dialogoNuevoIngrediente(modeloInv));

        panelBotones.add(btnEntrada); panelBotones.add(btnAjuste); panelBotones.add(btnNuevo);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        panelContenido.add(panel); panelContenido.revalidate(); panelContenido.repaint();
    }

    private void cargarInventario(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        try {
            String sql = "SELECT i.ingrediente_id, i.nombre, COALESCE(SUM(l.cantidad), 0) AS stock_total, um.simbolo AS unidad, "
                       + "MIN(l.fecha_caducidad) AS caducidad FROM ingrediente i "
                       + "JOIN unidad_medida um ON i.unidad_medida_id = um.unidad_medida_id "
                       + "LEFT JOIN lote_inventario l ON i.ingrediente_id = l.ingrediente_id GROUP BY i.ingrediente_id, i.nombre, um.simbolo ORDER BY i.nombre";
            ResultSet rs = cn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                double stock = rs.getDouble("stock_total");
                modelo.addRow(new Object[]{rs.getInt("ingrediente_id"), rs.getString("nombre"), stock, rs.getString("unidad"), rs.getString("caducidad") != null ? rs.getString("caducidad") : "Sin lote", stock <= 0 ? "Agotado" : "OK"});
            }
        } catch (Exception e) {}
    }

    private void dialogoMovimiento(JTable tabla, DefaultTableModel modelo, String tipo) {
        int fila = tabla.getSelectedRow();
        String ingrediente = tabla.getValueAt(fila, 1).toString();
        int ingId = Integer.parseInt(tabla.getValueAt(fila, 0).toString());

        if (tipo.equals("AJUSTE")) {
            double stockActual = Double.parseDouble(tabla.getValueAt(fila, 2).toString());
            
            String cantStr = JOptionPane.showInputDialog(this, "Stock reportado por el sistema: " + stockActual + "\n\nIngresa la cantidad REAL que hay en físico ahora mismo:", "Ajuste de Inventario", JOptionPane.QUESTION_MESSAGE);
            if (cantStr == null || cantStr.trim().isEmpty()) return;

            try {
                double stockFisico = Double.parseDouble(cantStr.trim());
                double diferencia = stockFisico - stockActual; 
                
                if (diferencia == 0) { JOptionPane.showMessageDialog(this, "El stock ya cuadra. No hay necesidad de ajuste."); return; }

                String motivo = JOptionPane.showInputDialog(this, "El sistema detectó una diferencia de " + diferencia + ".\n\nEscribe el motivo del ajuste (Ej: 'Merma', 'Error anterior'):", "Motivo", JOptionPane.QUESTION_MESSAGE);
                if (motivo == null || motivo.trim().isEmpty()) return;

                String numLote = "AJUSTE-" + System.currentTimeMillis();
                PreparedStatement psIns = cn.prepareStatement(
                        "INSERT INTO lote_inventario (ingrediente_id, numero_lote, cantidad, unidad_medida_id, fecha_caducidad) "
                        + "SELECT ?, ?, ?, unidad_medida_id, CURRENT_DATE FROM ingrediente WHERE ingrediente_id = ?");
                psIns.setInt(1, ingId);
                psIns.setString(2, numLote);
                psIns.setDouble(3, diferencia); 
                psIns.setInt(4, ingId);
                psIns.executeUpdate();
                psIns.close();

                JOptionPane.showMessageDialog(this, "✅ Ajuste completado. El sistema ha cuadrado el stock a " + stockFisico);
                cargarInventario(modelo);

            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Por favor ingrese números válidos."); }
        } else {
            // ENTRADA - Validar fecha de caducidad
            JPanel panelEntrada = new JPanel(new GridLayout(3, 2, 5, 10));
            JTextField txtCant = new JTextField();
            JTextField txtFecha = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            panelEntrada.add(new JLabel("Cantidad a ingresar:")); panelEntrada.add(txtCant);
            panelEntrada.add(new JLabel("Fecha Caducidad (AAAA-MM-DD):")); panelEntrada.add(txtFecha);

            int result = JOptionPane.showConfirmDialog(null, panelEntrada, "Entrada de Lote - FEFO", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String caducidad = txtFecha.getText().trim();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false); // Validacion estricta de fecha
                    java.util.Date fechaIngresada = sdf.parse(caducidad);
                    
                    java.util.Calendar calHoy = java.util.Calendar.getInstance();
                    calHoy.set(java.util.Calendar.HOUR_OF_DAY, 0); calHoy.set(java.util.Calendar.MINUTE, 0);
                    calHoy.set(java.util.Calendar.SECOND, 0); calHoy.set(java.util.Calendar.MILLISECOND, 0);
                    
                    if (fechaIngresada.before(calHoy.getTime())) {
                        JOptionPane.showMessageDialog(this, "¡Error! La fecha de caducidad no puede ser en el pasado.", "Fecha Inválida", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double cant = Double.parseDouble(txtCant.getText().trim());
                    PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO lote_inventario (ingrediente_id, numero_lote, cantidad, unidad_medida_id, fecha_caducidad) "
                        + "SELECT ?, ?, ?, unidad_medida_id, ? FROM ingrediente WHERE ingrediente_id = ?");
                    ps.setInt(1, ingId); ps.setString(2, "LOTE-" + System.currentTimeMillis()); ps.setDouble(3, cant); ps.setString(4, caducidad); ps.setInt(5, ingId);
                    ps.executeUpdate(); ps.close();
                    
                    JOptionPane.showMessageDialog(this, "✅ Entrada registrada con caducidad FEFO.");
                    cargarInventario(modelo);
                } catch (java.text.ParseException pe) {
                    JOptionPane.showMessageDialog(this, "Formato de fecha inválido o fecha inexistente.\nUsa el formato AAAA-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) { 
                    JOptionPane.showMessageDialog(this, "Error en los datos ingresados."); 
                }
            }
        }
    }

    private void dialogoNuevoIngrediente(DefaultTableModel modelo) {
        JDialog dlg = new JDialog(this, "Nuevo Ingrediente", true);
        dlg.setSize(380, 250); dlg.setLocationRelativeTo(this); dlg.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNombre = new JTextField(15); JTextField txtStock = new JTextField(15);
        JTextField txtUnidad = new JTextField(15); JTextField txtMinimo = new JTextField(15);

        String[][] campos = {{"Nombre:", null}, {"Stock inicial (opc):", null}, {"Unidad (kg, g, l, pza):", null}, {"Costo/unidad ($):", null}};
        JTextField[] controles = {txtNombre, txtStock, txtUnidad, txtMinimo};

        for (int i = 0; i < controles.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.4; dlg.add(new JLabel(campos[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.6; dlg.add(controles[i], gbc);
        }

        JButton btnGuardar = crearBotonAccion("💾 Guardar", new Color(0, 160, 60));
        btnGuardar.addActionListener(e -> {
            try {
                PreparedStatement psUm = cn.prepareStatement("SELECT unidad_medida_id FROM unidad_medida WHERE simbolo = ?");
                psUm.setString(1, txtUnidad.getText().trim());
                ResultSet rsUm = psUm.executeQuery();
                if (!rsUm.next()) { JOptionPane.showMessageDialog(dlg, "Unidad no encontrada. Usa: kg, g, l, ml, pza"); return; }
                int umId = rsUm.getInt(1);

                PreparedStatement ps = cn.prepareStatement("INSERT INTO ingrediente (nombre, unidad_medida_id, costo_por_unidad) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, txtNombre.getText()); ps.setInt(2, umId); ps.setDouble(3, txtMinimo.getText().isEmpty() ? 0 : Double.parseDouble(txtMinimo.getText()));
                ps.executeUpdate();
                
                ResultSet rsKeys = ps.getGeneratedKeys();
                if(rsKeys.next() && !txtStock.getText().isEmpty()){
                    PreparedStatement pL = cn.prepareStatement("INSERT INTO lote_inventario (ingrediente_id, numero_lote, cantidad, unidad_medida_id, fecha_caducidad) VALUES (?, 'INICIAL', ?, ?, CURRENT_DATE)");
                    pL.setInt(1, rsKeys.getInt(1)); pL.setDouble(2, Double.parseDouble(txtStock.getText())); pL.setInt(3, umId); pL.executeUpdate();
                }
                JOptionPane.showMessageDialog(dlg, "Ingrediente agregado."); cargarInventario(modelo); dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage()); }
        });
        gbc.gridx = 0; gbc.gridy = controles.length; gbc.gridwidth = 2; dlg.add(btnGuardar, gbc); dlg.setVisible(true);
    }

    // =====================================================================
    // HU-009: ALERTAS
    // =====================================================================
    private void verificarAlertas() {
        try {
            ResultSet rs = cn.prepareStatement("SELECT nombre FROM ingrediente i LEFT JOIN lote_inventario l ON i.ingrediente_id = l.ingrediente_id GROUP BY i.ingrediente_id HAVING COALESCE(SUM(l.cantidad),0) < 5").executeQuery();
            int count = 0; while (rs.next()) count++;
            if (count > 0) JOptionPane.showMessageDialog(this, "¡ALERTA!: Hay " + count + " ingredientes con stock por debajo del umbral mínimo de seguridad (< 5).", "Alerta de Stock", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {}
    }

    private void mostrarAlertas() {
        panelContenido.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 230, 211));
        
        // AGREGA EL ENCABEZADO CON BOTON ATRÁS
        panel.add(crearHeaderModulo("⚠️ Alertas de Stock de Ingredientes"), BorderLayout.NORTH);

        DefaultTableModel modeloAlertas = new DefaultTableModel(new String[]{"Ingrediente", "Stock Actual", "Estado"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloAlertas);
        tabla.setRowHeight(28); tabla.getTableHeader().setBackground(new Color(255, 180, 100));

        try {
            ResultSet rs = cn.prepareStatement("SELECT i.nombre, COALESCE(SUM(l.cantidad),0) AS stock_total, um.simbolo FROM ingrediente i JOIN unidad_medida um ON i.unidad_medida_id = um.unidad_medida_id LEFT JOIN lote_inventario l ON i.ingrediente_id = l.ingrediente_id GROUP BY i.ingrediente_id HAVING stock_total < 5 ORDER BY stock_total ASC").executeQuery();
            while (rs.next()) modeloAlertas.addRow(new Object[]{rs.getString("nombre"), rs.getDouble("stock_total") + " " + rs.getString("simbolo"), rs.getDouble("stock_total") == 0 ? "Agotado" : "Por Agotarse"});
        } catch (Exception e) {}

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panelContenido.add(panel); panelContenido.revalidate(); panelContenido.repaint();
    }

    // =====================================================================
    // HU-012: REPORTES 
    // =====================================================================
    private void mostrarReportes() {
        panelContenido.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 230, 211));
        
        // ENCABEZADO CON BOTÓN DE VOLVER AL MENÚ
        panel.add(crearHeaderModulo("📊 Reportes Inteligentes"), BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFiltros.setBackground(new Color(255, 244, 228));

        JComboBox<String> cboTipo = new JComboBox<>(new String[]{"Ventas por periodo", "Consumo REAL de Ingredientes"});
        JTextField txtFI = new JTextField("2025-01-01", 10);
        JTextField txtFF = new JTextField(java.time.LocalDate.now().toString(), 10);

        panelFiltros.add(new JLabel("Tipo:")); panelFiltros.add(cboTipo);
        panelFiltros.add(new JLabel("Desde (YYYY-MM-DD):")); panelFiltros.add(txtFI);
        panelFiltros.add(new JLabel("Hasta:")); panelFiltros.add(txtFF);

        DefaultTableModel modelo = new DefaultTableModel() { public boolean isCellEditable(int r, int c) { return false; }};
        JTable tabla = new JTable(modelo); tabla.setRowHeight(26); tabla.getTableHeader().setBackground(new Color(255, 200, 150));

        JButton btnGenerar = crearBotonAccion("Generar Reporte", new Color(0, 120, 180));
        btnGenerar.addActionListener(e -> {
            modelo.setRowCount(0); modelo.setColumnCount(0);
            try {
                if (cboTipo.getSelectedIndex() == 0) {
                    modelo.addColumn("Fecha"); modelo.addColumn("Total Ventas ($)");
                    PreparedStatement ps = cn.prepareStatement("SELECT DATE(fecha_venta), SUM(total) FROM venta WHERE DATE(fecha_venta) BETWEEN ? AND ? GROUP BY DATE(fecha_venta)");
                    ps.setString(1, txtFI.getText()); ps.setString(2, txtFF.getText());
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) modelo.addRow(new Object[]{rs.getString(1), "$" + rs.getString(2)});
                } else {
                    modelo.addColumn("Ingrediente"); modelo.addColumn("Cantidad Total Consumida");
                    String sql = "SELECT i.nombre, SUM(dv.cantidad * ri.cantidad) AS consumido, um.simbolo "
                               + "FROM venta v JOIN detalle_venta dv ON v.venta_id = dv.venta_id "
                               + "JOIN receta r ON dv.producto_id = r.producto_id "
                               + "JOIN receta_ingrediente ri ON r.receta_id = ri.receta_id "
                               + "JOIN ingrediente i ON ri.ingrediente_id = i.ingrediente_id "
                               + "JOIN unidad_medida um ON i.unidad_medida_id = um.unidad_medida_id "
                               + "WHERE DATE(v.fecha_venta) BETWEEN ? AND ? GROUP BY i.nombre, um.simbolo ORDER BY consumido DESC";
                    PreparedStatement ps = cn.prepareStatement(sql);
                    ps.setString(1, txtFI.getText()); ps.setString(2, txtFF.getText());
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) modelo.addRow(new Object[]{rs.getString(1), rs.getString(2) + " " + rs.getString(3)});
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage()); }
        });

        JButton btnExportar = crearBotonAccion("Exportar CSV", new Color(0, 160, 60));
        btnExportar.addActionListener(e -> {
            if (modelo.getRowCount() == 0) return;
            exportarCSV(modelo, cboTipo.getSelectedItem().toString());
        });

        panelFiltros.add(btnGenerar); panelFiltros.add(btnExportar);
        
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(panelFiltros, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        panel.add(centro, BorderLayout.CENTER);
        panelContenido.add(panel); panelContenido.revalidate(); panelContenido.repaint();
    }

    private void exportarCSV(DefaultTableModel modelo, String nombre) {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(nombre.replace(" ", "_") + ".csv"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile(), "UTF-8");
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    pw.print(modelo.getColumnName(i)); if (i < modelo.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
                for (int r = 0; r < modelo.getRowCount(); r++) {
                    for (int c = 0; c < modelo.getColumnCount(); c++) {
                        pw.print(modelo.getValueAt(r, c)); if (c < modelo.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                pw.close();
                JOptionPane.showMessageDialog(this, "✅ CSV exportado correctamente.");
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    // =====================================================================
    // UTILIDADES
    // =====================================================================
    private JButton crearBotonAccion(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new Gerente().setVisible(true); } catch (Exception ex) {}
        });
    }
}