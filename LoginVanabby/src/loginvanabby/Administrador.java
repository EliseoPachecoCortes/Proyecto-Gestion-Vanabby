package loginvanabby;

import conexion.ConexionBD;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.Image;

/**
 * Módulo de Administrador - Gestión de Usuarios y Permisos
 */
public class Administrador extends javax.swing.JFrame {

    private Connection cn;
    private int idUsuarioSeleccionado = -1;
    private boolean usuarioSeleccionadoEstaBloqueado = false;

    // Control de visibilidad de contraseña
    private boolean contraVisible = false;
    private ImageIcon iconoOjo;
    private ImageIcon iconoCerrarOjo;

    // Scheduler para respaldo automático
    private ScheduledExecutorService schedulerRespaldo;

    public Administrador() throws SQLException {
        this.cn = ConexionBD.getConexion();
        cargarIconosOjo();
        initComponents();
        configurarUIProfesional();
        cargarRoles();
        cargarUsuarios();

        tablaUsuarios.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                seleccionarUsuario();
            }
        });

        // Iniciar respaldo automático cada 12 horas
        iniciarRespaldoAutomatico();
    }

    // =========================================================================
    // CERRAR VENTANA - detener scheduler limpiamente
    // =========================================================================
    @Override
    public void dispose() {
        if (schedulerRespaldo != null && !schedulerRespaldo.isShutdown()) {
            schedulerRespaldo.shutdown();
        }
        super.dispose();
    }

    // =========================================================================
    // CARGA DE ÍCONOS OJO
    // =========================================================================
    private void cargarIconosOjo() {
        try {
            ImageIcon rawOjo = new ImageIcon(getClass().getResource("/imagenes/ojo (4).png"));
            Image imgOjo = rawOjo.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            iconoOjo = new ImageIcon(imgOjo);
        } catch (Exception e) {
            iconoOjo = null;
        }
        try {
            ImageIcon rawCerrar = new ImageIcon(getClass().getResource("/imagenes/cerrar-ojo.png"));
            Image imgCerrar = rawCerrar.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            iconoCerrarOjo = new ImageIcon(imgCerrar);
        } catch (Exception e) {
            iconoCerrarOjo = null;
        }
    }

    // =========================================================================
    // ALTERNAR VISIBILIDAD DE CONTRASEÑA
    // =========================================================================
    private void toggleContra() {
        contraVisible = !contraVisible;
        if (contraVisible) {
            txtContra.setEchoChar((char) 0);
            btnMostrarContra.setIcon(iconoCerrarOjo != null ? iconoCerrarOjo : null);
            if (iconoCerrarOjo == null) {
                btnMostrarContra.setText("🙈");
            }
        } else {
            txtContra.setEchoChar('●');
            btnMostrarContra.setIcon(iconoOjo != null ? iconoOjo : null);
            if (iconoOjo == null) {
                btnMostrarContra.setText("👁");
            }
        }
    }

    // =========================================================================
    // CONFIGURACIÓN UI
    // =========================================================================
    private void configurarUIProfesional() {
        setTitle("Panel de Administración - Gestión de Usuarios");
        this.setLocationRelativeTo(null);
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        JButton[] botones = {btnGuardar, btnEliminar, btnBloqDesbloq, btnLimpiar,
            btnCerrarSesion, btnAuditoria, btnRespaldo};
        for (JButton btn : botones) {
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        tablaUsuarios.setRowHeight(30);
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaUsuarios.getTableHeader().setBackground(new Color(255, 102, 51));
        tablaUsuarios.getTableHeader().setForeground(Color.WHITE);
        tablaUsuarios.getTableHeader().setOpaque(false);
        tablaUsuarios.setSelectionBackground(new Color(255, 204, 153));
        tablaUsuarios.setSelectionForeground(Color.BLACK);
    }

    // =========================================================================
    // CARGAR ROLES
    // =========================================================================
    private void cargarRoles() {
        cboRol.removeAllItems();
        cboRol.addItem("Seleccione un Rol...");
        try {
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery("SELECT nombre_rol FROM rol ORDER BY rol_id ASC");
            while (rs.next()) {
                cboRol.addItem(rs.getString("nombre_rol"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + e.getMessage());
        }
    }

    // =========================================================================
    // CARGAR USUARIOS EN TABLA
    // =========================================================================
    private void cargarUsuarios() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modelo.addColumn("ID");
        modelo.addColumn("Usuario");
        modelo.addColumn("Nombre Completo");
        modelo.addColumn("Rol");
        modelo.addColumn("Estado");

        new Thread(() -> {
            try {
                String sql = "SELECT empleado_id, nombre_usuario, nombre_completo, nombre_rol, bloqueado FROM v_empleados";
                Statement st = cn.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    String estado = rs.getBoolean("bloqueado") ? "BLOQUEADO" : "Activo";
                    modelo.addRow(new Object[]{
                        rs.getInt("empleado_id"),
                        rs.getString("nombre_usuario"),
                        rs.getString("nombre_completo"),
                        rs.getString("nombre_rol"),
                        estado
                    });
                }
                SwingUtilities.invokeLater(() -> tablaUsuarios.setModel(modelo));
            } catch (SQLException e) {
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage()));
            }
        }).start();
    }

    // =========================================================================
    // SELECCIONAR USUARIO DE LA TABLA
    // =========================================================================
    private void seleccionarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        idUsuarioSeleccionado = (int) tablaUsuarios.getValueAt(fila, 0);
        String usuario = tablaUsuarios.getValueAt(fila, 1).toString();
        String rol = tablaUsuarios.getValueAt(fila, 3).toString();
        usuarioSeleccionadoEstaBloqueado
                = tablaUsuarios.getValueAt(fila, 4).toString().equals("BLOQUEADO");

        txtUsuario.setText(usuario);
        cboRol.setSelectedItem(rol);
        txtContra.setText("");
        contraVisible = false;
        txtContra.setEchoChar('●');
        btnMostrarContra.setIcon(iconoOjo != null ? iconoOjo : null);
        if (iconoOjo == null) {
            btnMostrarContra.setText("👁");
        }

        try {
            PreparedStatement ps = cn.prepareStatement(
                    "SELECT nombre, ap_paterno, ap_materno FROM empleado WHERE empleado_id = ?");
            ps.setInt(1, idUsuarioSeleccionado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNombre.setText(rs.getString("nombre"));
                txtPaterno.setText(rs.getString("ap_paterno"));
                txtMaterno.setText(rs.getString("ap_materno"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnGuardar.setText("Actualizar Usuario");
        btnBloqDesbloq.setEnabled(true);
        if (usuarioSeleccionadoEstaBloqueado) {
            btnBloqDesbloq.setText("Desbloquear Cuenta");
            btnBloqDesbloq.setBackground(new java.awt.Color(255, 153, 0));
        } else {
            btnBloqDesbloq.setText("Bloquear Cuenta");
            btnBloqDesbloq.setBackground(new java.awt.Color(153, 0, 0));
        }
    }

    // =========================================================================
    // LIMPIAR CAMPOS
    // =========================================================================
    private void limpiarCampos() {
        idUsuarioSeleccionado = -1;
        usuarioSeleccionadoEstaBloqueado = false;
        txtUsuario.setText("");
        txtContra.setText("");
        txtNombre.setText("");
        txtPaterno.setText("");
        txtMaterno.setText("");
        cboRol.setSelectedIndex(0);
        contraVisible = false;
        txtContra.setEchoChar('●');
        btnMostrarContra.setIcon(iconoOjo != null ? iconoOjo : null);
        if (iconoOjo == null) {
            btnMostrarContra.setText("👁");
        }
        btnGuardar.setText("Crear Usuario");
        btnBloqDesbloq.setText("Bloq / Desbloq");
        btnBloqDesbloq.setBackground(new java.awt.Color(102, 102, 102));
        btnBloqDesbloq.setEnabled(false);
        tablaUsuarios.clearSelection();
    }

    // =========================================================================
    // GUARDAR / ACTUALIZAR USUARIO
    // =========================================================================
    private void guardarUsuario() {
        String usuario = txtUsuario.getText().trim();
        String contra = new String(txtContra.getPassword()).trim();
        String nombre = txtNombre.getText().trim();
        String paterno = txtPaterno.getText().trim();
        String materno = txtMaterno.getText().trim();

        if (usuario.isEmpty() || nombre.isEmpty() || cboRol.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Usuario, Nombre y Rol son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idRol = 0;
            PreparedStatement psRol = cn.prepareStatement(
                    "SELECT rol_id FROM rol WHERE nombre_rol = ?");
            psRol.setString(1, cboRol.getSelectedItem().toString());
            ResultSet rsRol = psRol.executeQuery();
            if (rsRol.next()) {
                idRol = rsRol.getInt("rol_id");
            }

            if (idUsuarioSeleccionado == -1) {
                if (contra.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "La contraseña es obligatoria para nuevos usuarios.");
                    return;
                }
                String sql = "INSERT INTO empleado (nombre_usuario, clave_hash, nombre, ap_paterno, ap_materno, rol_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = cn.prepareStatement(sql);
                ps.setString(1, usuario);
                ps.setString(2, contra);
                ps.setString(3, nombre);
                ps.setString(4, paterno);
                ps.setString(5, materno);
                ps.setInt(6, idRol);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.");
                }
            } else {
                String sql;
                PreparedStatement ps;
                if (contra.isEmpty()) {
                    sql = "UPDATE empleado SET nombre_usuario=?, nombre=?, ap_paterno=?, ap_materno=?, rol_id=? WHERE empleado_id=?";
                    ps = cn.prepareStatement(sql);
                    ps.setString(1, usuario);
                    ps.setString(2, nombre);
                    ps.setString(3, paterno);
                    ps.setString(4, materno);
                    ps.setInt(5, idRol);
                    ps.setInt(6, idUsuarioSeleccionado);
                } else {
                    sql = "UPDATE empleado SET nombre_usuario=?, clave_hash=?, nombre=?, ap_paterno=?, ap_materno=?, rol_id=? WHERE empleado_id=?";
                    ps = cn.prepareStatement(sql);
                    ps.setString(1, usuario);
                    ps.setString(2, contra);
                    ps.setString(3, nombre);
                    ps.setString(4, paterno);
                    ps.setString(5, materno);
                    ps.setInt(6, idRol);
                    ps.setInt(7, idUsuarioSeleccionado);
                }
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.");
                }
            }
            limpiarCampos();
            cargarUsuarios();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // ELIMINAR USUARIO
    // =========================================================================
    private void eliminarUsuario() {
        if (idUsuarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un usuario de la tabla para eliminar.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar a este usuario de forma permanente?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = cn.prepareStatement(
                        "DELETE FROM empleado WHERE empleado_id = ?");
                ps.setInt(1, idUsuarioSeleccionado);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.");
                    limpiarCampos();
                    cargarUsuarios();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar este usuario porque tiene registros dependientes (ej. Ventas registradas).",
                        "Error de Integridad", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // BLOQUEAR / DESBLOQUEAR USUARIO
    // =========================================================================
    private void alternarBloqueoUsuario() {
        if (idUsuarioSeleccionado == -1) {
            return;
        }
        try {
            if (usuarioSeleccionadoEstaBloqueado) {
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE empleado SET bloqueado = 0, intentos_fallidos = 0 WHERE empleado_id = ?");
                ps.setInt(1, idUsuarioSeleccionado);
                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this,
                            "La cuenta ha sido desbloqueada exitosamente.",
                            "Desbloqueo", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                String motivo = JOptionPane.showInputDialog(this,
                        "Ingrese el motivo por el cual bloquea esta cuenta:",
                        "Confirmar Bloqueo", JOptionPane.WARNING_MESSAGE);
                if (motivo == null) {
                    return;
                }
                if (motivo.trim().isEmpty()) {
                    motivo = "Bloqueo manual realizado por el Administrador";
                }

                cn.setAutoCommit(false);
                PreparedStatement psBloq = cn.prepareStatement(
                        "UPDATE empleado SET bloqueado = 1 WHERE empleado_id = ?");
                psBloq.setInt(1, idUsuarioSeleccionado);
                psBloq.executeUpdate();

                PreparedStatement psLog = cn.prepareStatement(
                        "INSERT INTO bloqueo (empleado_id, fecha_bloqueo, intentos_acumulados, motivo) VALUES (?, CURRENT_TIMESTAMP, 0, ?)");
                psLog.setInt(1, idUsuarioSeleccionado);
                psLog.setString(2, motivo);
                psLog.executeUpdate();

                cn.commit();
                cn.setAutoCommit(true);
                JOptionPane.showMessageDialog(this,
                        "La cuenta ha sido bloqueada exitosamente.",
                        "Bloqueo", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarCampos();
            cargarUsuarios();
        } catch (SQLException e) {
            try {
                cn.rollback();
                cn.setAutoCommit(true);
            } catch (Exception ex) {
            }
            JOptionPane.showMessageDialog(this,
                    "Error en el proceso: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // HISTORIAL DE AUDITORÍA
    // =========================================================================
    private void mostrarHistorialAuditoria() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this,
                "Historial de Auditoría - Acciones Críticas", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new java.awt.BorderLayout());

        DefaultTableModel modeloAuditoria = new DefaultTableModel(
                new String[]{"ID Log", "Usuario Afectado", "Fecha/Hora",
                    "Motivo / Descripción de Acción"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        javax.swing.JTable tablaAuditoria = new javax.swing.JTable(modeloAuditoria);
        tablaAuditoria.setRowHeight(25);
        tablaAuditoria.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaAuditoria.getTableHeader().setBackground(new Color(51, 51, 51));
        tablaAuditoria.getTableHeader().setForeground(Color.WHITE);

        try {
            String sql = "SELECT b.bloqueo_id, e.nombre_usuario, b.fecha_bloqueo, b.motivo "
                    + "FROM bloqueo b LEFT JOIN empleado e ON b.empleado_id = e.empleado_id "
                    + "ORDER BY b.fecha_bloqueo DESC";
            Statement st = cn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                modeloAuditoria.addRow(new Object[]{
                    rs.getInt("bloqueo_id"),
                    rs.getString("nombre_usuario") != null
                    ? rs.getString("nombre_usuario") : "Usuario no encontrado",
                    rs.getString("fecha_bloqueo"),
                    rs.getString("motivo")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar el historial de auditoría: " + e.getMessage());
        }

        dialog.add(new javax.swing.JScrollPane(tablaAuditoria), java.awt.BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // =========================================================================
    // RESPALDO MANUAL DE BASE DE DATOS
    // =========================================================================
    private void realizarRespaldoBD() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Seleccionar carpeta para guardar el respaldo");
        chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }

        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new java.util.Date());
        String carpeta = chooser.getSelectedFile().getAbsolutePath();
        String archivo = carpeta + java.io.File.separator
                + "respaldo_pame4_" + timestamp + ".sql";

        // Crear carpeta si no existe
        java.io.File carpetaDestino = new java.io.File(carpeta);
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs();
        }

        String mysqldump = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";

        if (!new java.io.File(mysqldump).exists()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró mysqldump.exe en:\n" + mysqldump
                    + "\n\nVerifica que MySQL Server 8.0 esté instalado en esa ruta.",
                    "Ruta no encontrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnRespaldo.setEnabled(false);
        btnRespaldo.setText("Generando...");

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        mysqldump,
                        "-h", "localhost",
                        "-P", "3306",
                        "-u", "root",
                        "-pmxmarcossj20",
                        "--databases", "pame4",
                        "--result-file=" + archivo,
                        "--default-character-set=utf8"
                );
                pb.redirectErrorStream(true);
                Process proceso = pb.start();

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proceso.getInputStream()));
                StringBuilder salida = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    salida.append(linea).append("\n");
                }

                proceso.waitFor();

                SwingUtilities.invokeLater(() -> {
                    btnRespaldo.setEnabled(true);
                    btnRespaldo.setText("Respaldar BD");

                    if (new java.io.File(archivo).exists()
                            && new java.io.File(archivo).length() > 0) {
                        JOptionPane.showMessageDialog(this,
                                "✅ Respaldo generado exitosamente.\n\n"
                                + "Archivo: " + archivo + "\n"
                                + "Tamaño: " + (new java.io.File(archivo).length() / 1024) + " KB",
                                "Respaldo Exitoso", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "❌ Error al generar el respaldo.\n\nDetalle:\n"
                                + (salida.length() > 0 ? salida.toString() : "Archivo vacío o no creado."),
                                "Error de Respaldo", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnRespaldo.setEnabled(true);
                    btnRespaldo.setText("Respaldar BD");
                    JOptionPane.showMessageDialog(this,
                            "Error al ejecutar el respaldo:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // =========================================================================
    // RESPALDO AUTOMÁTICO CADA 12 HORAS
    // =========================================================================
    private void iniciarRespaldoAutomatico() {
        // Carpeta fija donde se guardan los respaldos automáticos
        String carpetaAuto = "C:\\RespaldosVanabby";

        java.io.File carpeta = new java.io.File(carpetaAuto);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        schedulerRespaldo = Executors.newSingleThreadScheduledExecutor();

        schedulerRespaldo.scheduleAtFixedRate(() -> {
            try {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new java.util.Date());
                String archivo = carpetaAuto + java.io.File.separator
                        + "auto_pame4_" + timestamp + ".sql";

                String mysqldump
                        = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";

                ProcessBuilder pb = new ProcessBuilder(
                        mysqldump,
                        "-h", "localhost",
                        "-P", "3306",
                        "-u", "root",
                        "-pmxmarcossj20",
                        "--databases", "pame4",
                        "--result-file=" + archivo,
                        "--default-character-set=utf8"
                );
                pb.redirectErrorStream(true);
                Process proceso = pb.start();

                // Consumir salida para que el proceso no se bloquee
                new java.io.BufferedReader(
                        new java.io.InputStreamReader(proceso.getInputStream()))
                        .lines().forEach(l -> {
                        });

                proceso.waitFor();

                // Conservar solo los 5 respaldos automáticos más recientes
                java.io.File[] archivos = carpeta.listFiles(
                        (dir, name) -> name.startsWith("auto_pame4_") && name.endsWith(".sql"));
                if (archivos != null && archivos.length > 5) {
                    java.util.Arrays.sort(archivos,
                            java.util.Comparator.comparingLong(java.io.File::lastModified));
                    for (int i = 0; i < archivos.length - 5; i++) {
                        archivos[i].delete();
                    }
                }

                // Actualizar label en la UI
                SwingUtilities.invokeLater(()
                        -> lblUltimoRespaldo.setText("Último respaldo auto: " + timestamp));

            } catch (Exception e) {
                System.err.println("Error en respaldo automático: " + e.getMessage());
            }

        }, 0, 12, TimeUnit.HOURS);
        // 0 = primer respaldo al abrir la ventana, luego cada 12 horas
    }

    // =========================================================================
    // initComponents
    // =========================================================================
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanelFondo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblInstrucciones = new javax.swing.JLabel();
        lblUltimoRespaldo = new javax.swing.JLabel();
        jPanelFormulario = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        panelContra = new javax.swing.JPanel();
        txtContra = new javax.swing.JPasswordField();
        btnMostrarContra = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtPaterno = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtMaterno = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cboRol = new javax.swing.JComboBox<>();
        btnLimpiar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnBloqDesbloq = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        btnAuditoria = new javax.swing.JButton();
        btnRespaldo = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Fondo
        jPanelFondo.setBackground(new java.awt.Color(245, 230, 211));
        jPanelFondo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        // Título
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblTitulo.setForeground(new java.awt.Color(255, 102, 51));
        lblTitulo.setText("Gestión de Usuarios y Roles (Admin)");
        jPanelFondo.add(lblTitulo,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 600, 40));

        // Instrucciones
        lblInstrucciones.setFont(new java.awt.Font("Segoe UI", 0, 16));
        lblInstrucciones.setForeground(new java.awt.Color(80, 80, 80));
        lblInstrucciones.setText(
                "Crea, edita, elimina y gestiona el bloqueo de acceso al sistema para los empleados.");
        jPanelFondo.add(lblInstrucciones,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 700, 20));

        // Label último respaldo automático
        lblUltimoRespaldo.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lblUltimoRespaldo.setForeground(new java.awt.Color(0, 128, 0));
        lblUltimoRespaldo.setText("⏱ Último respaldo auto: pendiente...");
        jPanelFondo.add(lblUltimoRespaldo,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 85, 400, 18));

        // Botón Respaldar BD (manual)
        btnRespaldo.setBackground(new java.awt.Color(0, 128, 0));
        btnRespaldo.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnRespaldo.setForeground(java.awt.Color.WHITE);
        btnRespaldo.setText("Respaldar BD");
        btnRespaldo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRespaldo.addActionListener(evt -> realizarRespaldoBD());
        jPanelFondo.add(btnRespaldo,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 30, 150, 40));

        // Botón Auditoría
        btnAuditoria.setBackground(new java.awt.Color(0, 102, 204));
        btnAuditoria.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnAuditoria.setForeground(java.awt.Color.WHITE);
        btnAuditoria.setText("Historial de Auditoría");
        btnAuditoria.addActionListener(evt -> mostrarHistorialAuditoria());
        jPanelFondo.add(btnAuditoria,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 30, 160, 40));

        // Botón Cerrar Sesión
        btnCerrarSesion.setBackground(new java.awt.Color(255, 51, 51));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnCerrarSesion.setForeground(java.awt.Color.WHITE);
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.addActionListener(evt -> {
            new Login().setVisible(true);
            this.dispose();
        });
        jPanelFondo.add(btnCerrarSesion,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(1200, 30, 130, 40));

        // Panel Formulario
        jPanelFormulario.setBackground(new java.awt.Color(255, 244, 228));
        jPanelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
                "Datos del Empleado",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 14)));

        jLabel1.setText("Usuario de acceso:");
        jLabel2.setText("Contraseña:");
        jLabel3.setText("Nombre:");
        jLabel4.setText("Apellido Paterno:");
        jLabel5.setText("Apellido Materno:");
        jLabel6.setText("Asignar Rol:");

        // Panel contraseña + botón ojo
        panelContra.setOpaque(false);
        panelContra.setLayout(new java.awt.BorderLayout(0, 0));
        txtContra.setEchoChar('●');
        panelContra.add(txtContra, java.awt.BorderLayout.CENTER);

        btnMostrarContra.setPreferredSize(new java.awt.Dimension(32, 30));
        btnMostrarContra.setFocusPainted(false);
        btnMostrarContra.setBorderPainted(false);
        btnMostrarContra.setContentAreaFilled(false);
        btnMostrarContra.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMostrarContra.setToolTipText("Mostrar / Ocultar contraseña");
        if (iconoOjo != null) {
            btnMostrarContra.setIcon(iconoOjo);
        } else {
            btnMostrarContra.setText("👁");
        }
        btnMostrarContra.addActionListener(evt -> toggleContra());
        panelContra.add(btnMostrarContra, java.awt.BorderLayout.EAST);

        // Botones del formulario
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(evt -> limpiarCampos());

        btnGuardar.setBackground(new java.awt.Color(0, 153, 51));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.setText("Crear Usuario");
        btnGuardar.addActionListener(evt -> guardarUsuario());

        btnEliminar.setBackground(new java.awt.Color(204, 0, 0));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(evt -> eliminarUsuario());

        btnBloqDesbloq.setBackground(new java.awt.Color(102, 102, 102));
        btnBloqDesbloq.setForeground(Color.WHITE);
        btnBloqDesbloq.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBloqDesbloq.setText("Bloq / Desbloq");
        btnBloqDesbloq.setEnabled(false);
        btnBloqDesbloq.addActionListener(evt -> alternarBloqueoUsuario());

        // Layout del formulario
        javax.swing.GroupLayout gl_jPanelFormulario
                = new javax.swing.GroupLayout(jPanelFormulario);
        jPanelFormulario.setLayout(gl_jPanelFormulario);
        gl_jPanelFormulario.setHorizontalGroup(
                gl_jPanelFormulario.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(gl_jPanelFormulario.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(gl_jPanelFormulario.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1)
                                        .addComponent(txtUsuario,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 250,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2)
                                        .addComponent(panelContra,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 250,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel6)
                                        .addComponent(cboRol,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 250,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3)
                                        .addComponent(txtNombre,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 250,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4)
                                        .addComponent(txtPaterno,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 250,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5)
                                        .addComponent(txtMaterno,
                                                javax.swing.GroupLayout.PREFERRED_SIZE, 250,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(gl_jPanelFormulario.createSequentialGroup()
                                                .addComponent(btnGuardar,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 130,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(
                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnLimpiar,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(gl_jPanelFormulario.createSequentialGroup()
                                                .addComponent(btnBloqDesbloq,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(
                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnEliminar,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 94,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(20, Short.MAX_VALUE))
        );
        gl_jPanelFormulario.setVerticalGroup(
                gl_jPanelFormulario.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel1)
                        .addComponent(txtUsuario,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel2)
                        .addComponent(panelContra,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6)
                        .addComponent(cboRol,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel3)
                        .addComponent(txtNombre,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel4)
                        .addComponent(txtPaterno,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel5)
                        .addComponent(txtMaterno,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addGroup(gl_jPanelFormulario.createParallelGroup(
                                javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnGuardar,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnLimpiar,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(gl_jPanelFormulario.createParallelGroup(
                                javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnBloqDesbloq,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEliminar,
                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(20, Short.MAX_VALUE)
        );

        jPanelFondo.add(jPanelFormulario,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 310, 620));

        // Tabla
        tablaUsuarios.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{}, new String[]{}));
        jScrollPane1.setViewportView(tablaUsuarios);
        jPanelFondo.add(jScrollPane1,
                new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 110, 970, 600));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelFondo,
                                javax.swing.GroupLayout.DEFAULT_SIZE, 1370, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelFondo,
                                javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
        );

        pack();
    }

    // =========================================================================
    // MAIN
    // =========================================================================
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName()) || "Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Administrador().setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // =========================================================================
    // DECLARACIÓN DE VARIABLES
    // =========================================================================
    private javax.swing.JButton btnAuditoria;
    private javax.swing.JButton btnBloqDesbloq;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnMostrarContra;
    private javax.swing.JButton btnRespaldo;
    private javax.swing.JPanel panelContra;
    private javax.swing.JComboBox<String> cboRol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanelFondo;
    private javax.swing.JPanel jPanelFormulario;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblInstrucciones;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUltimoRespaldo;
    private javax.swing.JTable tablaUsuarios;
    private javax.swing.JPasswordField txtContra;
    private javax.swing.JTextField txtMaterno;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPaterno;
    private javax.swing.JTextField txtUsuario;
}
