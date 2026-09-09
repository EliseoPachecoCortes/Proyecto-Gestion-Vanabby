package loginvanabby;

import com.toedter.calendar.JDateChooser;
import conexion.ConexionBD;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.image.BufferedImage;

public class Registro_Venta extends javax.swing.JFrame {

    // Variable global estática para el usuario en sesión
    public static String cajeroEnSesion = "";

    private int ultimaVentaId = 0;
    private int ultimoClienteId = 0;
    private double ultimoTotal = 0;
    private int clienteIdBuscado = 0;
    private java.util.List<Object[]> productosTemp = new java.util.ArrayList<>();

    // Componentes del Formulario
    private JComboBox<String> cboEmpleado, cboEstadoPago, cboMetodoPago;
    private JDateChooser calendarVenta, calendarEntrega;
    private JTextField txtNombreCliente, txtApPaterno, txtApMaterno;
    private JTextField txtTelefono, txtCorreo, txtCalle, txtNumExt, txtColonia, txtCiudad, txtEstado, txtCP;
    private JButton btnBuscarCliente, btnNuevoCliente, btnActualizarCliente;

    // Componentes del Ticket
    private JTable tablaTicket;
    private DefaultTableModel modTicket;
    private JLabel lblGranTotal;
    private JLabel lblCajeroNombre;   // se actualiza en configurarLogicaInicial()
    private JButton btnProductos, btnRegistrar, btnAnticipo;

    public Registro_Venta() {
        crearInterfazGrafica();
        configurarLogicaInicial();
    }

    // =========================================================================
    // MÉTODO AUXILIAR: carga usuario.png con múltiples estrategias
    // =========================================================================
    private ImageIcon cargarImagenUsuario(int ancho, int alto) {
        // Estrategia 1: getResource desde el classpath (forma estándar)
        try {
            java.net.URL url = getClass().getResource("/imagenes/usuario.png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                return new ImageIcon(icon.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
            }
        } catch (Exception ex) {
            /* intenta siguiente */ }

        // Estrategia 2: ruta relativa al directorio de trabajo (build/classes)
        try {
            File f = new File("build/classes/imagenes/usuario.png");
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                // Usa Graphics2D para un escalado de alta calidad
                BufferedImage original = new BufferedImage(
                        icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = original.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(icon.getImage(), 0, 0, ancho, alto, null);
                g2d.dispose();
                return new ImageIcon(original);
            }
        } catch (Exception ex) {
            /* intenta siguiente */ }

        // Estrategia 3: ruta relativa al src (cuando se ejecuta desde el IDE directamente)
        try {
            File f = new File("src/imagenes/usuario.png");
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                return new ImageIcon(icon.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
            }
        } catch (Exception ex) {
            /* no encontrada */ }

        return null; // no se encontró la imagen en ninguna ruta
    }

    private void configurarLogicaInicial() {
        setTitle("Caja: Registro de Venta - Vanabby");
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        if (cajeroEnSesion == null || cajeroEnSesion.isEmpty()) {
            cajeroEnSesion = "Cajero Desconocido";
        }

        // Actualiza el label del header con el nombre real del cajero
        if (lblCajeroNombre != null) {
            lblCajeroNombre.setText(cajeroEnSesion);
        }

        cboEmpleado.removeAllItems();
        cboEmpleado.addItem(cajeroEnSesion);
        cboEmpleado.setSelectedIndex(0);
        cboEmpleado.setEnabled(false);

        btnActualizarCliente.setEnabled(false);

        Date hoy = new Date();
        calendarVenta.setDate(hoy);
        calendarVenta.setMinSelectableDate(hoy);
        calendarVenta.setMaxSelectableDate(hoy);
        calendarVenta.setEnabled(false);
        calendarEntrega.setMinSelectableDate(hoy);
    }

    public void agregarProductoLista(String nombre, int cantidad, double precio) {
        double subtotal = cantidad * precio;
        productosTemp.add(new Object[]{nombre, cantidad, precio});
        modTicket.addRow(new Object[]{nombre, cantidad, "$ " + String.format("%.2f", precio), "$ " + String.format("%.2f", subtotal)});
        actualizarTotal(subtotal);
    }

