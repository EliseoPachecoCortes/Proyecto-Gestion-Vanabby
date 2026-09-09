package loginvanabby;


import conexion.ConexionBD;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;

/**
 * Catálogo de Productos - Vanabby
 */
public class Catalogo extends javax.swing.JFrame {

    Connection cn;
    private Registro_Venta ventaPadre = null;

    public Catalogo() throws SQLException {
        this.cn = ConexionBD.getConexion();
        initComponents();
        cargarCatalogo();
        this.setSize(1150, 780);  // altura aumentada para mostrar todo
        this.setLocationRelativeTo(null);
        this.setTitle("Selección de Productos");
    }

    public Catalogo(Registro_Venta padre) throws SQLException {
        this.cn = ConexionBD.getConexion();
        this.ventaPadre = padre;
        initComponents();
        cargarCatalogo();
        this.setSize(1150, 780);  // altura aumentada para mostrar todo
        this.setLocationRelativeTo(null);
        this.setTitle("Catálogo de Productos - Seleccionar para venta");
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(null);
        panelPrincipal.setBackground(new Color(255, 244, 228));
        panelPrincipal.setPreferredSize(new Dimension(1150, 760));

        // ===== Sidebar =====
        JPanel sidebar = new JPanel(null);
        sidebar.setBackground(new Color(210, 140, 100));
        sidebar.setBounds(0, 0, 170, 760);

        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png")));
        logo.setBounds(5, 10, 160, 130);
        sidebar.add(logo);

        JLabel lblTitulo = new JLabel("CATÁLOGO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(0, 148, 170, 25);
        sidebar.add(lblTitulo);

        JButton btnVolver = crearBotonSide(" Volver", 25, 185);
        btnVolver.addActionListener(e -> dispose());
        sidebar.add(btnVolver);

        panelPrincipal.add(sidebar);

        // ===== Encabezado =====
        String tituloTexto = ventaPadre != null
                ? "Seleccionar Producto para Venta"
                : "Catálogo de Productos";
        JLabel lblEncabezado = new JLabel(tituloTexto);
        lblEncabezado.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblEncabezado.setForeground(new Color(180, 90, 40));
        lblEncabezado.setBounds(185, 10, 500, 35);
        panelPrincipal.add(lblEncabezado);

        // ===== Panel búsqueda =====
        JPanel panelBusqueda = new JPanel(null);
        panelBusqueda.setBackground(new Color(240, 220, 200));
        panelBusqueda.setBounds(175, 50, 960, 55);

        JLabel lblFiltro = new JLabel("Categoría:");
        lblFiltro.setBounds(10, 15, 75, 22);
        panelBusqueda.add(lblFiltro);

        cboCategorias = new JComboBox<>(new String[]{
            "Todos", "Pasteles", "Cupcakes", "Galletas", "Postres",
            "Tortas", "Tartas", "Panadería", "Otro"
        });
        cboCategorias.setBounds(88, 14, 130, 27);
        cboCategorias.addActionListener(e -> cargarCatalogo());
        panelBusqueda.add(cboCategorias);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setBounds(230, 15, 55, 22);
        panelBusqueda.add(lblBuscar);

        txtBuscar = new JTextField();
        txtBuscar.setBounds(285, 14, 200, 27);
        panelBusqueda.add(txtBuscar);

        JButton btnBuscar = crearBotonBusq("Buscar", 495, 12);
        btnBuscar.addActionListener(e -> buscarProducto());
        panelBusqueda.add(btnBuscar);

        JButton btnTodos = crearBotonBusq("Ver todos", 600, 12);
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cboCategorias.setSelectedIndex(0);
            cargarCatalogo();
        });
        panelBusqueda.add(btnTodos);

        panelPrincipal.add(panelBusqueda);

        // ===== Tabla productos =====
        tablaCatalogo = new JTable();
        tablaCatalogo.setRowHeight(24);
        tablaCatalogo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaCatalogo.setSelectionBackground(new Color(210, 160, 120));

