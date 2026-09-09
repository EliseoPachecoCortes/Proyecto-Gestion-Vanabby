package loginvanabby;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Módulo de Marketing - Repostería Vanabby (Reformulado para evitar bloqueos de NetBeans)
 */
public class Marketing extends javax.swing.JFrame {

    private final String CONFIG_FILE = "config_bienvenida.properties";
    private String rutaImagenActual = "";

    // Variables de la interfaz (Declaradas manualmente para evitar el bloqueo de NetBeans)
    private JButton btnCerrarSesion, btnGuardar, btnSeleccionarLogo;
    private JLabel jLabel1, jLabel2, jLabel3, jLabel4, jLabelPreviewTitle;
    private JPanel jPanelFondo, jPanelFormulario, jPanelPreview;
    private JScrollPane jScrollPanePromo;
    private JLabel lblEstadoAprobacion, lblInstrucciones, lblPreviewLogo;
    private JLabel lblPreviewMensaje, lblPreviewPromo, lblTitulo;
    private JSpinner spnTiempo;
    private JTextField txtMensaje;
    private JTextArea txtPromocion;

    public Marketing() {
        // Llamamos a nuestro propio método en lugar del que bloquea NetBeans
        crearInterfazGrafica();
        configurarUIProfesional();
        cargarConfiguracionActual();
    }

