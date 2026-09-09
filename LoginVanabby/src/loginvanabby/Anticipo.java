package loginvanabby;


import conexion.ConexionBD;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Interfaz de Anticipos Automatizada - Repostería Vanabby
 */
public class Anticipo extends javax.swing.JFrame {

    private int ventaId;
    private int clienteId;
    private String nombreCliente;
    private double totalVenta;
    private Date fechaLimiteVenta;

    // Componentes UI
    private JTextField txtAnticipo;
    private JTextField txtRestante; // Ahora de Solo Lectura
    private JTextField txtFechaRegistro;
    private JTextField txtFechaLimite;
    private JComboBox<String> cboEstado;
    private JButton btnAnticipar, btnVolver;
    private JLabel lblTotalVenta;

    public Anticipo() {
        this(0, 0, "Desconocido", 0.0);
    }

    public Anticipo(int ventaId, int clienteId, String nombreCliente, double totalVenta) {
        this.ventaId = ventaId;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.totalVenta = totalVenta;

        obtenerFechaLimiteDeBD(); // Recupera la fecha de entrega de la BD
        crearInterfazGrafica();
        configurarValoresAutomaticos();
    }

    private void obtenerFechaLimiteDeBD() {
        if (ventaId == 0) return;
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT fecha_entrega_programada FROM venta WHERE venta_id = ?")) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fechaLimiteVenta = rs.getTimestamp("fecha_entrega_programada");
                }
            }
        } catch (SQLException e) {
            System.out.println("No se pudo obtener la fecha límite: " + e.getMessage());
        }
    }

    private void configurarValoresAutomaticos() {
        setTitle("Registro de Anticipos - Vanabby");
        this.setLocationRelativeTo(null); // Centrar en pantalla
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date hoy = new Date();

        // 1. Fechas Automáticas (Bloqueadas para edición manual)
        txtFechaRegistro.setText(sdf.format(hoy));
        txtFechaRegistro.setEditable(false);
        txtFechaRegistro.setBackground(new Color(230, 230, 230));

        if (fechaLimiteVenta != null) {
            txtFechaLimite.setText(new SimpleDateFormat("dd/MM/yyyy").format(fechaLimiteVenta));
        } else {
            txtFechaLimite.setText("Sin fecha límite definida");
        }
        txtFechaLimite.setEditable(false);
        txtFechaLimite.setBackground(new Color(230, 230, 230));

        // 2. Reflejo del Total y Cálculo Sugerido (30%)
        lblTotalVenta.setText("TOTAL DEL PEDIDO: $ " + String.format("%.2f", totalVenta));
        
        if (totalVenta > 0) {
            double anticipoSugerido = Math.round(totalVenta * 0.30 * 100.0) / 100.0;
            double restanteSugerido = Math.round((totalVenta - anticipoSugerido) * 100.0) / 100.0;
            
            txtAnticipo.setText(String.valueOf(anticipoSugerido));
            txtRestante.setText(String.valueOf(restanteSugerido));
        } else {
            txtRestante.setText("0.00");
        }

        // Bloquear restante para que sea calculado automáticamente
        txtRestante.setEditable(false);
        txtRestante.setBackground(new Color(230, 230, 230));
        txtRestante.setForeground(new Color(204, 0, 0)); // Rojo para deuda
        txtRestante.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // 3. Listener: Si el cajero cambia el anticipo, el restante se ajusta automáticamente
        txtAnticipo.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { recalcularRestante(); }
            public void removeUpdate(DocumentEvent e) { recalcularRestante(); }
            public void insertUpdate(DocumentEvent e) { recalcularRestante(); }
        });
    }

    private void recalcularRestante() {
        try {
            double montoAnticipo = Double.parseDouble(txtAnticipo.getText().trim());
            if (montoAnticipo > totalVenta) {
                txtRestante.setText("Error: Excede Total");
            } else {
                double nuevoRestante = Math.round((totalVenta - montoAnticipo) * 100.0) / 100.0;
                txtRestante.setText(String.valueOf(nuevoRestante));
            }
        } catch (NumberFormatException e) {
            txtRestante.setText(String.valueOf(totalVenta)); // Si borra todo, debe el total
        }
    }

    private void registrarAnticipoEnBD() {
        if (ventaId == 0) {
            JOptionPane.showMessageDialog(this, "No hay una venta asociada a este anticipo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (txtAnticipo.getText().trim().isEmpty() || txtRestante.getText().trim().isEmpty() || cboEstado.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Debe llenar el monto anticipado y seleccionar el estado del pago.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double montoAnticipado;
        double montoRestante;

        try {
            montoAnticipado = Double.parseDouble(txtAnticipo.getText().trim());
            montoRestante = Double.parseDouble(txtRestante.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Los montos deben ser números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (montoAnticipado <= 0) {
            JOptionPane.showMessageDialog(this, "El monto anticipado debe ser mayor a $0.00", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (montoRestante < 0) {
            JOptionPane.showMessageDialog(this, "El anticipo no puede ser mayor al total de la venta.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = ConexionBD.getConexion()) {
            
            // 1. Obtener ID del estado
            int estadoAnticipoId = 1; // PENDIENTE por defecto
            String nombreEstado = cboEstado.getSelectedItem().toString().toUpperCase();
            try (PreparedStatement psEstado = con.prepareStatement("SELECT estado_anticipo_id FROM estado_anticipo WHERE nombre_estado = ?")) {
                psEstado.setString(1, nombreEstado);
                ResultSet rsEstado = psEstado.executeQuery();
                if (rsEstado.next()) estadoAnticipoId = rsEstado.getInt("estado_anticipo_id");
            }

            // 2. Insertar Anticipo (COLUMNAS CORREGIDAS: fecha_registro y fecha_limite)
            String sqlAnticipo = "INSERT INTO anticipo (venta_id, cliente_id, monto_anticipo, monto_restante, fecha_registro, fecha_limite, estado_anticipo_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psAnticipo = con.prepareStatement(sqlAnticipo)) {
                psAnticipo.setInt(1, ventaId);
                psAnticipo.setInt(2, clienteId);
                psAnticipo.setDouble(3, montoAnticipado);
                psAnticipo.setDouble(4, montoRestante);
                
                // Fecha registro = Ahora
                psAnticipo.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                
                // Fecha límite = Fecha de la venta
                if (fechaLimiteVenta != null) {
                    psAnticipo.setDate(6, new java.sql.Date(fechaLimiteVenta.getTime()));
                } else {
                    psAnticipo.setNull(6, java.sql.Types.DATE); 
                }
                
                psAnticipo.setInt(7, estadoAnticipoId);
                psAnticipo.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Anticipo registrado correctamente para la Venta #" + ventaId, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // 3. Enviar Correo
            enviarCorreoAnticipo(montoAnticipado, montoRestante);

            this.dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el anticipo en la BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarCorreoAnticipo(double anticipo, double restante) {
        try (Connection con = ConexionBD.getConexion()) {
            String sqlCorreo = "SELECT CONCAT_WS(' ', nombre, ap_paterno, ap_materno) AS nombre_completo, correo FROM cliente WHERE cliente_id = ?";
            try (PreparedStatement psCorreo = con.prepareStatement(sqlCorreo)) {
                psCorreo.setInt(1, clienteId);
                ResultSet rsCorreo = psCorreo.executeQuery();

                if (rsCorreo.next()) {
                    String correoCliente = rsCorreo.getString("correo");
                    String nombreCl = rsCorreo.getString("nombre_completo");

                    if (correoCliente != null && !correoCliente.isEmpty()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        EnviarCorreo.enviarTicketAnticipo(
                                correoCliente,
                                nombreCl,
                                "A-" + System.currentTimeMillis(), 
                                sdf.format(new Date()),
                                String.valueOf(anticipo),
                                String.valueOf(totalVenta),
                                String.valueOf(restante),
                                "Anticipo de pedido - Venta #" + ventaId,
                                "Vanabby"
                        );
                        JOptionPane.showMessageDialog(this, "El comprobante del anticipo ha sido enviado al correo: " + correoCliente, "Correo Enviado", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "El anticipo se guardó, pero falló el envío de correo:\n" + ex.getMessage(), "Error de Correo", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =========================================================================
    // CREACIÓN DE INTERFAZ GRÁFICA (SIN AMONTONAMIENTO)
    // =========================================================================
    private void crearInterfazGrafica() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(550, 600); // Tamaño fijo perfecto para esta ventana
        setResizable(false);
        
        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(new Color(255, 244, 228));

        // HEADER
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(245, 230, 211));
        panelHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitulo = new JLabel("Registrar Anticipo", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(new Color(255, 102, 51));
        
        btnVolver = new JButton("Volver");
        btnVolver.setBackground(new Color(255, 51, 51)); btnVolver.setForeground(Color.WHITE);
        btnVolver.addActionListener(e -> this.dispose());
        
        panelHeader.add(btnVolver, BorderLayout.WEST);
        panelHeader.add(lblTitulo, BorderLayout.CENTER);

        // CENTRO (FORMULARIO)
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(15, 20, 15, 20); gbc.weightx = 1.0;

        lblTotalVenta = new JLabel("TOTAL DEL PEDIDO: $ 0.00", SwingConstants.CENTER);
        lblTotalVenta.setFont(new Font("Arial Black", Font.BOLD, 22));
        lblTotalVenta.setForeground(new Color(0, 102, 204));
        
        txtAnticipo = new JTextField(); txtAnticipo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtRestante = new JTextField(); 
        txtFechaRegistro = new JTextField(); 
        txtFechaLimite = new JTextField(); 
        cboEstado = new JComboBox<>(new String[]{"Seleccionar", "Pendiente", "Pagado"});

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panelFormulario.add(lblTotalVenta, gbc);
        
        gbc.gridwidth = 1;
        agregarFilaGridBag(panelFormulario, gbc, 1, "Monto Anticipado: $", txtAnticipo);
        agregarFilaGridBag(panelFormulario, gbc, 2, "Monto Restante: $", txtRestante);
        agregarFilaGridBag(panelFormulario, gbc, 3, "Fecha Límite Pago:", txtFechaLimite);
        agregarFilaGridBag(panelFormulario, gbc, 4, "Fecha de Registro:", txtFechaRegistro);
        agregarFilaGridBag(panelFormulario, gbc, 5, "Estado del Anticipo:", cboEstado);

        // FOOTER
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelFooter.setBackground(new Color(245, 230, 211));
        panelFooter.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        btnAnticipar = new JButton("GUARDAR ANTICIPO");
        btnAnticipar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnAnticipar.setBackground(new Color(0, 153, 51)); btnAnticipar.setForeground(Color.WHITE);
        btnAnticipar.setPreferredSize(new Dimension(300, 50));
        btnAnticipar.addActionListener(e -> registrarAnticipoEnBD());
        
        panelFooter.add(btnAnticipar);

        panelFondo.add(panelHeader, BorderLayout.NORTH);
        panelFondo.add(panelFormulario, BorderLayout.CENTER);
        panelFondo.add(panelFooter, BorderLayout.SOUTH);

        setContentPane(panelFondo);
    }

    private void agregarFilaGridBag(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, Component componente) {
        gbc.gridy = fila; 
        gbc.gridx = 0; gbc.weightx = 0.4;
        JLabel lbl = new JLabel(etiqueta, SwingConstants.RIGHT); 
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        if(componente instanceof JTextField) ((JTextField) componente).setPreferredSize(new Dimension(0, 35));
        if(componente instanceof JComboBox) ((JComboBox) componente).setPreferredSize(new Dimension(0, 35));
        panel.add(componente, gbc);
    }

    public static void main(String args[]) {
        try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } } catch (Exception ex) {}
        EventQueue.invokeLater(() -> new Anticipo().setVisible(true));
    }
}