    public void actualizarTotal(double subtotal) {
        double totalActual = 0;
        for (Object[] prod : productosTemp) {
            int cant = (int) prod[1];
            double precio = (double) prod[2];
            totalActual += (cant * precio);
        }
        lblGranTotal.setText("$ " + String.format("%.2f", totalActual));
        ultimoTotal = totalActual;
    }

    private void buscarClientePorTelefono() {
        String telephone = txtTelefono.getText().trim();
        if (telephone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un número de teléfono para buscar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT c.cliente_id, c.nombre, c.ap_paterno, c.ap_materno, c.correo, COALESCE(d.calle,'') AS calle, "
                + "COALESCE(d.colonia,'') AS colonia, COALESCE(d.ciudad,'') AS ciudad, "
                + "COALESCE(d.estado_geo,'') AS estado_geo, COALESCE(d.cp,'') AS cp "
                + "FROM cliente c LEFT JOIN direccion_cliente d ON c.cliente_id = d.cliente_id AND d.es_principal = 1 "
                + "WHERE c.telefono = ?";

        try (Connection cn = ConexionBD.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, telephone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    clienteIdBuscado = rs.getInt("cliente_id");
                    txtNombreCliente.setText(rs.getString("nombre"));
                    txtApPaterno.setText(rs.getString("ap_paterno"));
                    txtApMaterno.setText(rs.getString("ap_materno"));
                    txtCorreo.setText(rs.getString("correo"));
                    txtColonia.setText(rs.getString("colonia"));
                    txtCiudad.setText(rs.getString("ciudad"));
                    txtEstado.setText(rs.getString("estado_geo"));
                    txtCP.setText(rs.getString("cp"));

                    String dir = rs.getString("calle");
                    if (dir.contains(" #")) {
                        String[] partes = dir.split(" #");
                        txtCalle.setText(partes[0]);
                        if (partes.length > 1) {
                            txtNumExt.setText(partes[1]);
                        }
                    } else {
                        txtCalle.setText(dir);
                        txtNumExt.setText("");
                    }

                    btnActualizarCliente.setEnabled(true);
                    txtCorreo.setBackground(new Color(220, 255, 220));
                    JOptionPane.showMessageDialog(this, "¡Cliente encontrado y cargado en el formulario!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    clienteIdBuscado = 0;
                    txtNombreCliente.setText("");
                    txtApPaterno.setText("");
                    txtApMaterno.setText("");
                    txtCorreo.setText("");
                    txtCalle.setText("");
                    txtNumExt.setText("");
                    txtColonia.setText("");
                    txtCiudad.setText("");
                    txtEstado.setText("");
                    txtCP.setText("");
                    btnActualizarCliente.setEnabled(false);
                    txtCorreo.setBackground(Color.WHITE);
                    JOptionPane.showMessageDialog(this, "El cliente no existe. Por favor llena sus datos para registrarlo.", "No encontrado", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage());
        }
    }

    private void registrarNuevoCliente() {
        String nombre = txtNombreCliente.getText().trim();
        String telephone = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String cp = txtCP.getText().trim();
        String estado = txtEstado.getText().trim();
        String calle = txtCalle.getText().trim();
        String numExt = txtNumExt.getText().trim();

        if (nombre.isEmpty() || telephone.isEmpty() || correo.isEmpty() || calle.isEmpty() || numExt.isEmpty() || cp.isEmpty() || estado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos principales son obligatorios.", "Campos Requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection cn = ConexionBD.getConexion()) {
            PreparedStatement psCheck = cn.prepareStatement("SELECT cliente_id FROM cliente WHERE telefono = ? OR correo = ?");
            psCheck.setString(1, telephone);
            psCheck.setString(2, correo);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                JOptionPane.showMessageDialog(this, "Ya existe un cliente con ese teléfono o correo en la BD.", "Cliente Duplicado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement psIns = cn.prepareStatement(
                    "INSERT INTO cliente (nombre, ap_paterno, ap_materno, telefono, correo) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            psIns.setString(1, nombre);
            psIns.setString(2, txtApPaterno.getText().trim());
            psIns.setString(3, txtApMaterno.getText().trim());
            psIns.setString(4, telephone);
            psIns.setString(5, correo);
            psIns.executeUpdate();

            ResultSet rsKeys = psIns.getGeneratedKeys();
            if (rsKeys.next()) {
                clienteIdBuscado = rsKeys.getInt(1);
                PreparedStatement psDir = cn.prepareStatement(
                        "INSERT INTO direccion_cliente (cliente_id, alias, calle, colonia, ciudad, estado_geo, cp, es_principal) VALUES (?, 'Principal', ?, ?, ?, ?, ?, 1)");
                psDir.setInt(1, clienteIdBuscado);
                psDir.setString(2, calle + " #" + numExt);
                psDir.setString(3, txtColonia.getText().trim());
                psDir.setString(4, txtCiudad.getText().trim());
                psDir.setString(5, estado);
                psDir.setString(6, cp);
                psDir.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Cliente nuevo guardado exitosamente en la BD.");
            btnActualizarCliente.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al registrar: " + e.getMessage());
        }
    }

    private void actualizarCliente() {
        if (clienteIdBuscado == 0) {
            JOptionPane.showMessageDialog(this, "Primero busca un cliente existente para poder actualizarlo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = txtNombreCliente.getText().trim();
        String telephone = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String cp = txtCP.getText().trim();
        String estado = txtEstado.getText().trim();
        String calle = txtCalle.getText().trim();
        String numExt = txtNumExt.getText().trim();

        try (Connection cn = ConexionBD.getConexion()) {
            PreparedStatement psUpd = cn.prepareStatement(
                    "UPDATE cliente SET nombre=?, ap_paterno=?, ap_materno=?, telefono=?, correo=? WHERE cliente_id=?");
            psUpd.setString(1, nombre);
            psUpd.setString(2, txtApPaterno.getText().trim());
            psUpd.setString(3, txtApMaterno.getText().trim());
            psUpd.setString(4, telephone);
            psUpd.setString(5, correo);
            psUpd.setInt(6, clienteIdBuscado);
            psUpd.executeUpdate();

            PreparedStatement psDirUpd = cn.prepareStatement(
                    "UPDATE direccion_cliente SET calle=?, colonia=?, ciudad=?, estado_geo=?, cp=? WHERE cliente_id=? AND es_principal=1");
            psDirUpd.setString(1, calle + " #" + numExt);
            psDirUpd.setString(2, txtColonia.getText().trim());
            psDirUpd.setString(3, txtCiudad.getText().trim());
            psDirUpd.setString(4, estado);
            psDirUpd.setString(5, cp);
            psDirUpd.setInt(6, clienteIdBuscado);
            psDirUpd.executeUpdate();

            JOptionPane.showMessageDialog(this, "Datos del cliente actualizados en la Base de Datos.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private void registrarVenta() {
        if (ultimoTotal <= 0 || productosTemp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El ticket está vacío. Agrega productos desde el catálogo.", "Carrito Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txtTelefono.getText().trim().isEmpty() || txtNombreCliente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Faltan los datos del Cliente (Teléfono o Nombre).", "Falta Cliente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (calendarEntrega.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Debes indicar una Fecha de Entrega válida.", "Falta Fecha", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cboMetodoPago.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar un Método de Pago.", "Falta Método", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cboEstadoPago.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Debes indicar el Estado del Pago (Pendiente / Pagado).", "Falta Estado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String Telefono = txtTelefono.getText().trim();
        String NombreCliente = txtNombreCliente.getText().trim();
        Date FechaVenta = calendarVenta.getDate();
        Date FechaEntrega = calendarEntrega.getDate();
        String metodoPago = cboMetodoPago.getSelectedItem().toString();
        String estadoPago = cboEstadoPago.getSelectedItem().toString();
        String numeroAutorizacionTransferencia = "";

        if (metodoPago.equalsIgnoreCase("Tarjeta de crédito") || metodoPago.equalsIgnoreCase("Tarjeta")) {
            JTextField txtNumTarjeta = new JTextField(16);
            JDateChooser calendarExpTarjeta = new JDateChooser();
            calendarExpTarjeta.setDateFormatString("MM/yy");
            calendarExpTarjeta.setMinSelectableDate(new Date());
            JPanel panelTarjeta = new JPanel(new GridLayout(2, 2, 10, 10));
            panelTarjeta.add(new JLabel("Número de Tarjeta (16 dígitos):"));
            panelTarjeta.add(txtNumTarjeta);
            panelTarjeta.add(new JLabel("Fecha Expiración:"));
            panelTarjeta.add(calendarExpTarjeta);
            int result = JOptionPane.showConfirmDialog(this, panelTarjeta, "Terminal TPV Bancaria", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                if (!txtNumTarjeta.getText().trim().matches("^\\d{16}$")) {
                    JOptionPane.showMessageDialog(this, "Transacción rechazada: El número de tarjeta debe contener exactamente 16 dígitos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (calendarExpTarjeta.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Transacción rechazada: Seleccione la fecha de vencimiento válida.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        } else if (metodoPago.equalsIgnoreCase("Transferencia")) {
            JPanel panelTransferencia = new JPanel();
            panelTransferencia.setLayout(new BoxLayout(panelTransferencia, BoxLayout.Y_AXIS));
            JLabel lblInst = new JLabel("Indique al cliente que realice la transferencia a la siguiente cuenta:");
            lblInst.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel lblCuenta = new JLabel("Clabe Interbancaria: 0123 4567 8910 1112 13");
            lblCuenta.setFont(new Font("Arial", Font.BOLD, 16));
            lblCuenta.setForeground(new Color(0, 102, 204));
            lblCuenta.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel lblTotal = new JLabel("Monto a transferir: $ " + String.format("%.2f", ultimoTotal));
            lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
            lblTotal.setForeground(new Color(0, 153, 51));
            lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelTransferencia.add(lblInst);
            panelTransferencia.add(Box.createVerticalStrut(15));
            panelTransferencia.add(lblCuenta);
            panelTransferencia.add(Box.createVerticalStrut(15));
            panelTransferencia.add(lblTotal);
            int result = JOptionPane.showConfirmDialog(this, panelTransferencia, "Transferencia Bancaria", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                numeroAutorizacionTransferencia = JOptionPane.showInputDialog(this, "Ingrese el número de autorización de la transferencia para validar el pago:", "Validación de Fondos", JOptionPane.QUESTION_MESSAGE);
                if (numeroAutorizacionTransferencia == null || numeroAutorizacionTransferencia.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Registro interrumpido: Es obligatorio validar el número de autorización.", "Validación Fallida", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        }

        try (Connection cn = ConexionBD.getConexion()) {
            cn.setAutoCommit(false);
            int empleadoID = 0, clienteID = 0;

            try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT empleado_id FROM empleado WHERE CONCAT_WS(' ', nombre, ap_paterno) = ? OR nombre = ? LIMIT 1")) {
                ps.setString(1, cajeroEnSesion);
                ps.setString(2, cajeroEnSesion);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    empleadoID = rs.getInt("empleado_id");
                }
            }
            if (empleadoID == 0) {
                JOptionPane.showMessageDialog(this, "Error crítico: No se pudo localizar el ID del cajero ('" + cajeroEnSesion + "').", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = cn.prepareStatement("SELECT cliente_id FROM cliente WHERE telefono = ?")) {
                ps.setString(1, Telefono);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    clienteID = rs.getInt("cliente_id");
                }
            }
            if (clienteID == 0) {
                JOptionPane.showMessageDialog(this, "El cliente actual no está guardado en la BD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int metodoPagoId = cboMetodoPago.getSelectedIndex();
            int estadoVentaId = estadoPago.equalsIgnoreCase("Pagado") ? 2 : 1;

            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO venta (empleado_id, cliente_id, metodo_pago_id, estado_venta_id, fecha_venta, fecha_entrega_programada, total) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, empleadoID);
                ps.setInt(2, clienteID);
                ps.setInt(3, metodoPagoId);
                ps.setInt(4, estadoVentaId);
                ps.setTimestamp(5, new Timestamp(FechaVenta.getTime()));
                ps.setTimestamp(6, new Timestamp(FechaEntrega.getTime()));
                ps.setDouble(7, ultimoTotal);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    ultimaVentaId = rs.getInt(1);
                }
            }
            ultimoClienteId = clienteID;

            try (PreparedStatement psDetalle = cn.prepareStatement(
                    "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, (SELECT producto_id FROM producto WHERE nombre = ? LIMIT 1), ?, ?, ?)"); PreparedStatement psStock = cn.prepareStatement(
                            "UPDATE inventario_producto SET stock_disponible = stock_disponible - ? WHERE producto_id = (SELECT producto_id FROM producto WHERE nombre = ? LIMIT 1)")) {
                for (Object[] prod : productosTemp) {
                    String np = (String) prod[0];
                    int cant = (int) prod[1];
                    double prec = (double) prod[2];
                    psDetalle.setInt(1, ultimaVentaId);
                    psDetalle.setString(2, np);
                    psDetalle.setInt(3, cant);
                    psDetalle.setDouble(4, prec);
                    psDetalle.setDouble(5, cant * prec);
                    psDetalle.executeUpdate();
                    psStock.setInt(1, cant);
                    psStock.setString(2, np);
                    psStock.executeUpdate();
                }
            }

            cn.commit();
            JOptionPane.showMessageDialog(this, "Venta #" + ultimaVentaId + " procesada con éxito.\nEl inventario ha sido actualizado.", "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);

            try {
                PreparedStatement psCorreo = cn.prepareStatement("SELECT correo FROM cliente WHERE cliente_id = ?");
                psCorreo.setInt(1, clienteID);
                ResultSet rsCorreo = psCorreo.executeQuery();
                if (rsCorreo.next()) {
                    String correoDestino = rsCorreo.getString("correo");
                    if (correoDestino != null && !correoDestino.isEmpty()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        StringBuilder lista = new StringBuilder();
                        for (Object[] prod : productosTemp) {
                            lista.append("• ").append((int) prod[1]).append("x ").append((String) prod[0]).append("\n");
                        }
                        EnviarCorreo.enviarTicketVenta(correoDestino, NombreCliente, "V-" + ultimaVentaId,
                                sdf.format(FechaVenta), sdf.format(FechaEntrega), lista.toString(),
                                String.valueOf(ultimoTotal), metodoPago, estadoPago, cajeroEnSesion);
                        JOptionPane.showMessageDialog(this, "Ticket enviado al correo:\n" + correoDestino, "Correo Enviado", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "El cliente no tiene correo registrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Venta guardada, pero error al enviar correo:\n" + ex.getMessage(), "Error de Envío", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al registrar venta: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtNombreCliente.setText("");
        txtApPaterno.setText("");
        txtApMaterno.setText("");
        txtTelefono.setText("");
        txtCalle.setText("");
        txtNumExt.setText("");
        txtColonia.setText("");
        txtCiudad.setText("");
        txtEstado.setText("");
        txtCP.setText("");
        txtCorreo.setText("");
        txtCorreo.setBackground(Color.WHITE);
        calendarEntrega.setDate(null);
        cboEstadoPago.setSelectedIndex(0);
        cboMetodoPago.setSelectedIndex(0);
        productosTemp.clear();
        modTicket.setRowCount(0);
        lblGranTotal.setText("$ 0.00");
        ultimoTotal = 0;
        clienteIdBuscado = 0;
        ultimaVentaId = 0;
        btnActualizarCliente.setEnabled(false);
    }

    private void crearInterfazGrafica() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(new Color(245, 230, 211));

        // =====================================================================
        // HEADER: [cajero + imagen | TÍTULO CENTRAL | Cerrar Sesión]
        // =====================================================================
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(255, 244, 228));
        panelHeader.setBorder(new EmptyBorder(8, 15, 8, 15));

        // ── IZQUIERDA: imagen usuario.png (arriba) + nombre cajero (abajo) ────
        JPanel pnlCajero = new JPanel();
        pnlCajero.setLayout(new BoxLayout(pnlCajero, BoxLayout.Y_AXIS));
        pnlCajero.setOpaque(false);
        pnlCajero.setBorder(new EmptyBorder(2, 50, 2, 20));

        // Imagen 90x90 — se carga con múltiples estrategias
        ImageIcon iconoUsuario = cargarImagenUsuario(90, 90);
        if (iconoUsuario != null) {
            JLabel lblIcono = new JLabel(iconoUsuario);
            lblIcono.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlCajero.add(lblIcono);
        } else {
            // Placeholder si no se encuentra la imagen
            JLabel lblSinImg = new JLabel("👤");
            lblSinImg.setFont(new Font("Segoe UI", Font.PLAIN, 50));
            lblSinImg.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlCajero.add(lblSinImg);
        }

        pnlCajero.add(Box.createVerticalStrut(4));

        JLabel lblCajeroTexto = new JLabel("Cajero en turno:");
        lblCajeroTexto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCajeroTexto.setForeground(new Color(100, 100, 100));
        lblCajeroTexto.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCajero.add(lblCajeroTexto);

        // Nombre — fuente grande naranja (se actualiza en configurarLogicaInicial)
        lblCajeroNombre = new JLabel(cajeroEnSesion);
        lblCajeroNombre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblCajeroNombre.setForeground(new Color(255, 102, 51));
        lblCajeroNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCajero.add(lblCajeroNombre);

        // ── CENTRO: título ────────────────────────────────────────────────────
        JLabel lblTitulo = new JLabel("Caja: Registrar Nuevo Pedido", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(255, 102, 51));

        // ── DERECHA: botón Cerrar Sesión ──────────────────────────────────────
        JPanel pnlDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        pnlDerecha.setOpaque(false);
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(255, 51, 51));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrarSesion.setPreferredSize(new Dimension(155, 42));
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(e -> {
            try {
                new Cajero().setVisible(true);
                this.dispose();
            } catch (Exception ex) {
            }
        });
        pnlDerecha.add(btnCerrarSesion);

        panelHeader.add(pnlCajero, BorderLayout.WEST);    // imagen + nombre  ← IZQUIERDA
        panelHeader.add(lblTitulo, BorderLayout.CENTER);  // título           ← CENTRO
        panelHeader.add(pnlDerecha, BorderLayout.EAST);    // cerrar sesión    ← DERECHA

        // =====================================================================
        // PANEL CENTRAL
        // =====================================================================
        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 25, 0));
        panelCentral.setBackground(new Color(245, 230, 211));
        panelCentral.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel panelFormularioBase = new JPanel(new GridBagLayout());
        panelFormularioBase.setBackground(new Color(245, 230, 211));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 0.0;
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.anchor = GridBagConstraints.NORTH;

        // PANEL CLIENTES
        JPanel panelCliente = new JPanel(new GridLayout(8, 1, 0, 4));
        panelCliente.setBackground(new Color(255, 244, 228));
        panelCliente.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 102, 51), 2),
                "1. Buscar o Registrar Cliente", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15), new Color(255, 102, 51)));

        txtTelefono = new JTextField();
        btnBuscarCliente = new JButton("🔍 Buscar");
        btnBuscarCliente.setBackground(new Color(0, 153, 51));
        btnBuscarCliente.setForeground(Color.WHITE);
        btnBuscarCliente.addActionListener(e -> buscarClientePorTelefono());

        txtNombreCliente = new JTextField();
        txtApPaterno = new JTextField();
        txtApMaterno = new JTextField();
        txtCorreo = new JTextField();
        txtCalle = new JTextField();
        txtNumExt = new JTextField();
        txtColonia = new JTextField();
        txtCiudad = new JTextField();
        txtEstado = new JTextField();
        txtCP = new JTextField();

        txtCP.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String cp = txtCP.getText().trim();
                if (cp.length() == 5) {
                    txtEstado.setText("Buscando...");
                    txtColonia.setText("Buscando...");
                    txtCiudad.setText("Buscando...");
                    new Thread(() -> {
                        try {
                            java.net.URL url = new java.net.URL("https://api.zippopotam.us/mx/" + cp);
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            if (conn.getResponseCode() == 200) {
                                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                                String line;
                                StringBuilder sb = new StringBuilder();
                                while ((line = in.readLine()) != null) {
                                    sb.append(line);
                                }
                                in.close();
                                String json = sb.toString();
                                String edo = json.contains("\"state\":") ? json.split("\"state\":\\s*\"")[1].split("\"")[0] : "";
                                java.util.List<String> lugares = new java.util.ArrayList<>();
                                String[] parts = json.split("\"place name\":\\s*\"");
                                for (int i = 1; i < parts.length; i++) {
                                    String lug = parts[i].split("\"")[0];
                                    if (!lugares.contains(lug)) {
                                        lugares.add(lug);
                                    }
                                }
                                final String edoF = edo;
                                final java.util.List<String> lugF = lugares;
                                SwingUtilities.invokeLater(() -> {
                                    txtEstado.setText(edoF);
                                    if (lugF.isEmpty()) {
                                        txtColonia.setText("");
                                        txtCiudad.setText("");
                                        txtColonia.requestFocus();
                                    } else if (lugF.size() == 1) {
                                        txtColonia.setText(lugF.get(0));
                                        txtCiudad.setText(lugF.get(0));
                                        txtCalle.requestFocus();
                                    } else {
                                        String[] ops = lugF.toArray(new String[0]);
                                        String sel = (String) JOptionPane.showInputDialog(Registro_Venta.this,
                                                "Seleccione la ubicación correspondiente al C.P. " + cp + ":",
                                                "Opciones Encontradas", JOptionPane.QUESTION_MESSAGE, null, ops, ops[0]);
                                        if (sel != null) {
                                            txtColonia.setText(sel);
                                            txtCiudad.setText(sel);
                                        } else {
                                            txtColonia.setText("");
                                            txtCiudad.setText("");
                                        }
                                        txtCalle.requestFocus();
                                    }
                                });
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    txtEstado.setText("");
                                    txtColonia.setText("");
                                    txtCiudad.setText("");
                                });
                            }
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                txtEstado.setText("");
                                txtColonia.setText("");
                                txtCiudad.setText("");
                            });
                        }
                    }).start();
                } else if (cp.length() < 5) {
                    txtEstado.setText("");
                    txtColonia.setText("");
                    txtCiudad.setText("");
                }
            }
        });

        panelCliente.add(crearFilaFormulario("Teléfono:", txtTelefono, btnBuscarCliente));
        panelCliente.add(crearFilaFormulario("Nombre(s):", txtNombreCliente));
        panelCliente.add(crearFilaFormulario("Apellidos:", txtApPaterno, txtApMaterno));
        panelCliente.add(crearFilaFormulario("Correo Electrónico:", txtCorreo));
        panelCliente.add(crearFilaFormulario("Calle:", txtCalle, crearFilaFormulario("Número Exterior: ", txtNumExt)));
        panelCliente.add(crearFilaFormulario("Colonia:", txtColonia, crearFilaFormulario("Código Postal: ", txtCP)));
        panelCliente.add(crearFilaFormulario("Ciudad:", txtCiudad, crearFilaFormulario("Estado: ", txtEstado)));

        JPanel pnlBotonesCliente = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        pnlBotonesCliente.setOpaque(false);
        btnNuevoCliente = new JButton("Validar Cliente Nuevo");
        btnActualizarCliente = new JButton("Actualizar Existente");
        btnNuevoCliente.addActionListener(e -> registrarNuevoCliente());
        btnActualizarCliente.addActionListener(e -> actualizarCliente());
        pnlBotonesCliente.add(btnNuevoCliente);
        pnlBotonesCliente.add(btnActualizarCliente);
        panelCliente.add(pnlBotonesCliente);
        panelFormularioBase.add(panelCliente, gbcForm);

        // PANEL VENTA
        JPanel panelVenta = new JPanel(new GridLayout(4, 1, 0, 4));
        panelVenta.setBackground(new Color(255, 244, 228));
        panelVenta.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 153, 51), 2),
                "2. Datos de Pago y Entrega", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15), new Color(0, 153, 51)));

        cboEmpleado = new JComboBox<>();
        calendarVenta = new JDateChooser();
        calendarEntrega = new JDateChooser();
        cboEstadoPago = new JComboBox<>(new String[]{"Seleccionar", "Pagado", "Pendiente"});
        cboMetodoPago = new JComboBox<>(new String[]{"Seleccionar", "Efectivo", "Tarjeta de crédito", "Transferencia"});

        panelVenta.add(crearFilaFormulario("Cajero en Turno:", cboEmpleado));
        panelVenta.add(crearFilaFormulario("Fecha de Entrega:", calendarEntrega));
        panelVenta.add(crearFilaFormulario("Método de Pago:", cboMetodoPago));
        panelVenta.add(crearFilaFormulario("Estado de Pago:", cboEstadoPago));

        gbcForm.gridy = 1;
        gbcForm.insets = new Insets(8, 0, 0, 0);
        panelFormularioBase.add(panelVenta, gbcForm);
        gbcForm.gridy = 2;
        gbcForm.weighty = 1.0;
        panelFormularioBase.add(Box.createVerticalGlue(), gbcForm);

        JScrollPane scrollIzquierdo = new JScrollPane(panelFormularioBase);
        scrollIzquierdo.setBorder(null);
        scrollIzquierdo.setOpaque(false);
        scrollIzquierdo.getViewport().setOpaque(false);
        scrollIzquierdo.getVerticalScrollBar().setUnitIncrement(16);

        // TICKET
        JPanel panelTicket = new JPanel(new BorderLayout(0, 15));
        panelTicket.setBackground(new Color(255, 244, 228));
        panelTicket.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
                "3. Resumen del Pedido (Ticket)", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15), new Color(0, 102, 204)));

        btnProductos = new JButton("ABRIR CATÁLOGO PARA AGREGAR PRODUCTOS");
        btnProductos.setBackground(new Color(0, 102, 204));
        btnProductos.setForeground(Color.WHITE);
        btnProductos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnProductos.setPreferredSize(new Dimension(0, 45));
        btnProductos.addActionListener(e -> {
            try {
                new Catalogo(Registro_Venta.this).setVisible(true);
            } catch (Exception ex) {
            }
        });

        modTicket = new DefaultTableModel(new String[]{"Producto", "Cant", "Precio U.", "Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaTicket = new JTable(modTicket);
        tablaTicket.setRowHeight(25);
        tablaTicket.getTableHeader().setBackground(new Color(50, 50, 50));
        tablaTicket.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollTicket = new JScrollPane(tablaTicket);

        JPanel pnlTotal = new JPanel(new GridLayout(2, 1));
        pnlTotal.setOpaque(false);
        pnlTotal.setBorder(new EmptyBorder(5, 0, 10, 0));
        JLabel lblTextoTotal = new JLabel("TOTAL A COBRAR:", SwingConstants.CENTER);
        lblTextoTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTextoTotal.setForeground(Color.GRAY);
        lblGranTotal = new JLabel("$ 0.00", SwingConstants.CENTER);
        lblGranTotal.setFont(new Font("Arial Black", Font.BOLD, 55));
        lblGranTotal.setForeground(new Color(204, 0, 0));
        pnlTotal.add(lblTextoTotal);
        pnlTotal.add(lblGranTotal);

        panelTicket.add(btnProductos, BorderLayout.NORTH);
        panelTicket.add(scrollTicket, BorderLayout.CENTER);
        panelTicket.add(pnlTotal, BorderLayout.SOUTH);

        panelCentral.add(scrollIzquierdo);
        panelCentral.add(panelTicket);

        // FOOTER
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        panelFooter.setBackground(new Color(245, 230, 211));

        btnRegistrar = new JButton("FINALIZAR Y REGISTRAR VENTA");
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnRegistrar.setBackground(new Color(0, 153, 51));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setPreferredSize(new Dimension(350, 50));
        btnRegistrar.addActionListener(e -> registrarVenta());

        btnAnticipo = new JButton("Registrar Anticipo");
        btnAnticipo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAnticipo.setBackground(new Color(255, 153, 0));
        btnAnticipo.setForeground(Color.WHITE);
        btnAnticipo.setPreferredSize(new Dimension(250, 50));
        btnAnticipo.addActionListener(e -> {
            if (ultimaVentaId == 0) {
                JOptionPane.showMessageDialog(this, "Primero debes 'FINALIZAR Y REGISTRAR VENTA'.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                new Anticipo(ultimaVentaId, ultimoClienteId, txtNombreCliente.getText(), ultimoTotal).setVisible(true);
                limpiarFormulario();
            } catch (Exception ex) {
            }
        });

        panelFooter.add(btnRegistrar);
        panelFooter.add(btnAnticipo);

        panelFondo.add(panelHeader, BorderLayout.NORTH);
        panelFondo.add(panelCentral, BorderLayout.CENTER);
        panelFondo.add(panelFooter, BorderLayout.SOUTH);
        setContentPane(panelFondo);
        pack();
    }

    private JPanel crearFilaFormulario(String textoEtiqueta, Component... componentes) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(textoEtiqueta);
        lbl.setPreferredSize(new Dimension(120, 28));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl, BorderLayout.WEST);
        JPanel pnlCampos = new JPanel(new GridLayout(1, componentes.length, 10, 0));
        pnlCampos.setOpaque(false);
        for (Component c : componentes) {
            c.setPreferredSize(new Dimension(100, 28));
            pnlCampos.add(c);
        }
        panel.add(pnlCampos, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
        }
        java.awt.EventQueue.invokeLater(() -> new Registro_Venta().setVisible(true));
    }
}