    private void configurarUIProfesional() {
        setTitle("Panel de Marketing - Gestión de Promociones");
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JButton[] botones = {btnGuardar, btnSeleccionarLogo, btnCerrarSesion};
        for (JButton btn : botones) {
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        txtPromocion.setLineWrap(true);
        txtPromocion.setWrapStyleWord(true);
        spnTiempo.setModel(new SpinnerNumberModel(3, 3, 5, 1));
    }

    private void cargarConfiguracionActual() {
        Properties prop = new Properties();
        File file = new File(CONFIG_FILE);
        
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                prop.load(in);
                txtMensaje.setText(prop.getProperty("mensaje_principal", "¡Bienvenidos a Vanabby!"));
                txtPromocion.setText(prop.getProperty("promocion_temporada", ""));
                
                int tiempo = Integer.parseInt(prop.getProperty("tiempo_pantalla", "3"));
                spnTiempo.setValue(tiempo);
                
                rutaImagenActual = prop.getProperty("ruta_logo", "");
                actualizarPreviewLogo(rutaImagenActual);
                
                String estado = prop.getProperty("estado_aprobacion", "PENDIENTE");
                lblEstadoAprobacion.setText("Estado actual: " + estado);
                if (estado.equals("APROBADO")) {
                    lblEstadoAprobacion.setForeground(new Color(0, 153, 51));
                } else {
                    lblEstadoAprobacion.setForeground(new Color(255, 102, 51));
                }
                
            } catch (IOException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la configuración: " + ex.getMessage());
            }
        } else {
            actualizarPreviewLogo(""); 
        }
        actualizarVistaPreviaTexto();
    }

    private void guardarConfiguracion() {
        String mensaje = txtMensaje.getText().trim();
        String promo = txtPromocion.getText().trim();
        int tiempo = (int) spnTiempo.getValue();
        
        if (mensaje.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El mensaje principal no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Properties prop = new Properties();
        prop.setProperty("mensaje_principal", mensaje);
        prop.setProperty("promocion_temporada", promo);
        prop.setProperty("tiempo_pantalla", String.valueOf(tiempo));
        prop.setProperty("ruta_logo", rutaImagenActual);
        prop.setProperty("estado_aprobacion", "PENDIENTE"); 

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            prop.store(out, "Configuracion de Pantalla de Bienvenida Vanabby");
            JOptionPane.showMessageDialog(this, "Configuración guardada correctamente.\nEnviada al Gerente para aprobación.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarConfiguracionActual();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar la configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarLogo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Nuevo Logo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "png", "jpeg"));
        
        int seleccion = fileChooser.showOpenDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            rutaImagenActual = archivo.getAbsolutePath();
            actualizarPreviewLogo(rutaImagenActual);
        }
    }

    private void actualizarPreviewLogo(String ruta) {
        try {
            ImageIcon icon;
            if (ruta == null || ruta.isEmpty() || !new File(ruta).exists()) {
                java.net.URL imgUrl = getClass().getResource("/imagenes/logoVanabby (250 x 250 px) (1).png");
                if (imgUrl != null) {
                    icon = new ImageIcon(imgUrl);
                } else {
                    lblPreviewLogo.setText("Sin Logo");
                    lblPreviewLogo.setIcon(null);
                    return;
                }
            } else {
                icon = new ImageIcon(ruta);
            }
            
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            lblPreviewLogo.setIcon(new ImageIcon(img));
            lblPreviewLogo.setText("");
        } catch (Exception e) {
            lblPreviewLogo.setText("Error cargando logo");
            lblPreviewLogo.setIcon(null);
        }
    }

    private void actualizarVistaPreviaTexto() {
        lblPreviewMensaje.setText("<html><center>" + txtMensaje.getText().replaceAll("\n", "<br>") + "</center></html>");
        lblPreviewPromo.setText("<html><center>" + txtPromocion.getText().replaceAll("\n", "<br>") + "</center></html>");
    }

    // =========================================================================
    // MÉTODO REFORMULADO (No usa el nombre de NetBeans para evitar conflictos)
    // =========================================================================
    private void crearInterfazGrafica() {

        jPanelFondo = new JPanel();
        lblTitulo = new JLabel();
        lblInstrucciones = new JLabel();
        btnCerrarSesion = new JButton();
        
        jPanelFormulario = new JPanel();
        jLabel1 = new JLabel();
        txtMensaje = new JTextField();
        jLabel2 = new JLabel();
        txtPromocion = new JTextArea();
        jScrollPanePromo = new JScrollPane(txtPromocion);
        jLabel3 = new JLabel();
        spnTiempo = new JSpinner();
        jLabel4 = new JLabel();
        btnSeleccionarLogo = new JButton();
        btnGuardar = new JButton();
        lblEstadoAprobacion = new JLabel();

        jPanelPreview = new JPanel();
        lblPreviewLogo = new JLabel();
        lblPreviewMensaje = new JLabel();
        lblPreviewPromo = new JLabel();
        jLabelPreviewTitle = new JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelFondo.setBackground(new Color(245, 230, 211));
        jPanelFondo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(255, 102, 51));
        lblTitulo.setText("Marketing - Configuración de Bienvenida");
        jPanelFondo.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 600, 40));

        lblInstrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblInstrucciones.setForeground(new Color(80, 80, 80));
        lblInstrucciones.setText("Edita la pantalla inicial. Los cambios requieren aprobación del Gerente.");
        jPanelFondo.add(lblInstrucciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 800, 20));

        btnCerrarSesion.setBackground(new Color(255, 51, 51));
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.addActionListener(evt -> {
            new Login().setVisible(true);
            this.dispose();
        });
        jPanelFondo.add(btnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(1200, 30, 130, 40));

        // PANEL DE FORMULARIO
        jPanelFormulario.setBackground(new Color(255, 244, 228));
        jPanelFormulario.setBorder(BorderFactory.createTitledBorder(null, "Editor de Contenido", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 14)));

        jLabel1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel1.setText("Mensaje Principal:");
        
        txtMensaje.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                actualizarVistaPreviaTexto();
            }
        });

        jLabel2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel2.setText("Promoción de Temporada:");

        txtPromocion.setColumns(20);
        txtPromocion.setRows(5);
        txtPromocion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                actualizarVistaPreviaTexto();
            }
        });

        jLabel3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel3.setText("Tiempo en pantalla (3 a 5 seg):");

        jLabel4.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jLabel4.setText("Logotipo de Bienvenida:");

        btnSeleccionarLogo.setText("Cargar Nueva Imagen");
        btnSeleccionarLogo.addActionListener(evt -> seleccionarLogo());

        btnGuardar.setBackground(new Color(0, 153, 51));
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setText("Guardar y Enviar a Aprobación");
        btnGuardar.addActionListener(evt -> guardarConfiguracion());

        lblEstadoAprobacion.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 14));
        lblEstadoAprobacion.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstadoAprobacion.setText("Estado actual: PENDIENTE");

        javax.swing.GroupLayout gl_jPanelFormulario = new javax.swing.GroupLayout(jPanelFormulario);
        jPanelFormulario.setLayout(gl_jPanelFormulario);
        gl_jPanelFormulario.setHorizontalGroup(
            gl_jPanelFormulario.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_jPanelFormulario.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(gl_jPanelFormulario.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEstadoAprobacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPanePromo)
                    .addComponent(txtMensaje)
                    .addGroup(gl_jPanelFormulario.createSequentialGroup()
                        .addGroup(gl_jPanelFormulario.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addGroup(gl_jPanelFormulario.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(spnTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(gl_jPanelFormulario.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(btnSeleccionarLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 48, Short.MAX_VALUE)))
                .addGap(20, 20, 20))
        );
        gl_jPanelFormulario.setVerticalGroup(
            gl_jPanelFormulario.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPanePromo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addGroup(gl_jPanelFormulario.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spnTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(gl_jPanelFormulario.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(btnSeleccionarLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addComponent(lblEstadoAprobacion)
                .addGap(18, 18, 18)
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
        );

        jPanelFondo.add(jPanelFormulario, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 420, 560));

        // PANEL DE VISTA PREVIA
        jPanelPreview.setBackground(Color.WHITE);
        jPanelPreview.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204), 2));
        jPanelPreview.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelPreviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabelPreviewTitle.setForeground(new Color(153, 153, 153));
        jLabelPreviewTitle.setText("VISTA PREVIA DE LA PANTALLA");
        jPanelPreview.add(jLabelPreviewTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 300, -1));

        lblPreviewLogo.setHorizontalAlignment(SwingConstants.CENTER);
        jPanelPreview.add(lblPreviewLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 80, 300, 200));

        lblPreviewMensaje.setFont(new Font("Times New Roman", Font.BOLD, 36));
        lblPreviewMensaje.setForeground(new Color(0, 153, 0));
        lblPreviewMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        jPanelPreview.add(lblPreviewMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, 700, 60));

        lblPreviewPromo.setFont(new Font("Arial", Font.PLAIN, 24));
        lblPreviewPromo.setForeground(new Color(255, 102, 51));
        lblPreviewPromo.setHorizontalAlignment(SwingConstants.CENTER);
        jPanelPreview.add(lblPreviewPromo, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 370, 700, 100));

        jPanelFondo.add(jPanelPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 110, 800, 560));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelFondo, javax.swing.GroupLayout.DEFAULT_SIZE, 1370, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelFondo, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
        );

        pack();
    }
}
