package loginvanabby;


import conexion.ConexionBD;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class Cliente extends javax.swing.JFrame {

    private ConexionBD con = new ConexionBD();
    
    // Variables para el carrito de compras
    private ArrayList<ItemCarrito> carrito = new ArrayList<>();
    private JButton btnVerCarrito;
    private int idClienteSesion = 1; // IMPORTANTE: Aquí debes pasar el ID del cliente que inició sesión desde el Login

    // Nuevos filtros
    private JComboBox<String> cboSabor;
    private JComboBox<String> cboPrecio;
    private JPanel panelProductos;

    public Cliente() {
        initComponents(); // Construye la interfaz y los filtros PRIMERO
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Catálogo de Clientes - Vanabby Repostería");
        
        cargarSaboresReales(); // Busca los sabores que sí existen en la BD
        cargarProductos();     // Luego carga los productos
    }

    // =========================================================================
    // FILTRO INTELIGENTE DE SABORES
    // =========================================================================
    private void cargarSaboresReales() {
        cboSabor.removeAllItems();
        cboSabor.addItem("Todos");
        
        // Lista de sabores comunes a buscar en la BD
        String[] saboresPosibles = {"Chocolate", "Vainilla", "Fresa", "Nuez", "Limón", "Zanahoria", "Moka", "Tres Leches", "Cajeta", "Queso", "Oreo"};
        
        try (Connection cn = con.getConexion()) {
            for (String sabor : saboresPosibles) {
                // Verificamos si al menos un producto menciona este sabor en su nombre o descripción
                String sql = "SELECT COUNT(*) FROM producto WHERE nombre LIKE ? OR descripcion LIKE ?";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, "%" + sabor + "%");
                    ps.setString(2, "%" + sabor + "%");
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        cboSabor.addItem(sabor); // Solo lo agrega si existe en la BD
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error al cargar sabores: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        panelProductos.removeAll();
        panelProductos.setLayout(new GridLayout(0, 4, 20, 20)); 
        panelProductos.setBackground(new Color(245, 230, 211));
        panelProductos.setBorder(new EmptyBorder(20, 20, 20, 20));

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            cn = con.getConexion();
            
            String saborFiltro = cboSabor.getSelectedItem() != null ? cboSabor.getSelectedItem().toString() : "Todos";
            String precioFiltro = cboPrecio.getSelectedItem() != null ? cboPrecio.getSelectedItem().toString() : "Todos";
            
            String sql = "SELECT p.producto_id, p.nombre, p.descripcion, c.nombre_categoria AS categoria, "
                       + "p.precio, p.tiempo_preparacion_min, p.sku, p.imagen, "
                       + "COALESCE(inv.stock_disponible, 0) AS stock_disponible "
                       + "FROM producto p "
                       + "LEFT JOIN categoria c ON p.categoria_id = c.categoria_id "
                       + "LEFT JOIN inventario_producto inv ON p.producto_id = inv.producto_id "
                       + "WHERE 1=1 "; 

            if (!saborFiltro.equals("Todos")) {
                sql += " AND (p.nombre LIKE '%" + saborFiltro + "%' OR p.descripcion LIKE '%" + saborFiltro + "%') ";
            }

            if (precioFiltro.equals("Menor a $100")) {
                sql += " AND p.precio < 100 ";
            } else if (precioFiltro.equals("$100 - $300")) {
                sql += " AND p.precio BETWEEN 100 AND 300 ";
            } else if (precioFiltro.equals("Mayor a $300")) {
                sql += " AND p.precio > 300 ";
            }

            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                int idProducto = rs.getInt("producto_id");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                String categoria = rs.getString("categoria");
                double precio = rs.getDouble("precio");
                int tiempo = rs.getInt("tiempo_preparacion_min");
                String sku = rs.getString("sku");
                String imagen = rs.getString("imagen");
                int stock = rs.getInt("stock_disponible"); 

                JPanel card = crearCard(idProducto, nombre, descripcion, categoria, precio, tiempo, sku, imagen, stock);
                panelProductos.add(card);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); if (ps != null) ps.close(); if (cn != null) cn.close(); } catch (Exception ex) {}
        }

        panelProductos.revalidate();
        panelProductos.repaint();
    }

    private JPanel crearCard(int idProducto, String nombre, String descripcion, String categoria,
            double precio, int tiempo, String sku, String imagen, int stock) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 244, 228));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 102, 51), 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(240, 440)); 

        JLabel lblImagen = new JLabel();
        lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // =========================================================================
        // CARGA DE IMÁGENES CORREGIDA
        // =========================================================================
        try {
            java.net.URL imgUrl = (imagen != null && !imagen.isEmpty()) ? getClass().getResource("/imagenes/" + imagen) : null;
            if (imgUrl != null) {
                Image img = new ImageIcon(imgUrl).getImage().getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
            } else {
                java.io.File archivo = new java.io.File("imagenes/" + imagen);
                if (archivo.exists()) {
                    Image img = new ImageIcon(archivo.getAbsolutePath()).getImage().getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(img));
                } else {
                    lblImagen.setText("Sin imagen");
                    lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
                }
            }
        } catch (Exception e) {
            lblImagen.setText("Sin imagen");
        }

        String catText = (categoria != null) ? categoria.toUpperCase() : "";
        JLabel lblCategoria = new JLabel(catText);
        lblCategoria.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCategoria.setFont(new Font("Arial", Font.BOLD, 11));
        lblCategoria.setForeground(new Color(255, 102, 51));

        JLabel lblNombre = new JLabel("<html><center>" + nombre + "</center></html>");
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setFont(new Font("Times New Roman", Font.BOLD, 16));
        lblNombre.setForeground(new Color(80, 40, 0));

        JLabel lblDesc = new JLabel("<html><center>" + descripcion + "</center></html>");
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDesc.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(100, 80, 60));

        JLabel lblPrecio = new JLabel("$ " + String.format("%.2f", precio));
        lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPrecio.setFont(new Font("Arial Black", Font.PLAIN, 18));
        lblPrecio.setForeground(new Color(0, 153, 51));

        JLabel lblStock = new JLabel();
        lblStock.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStock.setFont(new Font("Arial", Font.BOLD, 12));

        JButton btnAgregar = new JButton("Agregar al pedido");
        btnAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAgregar.setBackground(new Color(0, 153, 51));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (stock > 0) {
            lblStock.setText("Stock disponible: " + stock);
            lblStock.setForeground(new Color(0, 102, 204));
        } else {
            lblStock.setText("¡AGOTADO!");
            lblStock.setForeground(Color.RED);
            btnAgregar.setEnabled(false); 
            btnAgregar.setBackground(Color.GRAY);
        }
        
        btnAgregar.addActionListener(e -> {
            int cantidadEnCarrito = 0;
            for (ItemCarrito item : carrito) {
                if (item.idProducto == idProducto) {
                    cantidadEnCarrito += item.cantidad;
                }
            }

            if (cantidadEnCarrito >= stock) {
                JOptionPane.showMessageDialog(this, 
                    "No puedes agregar más de este producto.\nYa tienes " + cantidadEnCarrito + " en tu carrito, que es todo el stock disponible.", 
                    "Stock Agotado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int stockRestante = stock - cantidadEnCarrito;

            String cantStr = JOptionPane.showInputDialog(this, 
                    "Stock restante para ti: " + stockRestante + "\n\n¿Cuántas unidades de " + nombre + " deseas pedir?", "1");
            
            if (cantStr != null && !cantStr.trim().isEmpty()) {
                try {
                    int cantidadPedida = Integer.parseInt(cantStr);
                    
                    if (cantidadPedida <= 0) {
                        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (cantidadPedida > stockRestante) {
                        JOptionPane.showMessageDialog(this, 
                            "¡Stock insuficiente!\nEstás pidiendo " + cantidadPedida + ", pero solo quedan " + stockRestante + " disponibles.", 
                            "Stock Insuficiente", JOptionPane.ERROR_MESSAGE);
                    } else {
                        boolean existeEnCarrito = false;
                        for (ItemCarrito item : carrito) {
                            if (item.idProducto == idProducto) {
                                item.cantidad += cantidadPedida;
                                existeEnCarrito = true;
                                break;
                            }
                        }
                        
                        if (!existeEnCarrito) {
                            carrito.add(new ItemCarrito(idProducto, nombre, precio, cantidadPedida));
                        }
                        
                        actualizarBotonCarrito();
                        JOptionPane.showMessageDialog(this, "Producto agregado al carrito exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        card.add(lblImagen);
        card.add(Box.createVerticalStrut(6));
        card.add(lblCategoria);
        card.add(Box.createVerticalStrut(4));
        card.add(lblNombre);
        card.add(Box.createVerticalStrut(6));
        card.add(lblDesc);
        card.add(Box.createVerticalStrut(8));
        card.add(lblPrecio);
        card.add(Box.createVerticalStrut(4));
        card.add(lblStock); 
        card.add(Box.createVerticalStrut(8));
        card.add(btnAgregar);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(255, 235, 210));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 153, 51), 2, true),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(255, 244, 228));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 102, 51), 2, true),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }
        });

        return card;
    }

    private void actualizarBotonCarrito() {
        int totalItems = 0;
        for (ItemCarrito item : carrito) {
            totalItems += item.cantidad;
        }
        btnVerCarrito.setText("Ver Carrito 🛒 (" + totalItems + ")");
    }

    private void abrirVentanaCheckout() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tu carrito está vacío. Agrega productos primero.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Confirmar Pedido", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(255, 244, 228));

        String[] columnas = {"Producto", "Precio U.", "Cantidad", "Subtotal"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        double totalPedido = 0;
        for (ItemCarrito item : carrito) {
            double subtotal = item.precio * item.cantidad;
            totalPedido += subtotal;
            modelo.addRow(new Object[]{item.nombre, "$" + item.precio, item.cantidad, "$" + subtotal});
        }
        
        JTable tablaCheckout = new JTable(modelo);
        tablaCheckout.setRowHeight(25);
        
        JPanel panelInferior = new JPanel(new GridLayout(3, 1, 10, 10));
        panelInferior.setBackground(new Color(255, 244, 228));
        panelInferior.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTotal = new JLabel("Total del Pedido: $" + String.format("%.2f", totalPedido));
        lblTotal.setFont(new Font("Arial Black", Font.BOLD, 18));
        lblTotal.setForeground(new Color(0, 153, 51));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelFecha.setBackground(new Color(255, 244, 228));
        panelFecha.add(new JLabel("Fecha de Entrega (AAAA-MM-DD): "));
        JTextField txtFecha = new JTextField(10);
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        txtFecha.setText(new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
        panelFecha.add(txtFecha);

        JButton btnConfirmar = new JButton("Confirmar y Realizar Pedido");
        btnConfirmar.setBackground(new Color(255, 102, 51));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        final double totalFinal = totalPedido;
        
        btnConfirmar.addActionListener(e -> {
            String fechaDeseada = txtFecha.getText().trim();
            if (fechaDeseada.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debe ingresar una fecha de entrega.");
                return;
            }
            
            // =========================================================================
            // VALIDACIÓN ESTRICTA DE FECHA DE ENTREGA
            // =========================================================================
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false); // Validar que la fecha sea real (no 2026-15-40)
                java.util.Date parsedDate = sdf.parse(fechaDeseada);
                
                // Remover horas de la fecha actual para comparar solo el día
                java.util.Calendar calHoy = java.util.Calendar.getInstance();
                calHoy.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calHoy.set(java.util.Calendar.MINUTE, 0);
                calHoy.set(java.util.Calendar.SECOND, 0);
                calHoy.set(java.util.Calendar.MILLISECOND, 0);
                
                if (parsedDate.before(calHoy.getTime())) {
                    JOptionPane.showMessageDialog(dialog, "La fecha de entrega no puede ser en el pasado.", "Fecha Inválida", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de fecha inválido. Usa el formato AAAA-MM-DD (Ej: 2026-12-31).", "Fecha Inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            procesarCompraEnBD(fechaDeseada, totalFinal);
            dialog.dispose();
        });

        panelInferior.add(lblTotal);
        panelInferior.add(panelFecha);
        panelInferior.add(btnConfirmar);

        dialog.add(new JScrollPane(tablaCheckout), BorderLayout.CENTER);
        dialog.add(panelInferior, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void procesarCompraEnBD(String fechaEntrega, double total) {
        Connection cn = null;
        try {
            cn = con.getConexion();
            cn.setAutoCommit(false); 

            // 1. Insertar Venta
            String sqlVenta = "INSERT INTO venta (cliente_id, fecha_venta, fecha_entrega_programada, estado_venta_id, metodo_pago_id, total) "
                            + "VALUES (?, CURRENT_DATE(), ?, (SELECT estado_venta_id FROM estado_venta WHERE nombre_estado = 'PENDIENTE' LIMIT 1), 1, ?)";
            
            PreparedStatement psVenta = cn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, idClienteSesion);
            psVenta.setString(2, fechaEntrega);
            psVenta.setDouble(3, total);
            psVenta.executeUpdate();

            ResultSet rsKeys = psVenta.getGeneratedKeys();
            int idVentaGenerada = 0;
            if (rsKeys.next()) {
                idVentaGenerada = rsKeys.getInt(1);
            } else {
                throw new SQLException("No se pudo obtener el ID de la venta generada.");
            }

            // 2. Insertar Detalle Venta
            String sqlDetalle = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psDetalle = cn.prepareStatement(sqlDetalle);

            String sqlActualizarStock = "UPDATE inventario_producto SET stock_disponible = stock_disponible - ? WHERE producto_id = ?";
            PreparedStatement psStock = cn.prepareStatement(sqlActualizarStock);

            for (ItemCarrito item : carrito) {
                psDetalle.setInt(1, idVentaGenerada);
                psDetalle.setInt(2, item.idProducto);
                psDetalle.setInt(3, item.cantidad);
                psDetalle.setDouble(4, item.precio);
                psDetalle.setDouble(5, (item.precio * item.cantidad));
                psDetalle.executeUpdate();
                
                psStock.setInt(1, item.cantidad);
                psStock.setInt(2, item.idProducto);
                psStock.executeUpdate();
            }

            cn.commit(); 
            
            // =========================================================================
            // ENVIAR TICKET AL CORREO DEL CLIENTE
            // =========================================================================
            try {
                String sqlCorreo = "SELECT nombre, correo FROM cliente WHERE cliente_id = ?";
                PreparedStatement psCorreo = cn.prepareStatement(sqlCorreo);
                psCorreo.setInt(1, idClienteSesion);
                ResultSet rsCorreo = psCorreo.executeQuery();

                if (rsCorreo.next()) {
                    String correoDestino = rsCorreo.getString("correo");
                    String nombreCl = rsCorreo.getString("nombre");
                    
                    if (correoDestino != null && !correoDestino.isEmpty()) {
                        StringBuilder listaProductos = new StringBuilder();
                        for (ItemCarrito item : carrito) {
                            listaProductos.append("• ").append(item.cantidad).append("x ").append(item.nombre).append("\n");
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        EnviarCorreo.enviarTicketVenta(
                                correoDestino, nombreCl,
                                "V-" + idVentaGenerada,
                                sdf.format(new java.util.Date()), 
                                fechaEntrega,
                                listaProductos.toString(), 
                                String.valueOf(total), "Efectivo/Pendiente", "PENDIENTE", "Autoservicio Cliente"
                        );
                    }
                }
            } catch (Exception ex) {
                System.out.println("No se pudo enviar el correo del cliente: " + ex.getMessage());
            }

            carrito.clear();
            actualizarBotonCarrito();
            cargarProductos(); // REFRESCAR LA PANTALLA PARA VER EL NUEVO STOCK
            JOptionPane.showMessageDialog(this, "¡Tu pedido ha sido registrado con éxito!\nNos pondremos en contacto contigo pronto.", "Pedido Completado", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            try { if (cn != null) cn.rollback(); } catch (SQLException ex) {} 
            JOptionPane.showMessageDialog(this, "Error al registrar el pedido: " + e.getMessage(), "Error en BD", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try { if (cn != null) cn.setAutoCommit(true); cn.close(); } catch (SQLException ex) {}
        }
    }

    private void initComponents() {
        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(new Color(245, 230, 211));

        // --- ENCABEZADO SUPERIOR ---
        JPanel panelArriba = new JPanel();
        panelArriba.setLayout(new BoxLayout(panelArriba, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 244, 228));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png"));
        Image logoImg = logoIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(logoImg));

        JLabel lblTitulo = new JLabel("Catálogo Vanabby Repostería");
        lblTitulo.setFont(new Font("Times New Roman", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 153, 51));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel pnlBotonesHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotonesHeader.setBackground(new Color(255, 244, 228));

        // BOTÓN CON ÍCONO DE CARRITO
        btnVerCarrito = new JButton("Ver Carrito 🛒 (0)");
        btnVerCarrito.setBackground(new Color(255, 153, 0));
        btnVerCarrito.setForeground(Color.WHITE);
        btnVerCarrito.setFont(new Font("Arial", Font.BOLD, 14));
        btnVerCarrito.setFocusPainted(false);
        btnVerCarrito.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerCarrito.addActionListener(e -> abrirVentanaCheckout());

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.setBackground(new Color(255, 102, 51));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFont(new Font("Arial", Font.BOLD, 13));
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });

        pnlBotonesHeader.add(btnVerCarrito);
        pnlBotonesHeader.add(btnSalir);

        header.add(lblLogo, BorderLayout.WEST);
        header.add(lblTitulo, BorderLayout.CENTER);
        header.add(pnlBotonesHeader, BorderLayout.EAST);
        
        panelArriba.add(header);

        // --- BARRA DE FILTROS DINÁMICA ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelFiltros.setBackground(new Color(245, 230, 211));
        panelFiltros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 102, 51)), "Filtros de Búsqueda", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(255, 102, 51)));

        cboSabor = new JComboBox<>(new String[]{"Todos"}); // Se llena dinámicamente en cargarSaboresReales()
        cboSabor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboSabor.addActionListener(e -> cargarProductos());

        cboPrecio = new JComboBox<>(new String[]{"Todos", "Menor a $100", "$100 - $300", "Mayor a $300"});
        cboPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboPrecio.addActionListener(e -> cargarProductos());

        JLabel lblSabor = new JLabel("🔍 Filtrar por Sabor:");
        lblSabor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel lblPrecio = new JLabel("💲 Rango de Precio:");
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panelFiltros.add(lblSabor);
        panelFiltros.add(cboSabor);
        panelFiltros.add(lblPrecio);
        panelFiltros.add(cboPrecio);

        panelArriba.add(panelFiltros);
        panelFondo.add(panelArriba, BorderLayout.NORTH);

        // --- ÁREA DE PRODUCTOS ---
        panelProductos = new JPanel();
        panelProductos.setBackground(new Color(245, 230, 211));
        JScrollPane scroll = new JScrollPane(panelProductos);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(new Color(245, 230, 211));

        panelFondo.add(scroll, BorderLayout.CENTER);

        setContentPane(panelFondo);
        pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cliente().setVisible(true));
    }

    class ItemCarrito {
        int idProducto;
        String nombre;
        double precio;
        int cantidad;

        public ItemCarrito(int idProducto, String nombre, double precio, int cantidad) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }
    }
}