package loginvanabby;


import dao.UsuarioDAO;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Login extends javax.swing.JFrame {

    public static Administrador admin;
    public static Gerente gere;
    public static Cajero caja;
    public static Repostero rep;
    public static Marketing mark;
    public static Supervisor sup;
    public static Cliente clien;

    // ── GUARDA EL NOMBRE REAL DEL USUARIO ACTIVO ──
    public static String usuarioActual = "";

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Login() {
        initComponents();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login - Repostería Vanabby");
        this.lblOjoCerrado.setVisible(false);
        this.setLocationRelativeTo(null);
        btnIngresar.putClientProperty("JButton.buttonType", "roundRect");
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void mostrarError(String mensaje) {
        lblErrorIngreso.setText(mensaje);
        lblErrorIngreso.setForeground(Color.RED);
    }

    private void mostrarExito(String mensaje) {
        lblErrorIngreso.setText(mensaje);
        lblErrorIngreso.setForeground(new Color(0, 100, 0));
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblCorreo = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        lblContra = new javax.swing.JLabel();
        pswContra = new javax.swing.JPasswordField();
        jLabel8 = new javax.swing.JLabel();
        lblOjoAbierto = new javax.swing.JLabel();
        lblOjoCerrado = new javax.swing.JLabel();
        lblErrorIngreso = new javax.swing.JLabel();
        btnIngresar = new javax.swing.JButton();
        lblCorreo1 = new javax.swing.JLabel();
        lblCliente = new javax.swing.JLabel();
        lblRecuperar = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(245, 230, 211));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 244, 228));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png")));
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 0, 200, 190));

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 36));
        jLabel2.setForeground(new java.awt.Color(0, 153, 0));
        jLabel2.setText("BIENVENIDO");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 190, 270, 30));

        lblCorreo.setBackground(new java.awt.Color(204, 204, 204));
        lblCorreo.setFont(new java.awt.Font("Arial", 0, 14));
        lblCorreo.setForeground(new java.awt.Color(153, 153, 153));
        lblCorreo.setText("Ingrese su nombre de usuario");
        jPanel1.add(lblCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 300, 230, 20));

        jLabel5.setFont(new java.awt.Font("Arial Black", 0, 18));
        jLabel5.setForeground(new java.awt.Color(0, 153, 51));
        jLabel5.setText("Contraseña");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 360, 130, 20));

        txtUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtUsuarioMouseClicked(evt);
            }
        });
        txtUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtUsuarioKeyPressed(evt);
            }
        });
        jPanel1.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 290, 380, 50));

        jLabel6.setFont(new java.awt.Font("Arial Black", 0, 18));
        jLabel6.setForeground(new java.awt.Color(0, 153, 51));
        jLabel6.setText("Usuario");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 260, -1, -1));

        lblContra.setBackground(new java.awt.Color(204, 204, 204));
        lblContra.setFont(new java.awt.Font("Arial", 0, 14));
        lblContra.setForeground(new java.awt.Color(153, 153, 153));
        lblContra.setText("Ingrese su contraseña");
        jPanel1.add(lblContra, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 400, 230, 20));

        pswContra.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pswContraMouseClicked(evt);
            }
        });
        pswContra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pswContraKeyPressed(evt);
            }
        });
        jPanel1.add(pswContra, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, 380, 50));

        jLabel8.setBackground(new java.awt.Color(204, 204, 204));
        jLabel8.setFont(new java.awt.Font("Arial", 0, 18));
        jLabel8.setForeground(new java.awt.Color(153, 153, 153));
        jLabel8.setText("Inicia sesión en tú cuenta");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 220, 230, 20));

        lblOjoAbierto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/ojo (4).png")));
        lblOjoAbierto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOjoAbiertoMouseClicked(evt);
            }
        });
        jPanel1.add(lblOjoAbierto, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 400, -1, -1));

        lblOjoCerrado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/cerrar-ojo.png")));
        lblOjoCerrado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOjoCerradoMouseClicked(evt);
            }
        });
        jPanel1.add(lblOjoCerrado, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 390, 40, 50));

        // ── ETIQUETA: RECUPERAR CONTRASEÑA ──
        lblRecuperar.setFont(new java.awt.Font("Arial", 1, 12));
        lblRecuperar.setForeground(new java.awt.Color(0, 102, 204));
        lblRecuperar.setText("¿Olvidaste tu contraseña?");
        lblRecuperar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblRecuperar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                recuperarContrasena();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRecuperar.setForeground(new java.awt.Color(255, 102, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRecuperar.setForeground(new java.awt.Color(0, 102, 204));
            }
        });
        jPanel1.add(lblRecuperar, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 445, 170, 20));

        lblErrorIngreso.setFont(new java.awt.Font("Arial", 1, 14));
        jPanel1.add(lblErrorIngreso, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 475, 450, 40));

        btnIngresar.setBackground(new java.awt.Color(255, 102, 51));
        btnIngresar.setFont(new java.awt.Font("Arial Black", 0, 18));
        btnIngresar.setForeground(new java.awt.Color(255, 255, 255));
        btnIngresar.setText("INGRESAR");
        btnIngresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIngresarActionPerformed(evt);
            }
        });
        jPanel1.add(btnIngresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 520, 310, 40));

        lblCorreo1.setBackground(new java.awt.Color(204, 204, 204));
        lblCorreo1.setFont(new java.awt.Font("Arial", 0, 14));
        lblCorreo1.setForeground(new java.awt.Color(153, 153, 153));
        lblCorreo1.setText("Ingrese su nombre de usuario");
        jPanel1.add(lblCorreo1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 300, 230, 20));

        lblCliente.setText("Ingresar como cliente");
        lblCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblClienteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblClienteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblClienteMouseExited(evt);
            }
        });
        jPanel1.add(lblCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 580, 130, 20));

        jPanel2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 60, 560, 620));
        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1540, 890));

        pack();
    }

    // =====================================================================
    // LÓGICA DE RECUPERACIÓN DE CONTRASEÑA (ASIGNA CONTRASEÑA TEMPORAL)
    // =====================================================================
    private void recuperarContrasena() {
        String inputUsuario = JOptionPane.showInputDialog(this, 
            "Ingrese su nombre de usuario para recuperar su contraseña:", 
            "Recuperar Contraseña", 
            JOptionPane.QUESTION_MESSAGE);

        if (inputUsuario != null && !inputUsuario.trim().isEmpty()) {
            
            btnIngresar.setText("Enviando correo...");
            
            new Thread(() -> {
                try (Connection cn = conexion.ConexionBD.getConexion();
                     PreparedStatement ps = cn.prepareStatement("SELECT correo FROM empleado WHERE nombre_usuario = ?")) {
                    
                    ps.setString(1, inputUsuario.trim());
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String correoDestino = rs.getString("correo");
                            
                            if (correoDestino != null && !correoDestino.trim().isEmpty()) {
                                
                                // 1. Generar contraseña temporal aleatoria (8 caracteres)
String passTemporal = generarPasswordTemporal();

// 2. Actualizar la base de datos guardando la contraseña directamente (sin librería extra)
try (java.sql.PreparedStatement psUpd = cn.prepareStatement("UPDATE empleado SET clave_hash = ? WHERE nombre_usuario = ?")) {
    psUpd.setString(1, passTemporal);
    psUpd.setString(2, inputUsuario.trim());
    psUpd.executeUpdate();
}
// 2. Actualizar la base de datos con la nueva contraseña
                                try (PreparedStatement psUpd = cn.prepareStatement("UPDATE empleado SET clave_hash = ? WHERE nombre_usuario = ?")) {
                                    psUpd.setString(1, passTemporal); // <--- AQUÍ ESTÁ LA CORRECCIÓN
                                    psUpd.setString(2, inputUsuario.trim());
                                    psUpd.executeUpdate();
                                }
                                
                                String correoOculto = enmascararCorreo(correoDestino);
                                
                                // 4. Enviar el correo pasándole la contraseña temporal EN CLARO
                                EnviarCorreo.enviarCorreoRecuperacion(correoDestino, inputUsuario.trim(), passTemporal);

SwingUtilities.invokeLater(() -> {
    btnIngresar.setText("INGRESAR");
    JOptionPane.showMessageDialog(this, 
        "Se ha generado y enviado una contraseña temporal al correo:\n" + correoOculto, 
        "Contraseña Restablecida", 
        JOptionPane.INFORMATION_MESSAGE);
});
                                
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    btnIngresar.setText("INGRESAR");
                                    mostrarError("El usuario existe, pero no tiene un correo registrado.");
                                });
                            }
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                btnIngresar.setText("INGRESAR");
                                mostrarError("No se encontró ningún usuario con ese nombre.");
                            });
                        }
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        btnIngresar.setText("INGRESAR");
                        mostrarError("Error al restablecer contraseña: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    // Método generador de contraseñas aleatorias
    private String generarPasswordTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    // =====================================================================
    // EVENTOS DE INTERFAZ GRÁFICA
    // =====================================================================
    private void lblOjoAbiertoMouseClicked(java.awt.event.MouseEvent evt) {
        lblOjoAbierto.setVisible(false);
        lblOjoCerrado.setVisible(true);
        pswContra.setEchoChar((char) 0);
    }

    private void lblOjoCerradoMouseClicked(java.awt.event.MouseEvent evt) {
        lblOjoAbierto.setVisible(true);
        lblOjoCerrado.setVisible(false);
        pswContra.setEchoChar('●');
    }

    private void txtUsuarioMouseClicked(java.awt.event.MouseEvent evt) { lblCorreo.setVisible(false); }
    private void pswContraMouseClicked(java.awt.event.MouseEvent evt) { lblContra.setVisible(false); }
    private void txtUsuarioKeyPressed(java.awt.event.KeyEvent evt) { lblCorreo.setVisible(false); }
    private void pswContraKeyPressed(java.awt.event.KeyEvent evt) { lblContra.setVisible(false); }

    private void lblClienteMouseClicked(java.awt.event.MouseEvent evt) {
        this.setVisible(false);
        clien = new Cliente();
        clien.setVisible(true);
    }

    private void lblClienteMouseEntered(java.awt.event.MouseEvent evt) {
        lblCliente.setForeground(new java.awt.Color(255, 102, 51));
        lblCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void lblClienteMouseExited(java.awt.event.MouseEvent evt) {
        lblCliente.setForeground(new java.awt.Color(153, 153, 153));
        lblCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    // =====================================================================
    // LÓGICA DE INGRESO
    // =====================================================================
    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {
        String usuario = txtUsuario.getText().trim();
        String contra = new String(pswContra.getPassword()).trim();

        if (usuario.isEmpty() || contra.isEmpty()) {
            mostrarError("Debe completar los campos requeridos.");
            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("Validando...");

        new Thread(() -> {
            try {
                ResultSet rs = usuarioDAO.validarUsuario(usuario, contra);

                if (rs.next()) {
                    boolean cuentaBloqueada = rs.getBoolean("bloqueado");

                    if (cuentaBloqueada) {
                        SwingUtilities.invokeLater(() -> mostrarError("Su cuenta ha sido bloqueada. Contacte al administrador."));
                    } else {
                        int rolId = rs.getInt("rol_id");

                        // ── BUSCAMOS EL NOMBRE REAL DIRECTO EN LA BD ──
                        String nombreCajeroDB = usuario; 
                        
                        try (Connection cn = conexion.ConexionBD.getConexion();
                             PreparedStatement ps = cn.prepareStatement("SELECT nombre, ap_paterno FROM empleado WHERE nombre_usuario = ?")) {
                            
                            ps.setString(1, usuario);
                            try (ResultSet rsNombre = ps.executeQuery()) {
                                if (rsNombre.next()) {
                                    String n = rsNombre.getString("nombre");
                                    String ap = rsNombre.getString("ap_paterno"); 
                                    if (n != null && !n.isEmpty()) {
                                        nombreCajeroDB = n + (ap != null ? " " + ap : "");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error obteniendo nombre real: " + e.getMessage());
                        }

                        final String nombreCajeroActivo = nombreCajeroDB;

                        SwingUtilities.invokeLater(() -> {
                            mostrarExito("¡Ingreso exitoso!");
                            
                            // Guardamos el nombre real (ej: "Marta Flores") en las variables
                            usuarioActual = nombreCajeroActivo; 
                            
                            try {
                                Registro_Venta.cajeroEnSesion = nombreCajeroActivo; 
                            } catch (Exception ex) {
                                // Ignorar si Registro_Venta no está importado
                            }
                            
                            this.setVisible(false);
                            abrirModuloPorRol(rolId);
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() -> mostrarError("Usuario o contraseña incorrectos."));
                }
                rs.close();

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> mostrarError("Error al conectar con el servidor: " + e.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("INGRESAR");
                });
            }
        }).start();
    }

    private void abrirModuloPorRol(int rolId) {
        try {
            switch (rolId) {
                case 1 -> {
                    admin = new Administrador();
                    admin.setVisible(true);
                }
                case 2 -> {
                    gere = new Gerente();
                    gere.setVisible(true);
                }
                case 3 -> {
                    rep = new Repostero();
                    rep.setVisible(true);
                }
                case 4 -> {
                    caja = new Cajero(usuarioActual);
                    caja.setVisible(true);
                }
                case 5 -> {
                    sup = new Supervisor();
                    sup.setVisible(true);
                }
                case 6 -> {
                    mark = new Marketing();
                    mark.setVisible(true);
                }
                default ->
                    mostrarError("El usuario no tiene un rol válido asignado.");
            }
        } catch (Exception ex) {
            mostrarError("Error al abrir el módulo: " + ex.getMessage());
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName()) || "Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration
    private javax.swing.JButton btnIngresar;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblContra;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblCorreo1;
    private javax.swing.JLabel lblErrorIngreso;
    private javax.swing.JLabel lblOjoAbierto;
    private javax.swing.JLabel lblOjoCerrado;
    private javax.swing.JLabel lblRecuperar;
    private javax.swing.JPasswordField pswContra;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration
// Método auxiliar para ocultar parte del correo (ej. ma****@gmail.com)
    private String enmascararCorreo(String correo) {
        try {
            String[] partes = correo.split("@");
            String nombre = partes[0];
            String dominio = partes[1];
            
            if (nombre.length() > 2) {
                nombre = nombre.substring(0, 2) + "****" + nombre.substring(nombre.length() - 1);
            } else {
                nombre = nombre.substring(0, 1) + "****";
            }
            return nombre + "@" + dominio;
        } catch (Exception e) {
            return correo; // Devuelve el original si algo falla
        }
    }
   
}