        // ===== RENDERER: colorear columna Stock =====
        tablaCatalogo.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 6 && value != null) {
                    try {
                        int stock = Integer.parseInt(value.toString().trim());
                        if (!isSelected) {
                            if (stock == 0) {
                                c.setBackground(new Color(255, 180, 180));
                                c.setForeground(new Color(150, 0, 0));
                            } else if (stock <= 5) {
                                c.setBackground(new Color(255, 235, 150));
                                c.setForeground(new Color(130, 90, 0));
                            } else {
                                c.setBackground(new Color(200, 240, 200));
                                c.setForeground(new Color(0, 100, 0));
                            }
                        }
                        String etiqueta = stock == 0 ? " Sin stock" : (stock <= 5 ? " Poco" : " Disponible");
                        ((JLabel) c).setText(stock + etiqueta);
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    } catch (NumberFormatException ex) {
                        if (!isSelected) {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    }
                } else {
                    if (!isSelected) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tablaCatalogo);
        scroll.setBounds(175, 113, 600, 550);
        panelPrincipal.add(scroll);

        // ===== Panel detalle derecha =====
        JPanel panelDetalle = new JPanel(null);
        panelDetalle.setBackground(new Color(255, 235, 210));
        panelDetalle.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 140, 100), 2),
                "  Detalle  ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(180, 90, 40)
        ));
        panelDetalle.setBounds(785, 113, 350, 620);  // más alto para que entre todo

        // Imagen
        lblImagenPreview = new JLabel("Sin imagen", SwingConstants.CENTER);
        lblImagenPreview.setBounds(27, 22, 295, 160);
        lblImagenPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 150, 100), 2));
        lblImagenPreview.setBackground(new Color(245, 230, 215));
        lblImagenPreview.setOpaque(true);
        lblImagenPreview.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblImagenPreview.setForeground(Color.GRAY);
        panelDetalle.add(lblImagenPreview);

        // Nombre
        lblDetNombre = new JLabel("", SwingConstants.CENTER);
        lblDetNombre.setBounds(5, 188, 340, 24);
        lblDetNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDetNombre.setForeground(new Color(150, 70, 20));
        panelDetalle.add(lblDetNombre);

        // SKU
        lblDetSku = new JLabel("", SwingConstants.CENTER);
        lblDetSku.setBounds(5, 212, 340, 18);
        lblDetSku.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDetSku.setForeground(new Color(130, 100, 70));
        panelDetalle.add(lblDetSku);

        JSeparator sep = new JSeparator();
        sep.setBounds(15, 234, 320, 2);
        sep.setForeground(new Color(210, 150, 100));
        panelDetalle.add(sep);

        // Descripción
        JLabel lblTitDesc = new JLabel("Descripción:");
        lblTitDesc.setBounds(12, 240, 280, 18);
        lblTitDesc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitDesc.setForeground(new Color(130, 70, 20));
        panelDetalle.add(lblTitDesc);

        lblDetDescripcion = new JLabel("");
        lblDetDescripcion.setBounds(12, 258, 325, 35);
        lblDetDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDetDescripcion.setForeground(new Color(80, 50, 20));
        panelDetalle.add(lblDetDescripcion);

        // Categoría
        JLabel lblTitCat = new JLabel("Categoría:");
        lblTitCat.setBounds(12, 298, 100, 20);
        lblTitCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitCat.setForeground(new Color(130, 70, 20));
        panelDetalle.add(lblTitCat);
        lblDetCategoria = new JLabel("");
        lblDetCategoria.setBounds(118, 298, 200, 20);
        lblDetCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelDetalle.add(lblDetCategoria);

        // Precio
        JLabel lblTitPrecio = new JLabel("Precio:");
        lblTitPrecio.setBounds(12, 322, 100, 20);
        lblTitPrecio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitPrecio.setForeground(new Color(130, 70, 20));
        panelDetalle.add(lblTitPrecio);
        lblDetPrecio = new JLabel("");
        lblDetPrecio.setBounds(118, 322, 200, 22);
        lblDetPrecio.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetPrecio.setForeground(new Color(0, 130, 60));
        panelDetalle.add(lblDetPrecio);

        // Tiempo
        JLabel lblTitTiempo = new JLabel("Tiempo prep:");
        lblTitTiempo.setBounds(12, 348, 105, 20);
        lblTitTiempo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitTiempo.setForeground(new Color(130, 70, 20));
        panelDetalle.add(lblTitTiempo);
        lblDetTiempo = new JLabel("");
        lblDetTiempo.setBounds(118, 348, 200, 20);
        lblDetTiempo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panelDetalle.add(lblDetTiempo);

        // Stock
        JLabel lblTitStock = new JLabel("Disponibles:");
        lblTitStock.setBounds(12, 372, 105, 20);
        lblTitStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitStock.setForeground(new Color(130, 70, 20));
        panelDetalle.add(lblTitStock);
        lblDetStock = new JLabel("");
        lblDetStock.setBounds(118, 372, 210, 20);
        lblDetStock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelDetalle.add(lblDetStock);

        // Cantidad
        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setBounds(12, 400, 100, 20);
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCantidad.setForeground(new Color(130, 70, 20));
        panelDetalle.add(lblCantidad);

        spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnCantidad.setBounds(118, 398, 70, 26);
        spnCantidad.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // ===== BLOQUEAR ESCRITURA MANUAL EN EL SPINNER =====
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) spnCantidad.getEditor()).getTextField();
        spinnerTextField.setEditable(false);
        spinnerTextField.setBackground(new Color(255, 255, 255));

        panelDetalle.add(spnCantidad);

        // ===== Botón AGREGAR A VENTA =====
        if (ventaPadre != null) {
            JButton btnAgregar = new JButton("  Agregar a venta");
            btnAgregar.setBounds(27, 438, 295, 40);
            btnAgregar.setBackground(new Color(0, 150, 80));
            btnAgregar.setForeground(Color.WHITE);
            btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnAgregar.setFocusPainted(false);
            btnAgregar.setBorder(BorderFactory.createEmptyBorder());
            btnAgregar.addActionListener(e -> agregarAVenta());
            panelDetalle.add(btnAgregar);
        }

        // ===== Botón Limpiar (siempre visible) =====
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setBounds(ventaPadre != null ? 100 : 110,
                ventaPadre != null ? 492 : 452,
                140, 32);
        btnLimpiar.setBackground(new Color(160, 110, 80));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        btnLimpiar.addActionListener(e -> limpiarDetalle());
        panelDetalle.add(btnLimpiar);

        // ===== Label de estado (producto agregado) — siempre visible =====
        lblEstado = new JLabel("");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstado.setForeground(new Color(0, 130, 0));
        lblEstado.setBounds(27, ventaPadre != null ? 540 : 500, 310, 22);
        panelDetalle.add(lblEstado);

        panelPrincipal.add(panelDetalle);

        // ===== Leyenda de colores =====
        JPanel panelLeyenda = new JPanel(null);
        panelLeyenda.setBackground(new Color(255, 244, 228));
        panelLeyenda.setBounds(175, 668, 590, 22);

        JLabel cDisp = new JLabel("■");
        cDisp.setForeground(new Color(0, 130, 0));
        cDisp.setBounds(0, 2, 14, 18);
        panelLeyenda.add(cDisp);
        JLabel tDisp = new JLabel("Disponible");
        tDisp.setBounds(14, 2, 75, 18);
        tDisp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panelLeyenda.add(tDisp);

        JLabel cPoco = new JLabel("■");
        cPoco.setForeground(new Color(200, 150, 0));
        cPoco.setBounds(95, 2, 14, 18);
        panelLeyenda.add(cPoco);
        JLabel tPoco = new JLabel("Poco (≤5)");
        tPoco.setBounds(109, 2, 70, 18);
        tPoco.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panelLeyenda.add(tPoco);

        JLabel cSin = new JLabel("■");
        cSin.setForeground(new Color(200, 0, 0));
        cSin.setBounds(185, 2, 14, 18);
        panelLeyenda.add(cSin);
        JLabel tSin = new JLabel("Sin stock");
        tSin.setBounds(199, 2, 65, 18);
        tSin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panelLeyenda.add(tSin);

        panelPrincipal.add(panelLeyenda);

        // ===== Evento tabla =====
        tablaCatalogo.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mostrarDetalle();
            }
        });

        getContentPane().add(panelPrincipal);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    // ===== AGREGAR PRODUCTO A VENTA =====
    private void agregarAVenta() {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla primero.");
            return;
        }

        String nombre = tablaCatalogo.getValueAt(fila, 1).toString();
        String precioStr = tablaCatalogo.getValueAt(fila, 4).toString();
        int stockDisponible = 0;
        try {
            stockDisponible = Integer.parseInt(tablaCatalogo.getValueAt(fila, 6).toString().trim());
        } catch (Exception ex) {
        }

        if (stockDisponible <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Este producto no tiene stock disponible.\nNo se puede agregar a la venta.",
                    "Sin stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad = (int) spnCantidad.getValue();

        if (cantidad > stockDisponible) {
            JOptionPane.showMessageDialog(this,
                    "Solo hay " + stockDisponible + " unidades disponibles de \"" + nombre + "\".\nReduce la cantidad.",
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            spnCantidad.setValue(stockDisponible);
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            double subtotal = precio * cantidad;

            ventaPadre.actualizarTotal(subtotal);
            ventaPadre.agregarProductoLista(nombre, cantidad, precio);

            lblEstado.setForeground(new Color(0, 130, 0));
            lblEstado.setText("✔ " + cantidad + "x " + nombre + " — $" + String.format("%.2f", subtotal));
            spnCantidad.setValue(1);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer el precio del producto.");
        }
    }

    // ===== IMAGEN DESDE BD =====
    private void mostrarImagenDesdeBD(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            limpiarImagen();
            return;
        }
        try {
            java.net.URL imgUrl = getClass().getResource("/imagenes/" + nombreArchivo);
            if (imgUrl != null) {
                ImageIcon icono = new ImageIcon(imgUrl);
                Image img = icono.getImage().getScaledInstance(293, 158, Image.SCALE_SMOOTH);
                lblImagenPreview.setIcon(new ImageIcon(img));
                lblImagenPreview.setText("");
                return;
            }
        } catch (Exception ex) {
        }
        try {
            java.io.File archivo = new java.io.File("imagenes/" + nombreArchivo);
            if (archivo.exists()) {
                ImageIcon icono = new ImageIcon(archivo.getAbsolutePath());
                Image img = icono.getImage().getScaledInstance(293, 158, Image.SCALE_SMOOTH);
                lblImagenPreview.setIcon(new ImageIcon(img));
                lblImagenPreview.setText("");
                return;
            }
        } catch (Exception ex) {
        }
        limpiarImagen();
    }

    private void limpiarImagen() {
        lblImagenPreview.setIcon(null);
        lblImagenPreview.setText("Sin imagen");
    }

    // ===== MOSTRAR DETALLE =====
    private void mostrarDetalle() {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila < 0) {
            return;
        }

        String sku = tablaCatalogo.getValueAt(fila, 0).toString();
        String nombre = tablaCatalogo.getValueAt(fila, 1).toString();
        String descripcion = tablaCatalogo.getValueAt(fila, 2).toString();
        String categoria = tablaCatalogo.getValueAt(fila, 3).toString();
        String precio = tablaCatalogo.getValueAt(fila, 4).toString();
        String tiempo = tablaCatalogo.getValueAt(fila, 5).toString();
        String stockStr = tablaCatalogo.getValueAt(fila, 6).toString();

        lblDetNombre.setText(nombre);
        lblDetSku.setText("SKU: " + sku);
        lblDetDescripcion.setText("<html><body style='width:310px'>" + descripcion + "</body></html>");
        lblDetCategoria.setText(categoria);
        lblDetPrecio.setText("$" + precio);
        lblDetTiempo.setText(tiempo + " min");

        try {
            int stock = Integer.parseInt(stockStr.trim());
            if (stock == 0) {
                lblDetStock.setText("Sin stock");
                lblDetStock.setForeground(new Color(180, 0, 0));
            } else if (stock <= 5) {
                lblDetStock.setText(stock + " unidades   ¡Poco stock!");
                lblDetStock.setForeground(new Color(160, 110, 0));
            } else {
                lblDetStock.setText(stock + " unidades");
                lblDetStock.setForeground(new Color(0, 130, 0));
            }
        } catch (Exception ex) {
            lblDetStock.setText(stockStr);
            lblDetStock.setForeground(Color.DARK_GRAY);
        }

        // Leer imagen desde la columna 7
        String imagen = null;
        try {
            imagen = tablaCatalogo.getValueAt(fila, 7) != null
                    ? tablaCatalogo.getValueAt(fila, 7).toString()
                    : null;
        } catch (Exception ex) {
        }
        mostrarImagenDesdeBD(imagen);
        spnCantidad.setValue(1);
        lblEstado.setText("");
    }

    // ===== LIMPIAR DETALLE =====
    private void limpiarDetalle() {
        lblDetNombre.setText("");
        lblDetSku.setText("");
        lblDetDescripcion.setText("");
        lblDetCategoria.setText("");
        lblDetPrecio.setText("");
        lblDetTiempo.setText("");
        lblDetStock.setText("");
        limpiarImagen();
        spnCantidad.setValue(1);
        tablaCatalogo.clearSelection();
        lblEstado.setText("");
    }

    // ===== CARGAR TABLA =====
    public void cargarCatalogo() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"SKU", "Nombre", "Descripción", "Categoría", "Precio", "Tiempo (min)", "Stock", "imagen"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String categoria = cboCategorias.getSelectedItem().toString();
        String sql = categoria.equals("Todos")
                ? "SELECT p.sku, p.nombre, p.descripcion, c.nombre_categoria AS categoria, p.precio, p.tiempo_preparacion_min, COALESCE(i.stock_disponible,0) AS stock, p.imagen "
                + "FROM producto p LEFT JOIN categoria c ON p.categoria_id = c.categoria_id LEFT JOIN inventario_producto i ON p.producto_id = i.producto_id "
                + "ORDER BY c.nombre_categoria, p.nombre"
                : "SELECT p.sku, p.nombre, p.descripcion, c.nombre_categoria AS categoria, p.precio, p.tiempo_preparacion_min, COALESCE(i.stock_disponible,0) AS stock, p.imagen "
                + "FROM producto p LEFT JOIN categoria c ON p.categoria_id = c.categoria_id LEFT JOIN inventario_producto i ON p.producto_id = i.producto_id "
                + "WHERE c.nombre_categoria = ? ORDER BY p.nombre";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (!categoria.equals("Todos")) {
                ps.setString(1, categoria);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("sku"), rs.getString("nombre"),
                    rs.getString("descripcion"), rs.getString("categoria"),
                    rs.getString("precio"), rs.getString("tiempo_preparacion_min"),
                    rs.getString("stock"), rs.getString("imagen")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar catálogo: " + e.getMessage());
        }

        tablaCatalogo.setModel(model);
        tablaCatalogo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Ocultar columna imagen (Indice 7)
        tablaCatalogo.getColumnModel().getColumn(7).setMinWidth(0);
        tablaCatalogo.getColumnModel().getColumn(7).setMaxWidth(0);
        tablaCatalogo.getColumnModel().getColumn(7).setWidth(0);
        
        tablaCatalogo.getColumnModel().getColumn(0).setPreferredWidth(45);
        tablaCatalogo.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaCatalogo.getColumnModel().getColumn(2).setPreferredWidth(170);
        tablaCatalogo.getColumnModel().getColumn(3).setPreferredWidth(65);
        tablaCatalogo.getColumnModel().getColumn(4).setPreferredWidth(55);
        tablaCatalogo.getColumnModel().getColumn(5).setPreferredWidth(65);
        tablaCatalogo.getColumnModel().getColumn(6).setPreferredWidth(95);
        
        lblEstado.setText("Total: " + model.getRowCount() + " productos");
    }

    // ===== BUSCAR =====
    private void buscarProducto() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            cargarCatalogo();
            return;
        }

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"SKU", "Nombre", "Descripción", "Categoría", "Precio", "Tiempo (min)", "Stock", "imagen"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT p.sku, p.nombre, p.descripcion, c.nombre_categoria AS categoria, p.precio, p.tiempo_preparacion_min, COALESCE(i.stock_disponible,0) AS stock, p.imagen "
                + "FROM producto p LEFT JOIN categoria c ON p.categoria_id = c.categoria_id LEFT JOIN inventario_producto i ON p.producto_id = i.producto_id "
                + "WHERE p.nombre LIKE ? OR p.sku LIKE ? OR p.descripcion LIKE ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            String val = "%" + texto + "%";
            ps.setString(1, val);
            ps.setString(2, val);
            ps.setString(3, val);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("sku"), rs.getString("nombre"),
                    rs.getString("descripcion"), rs.getString("categoria"),
                    rs.getString("precio"), rs.getString("tiempo_preparacion_min"),
                    rs.getString("stock"), rs.getString("imagen")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        tablaCatalogo.setModel(model);
        tablaCatalogo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Ocultar columna imagen (Indice 7)
        tablaCatalogo.getColumnModel().getColumn(7).setMinWidth(0);
        tablaCatalogo.getColumnModel().getColumn(7).setMaxWidth(0);
        tablaCatalogo.getColumnModel().getColumn(7).setWidth(0);
        
        lblEstado.setText("Resultados: " + model.getRowCount() + " productos");
    }

    // ===== UTILIDADES =====
    private JButton crearBotonSide(String texto, int x, int y) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 120, 30);
        btn.setBackground(new Color(180, 100, 60));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        return btn;
    }

    private JButton crearBotonBusq(String texto, int x, int y) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 100, 28);
        btn.setBackground(new Color(210, 140, 100));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        return btn;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Catalogo().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ===== VARIABLES =====
    private JTable tablaCatalogo;
    private JTextField txtBuscar;
    private JComboBox<String> cboCategorias;
    private JSpinner spnCantidad;
    private JLabel lblEstado, lblImagenPreview;
    private JLabel lblDetNombre, lblDetSku, lblDetDescripcion;
    private JLabel lblDetCategoria, lblDetPrecio, lblDetTiempo;
    private JLabel lblDetStock;
}