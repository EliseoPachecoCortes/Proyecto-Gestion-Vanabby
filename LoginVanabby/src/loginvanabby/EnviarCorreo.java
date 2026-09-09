package loginvanabby;


import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Clase utilitaria para enviar tickets por correo Gmail - Vanabby
 *
 * IMPORTANTE: Cambia CORREO_VANABBY y CONTRASENA_APP por los datos reales
 */
public class EnviarCorreo {

    // ===== CONFIGURACIÓN - CAMBIA ESTOS DATOS =====
    private static final String CORREO_VANABBY = "marcojimenezjuarez407@gmail.com";   // <- pon el correo de Vanabby
    private static final String CONTRASENA_APP = "bmaw zior cnef vdtv";  // <- contraseña de aplicación de Google (16 caracteres)

    /**
     * Envía un ticket de VENTA al correo del cliente
     */
    public static void enviarTicketVenta(
            String correoCliente,
            String nombreCliente,
            String folio,
            String fecha,
            String fechaEntrega,
            String productos, // Lista de productos
            String total,
            String metodoPago,
            String estadoPago,
            String vendedor) {

        String asunto = "Tu pedido en Vanabby - Folio " + folio;
        String cuerpo = construirHTMLVenta(nombreCliente, folio, fecha, fechaEntrega,
                productos, total, metodoPago, estadoPago, vendedor);
        enviar(correoCliente, asunto, cuerpo);
    }

    /**
     * Envía un ticket de ANTICIPO al correo del cliente
     */
    public static void enviarTicketAnticipo(
            String correoCliente, String nombreCliente, String folio, String fecha, String productos, String montoAnticipo, String totalPedido, String restante, String concepto) {

        String asunto = "Anticipo registrado en Vanabby - Folio " + folio;
        String vendedor = null;
        String cuerpo = construirHTMLAnticipo(nombreCliente, folio, fecha, productos, montoAnticipo,
                totalPedido, restante, concepto, vendedor);
        enviar(correoCliente, asunto, cuerpo);
    }

    // ===== MÉTODO PRINCIPAL DE ENVÍO =====
    private static void enviar(String destinatario, String asunto, String cuerpoHTML) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "*");  //cualquier destinatario

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO_VANABBY, CONTRASENA_APP);
            }
        });

        try {
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(CORREO_VANABBY, "Repostería Vanabby"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setContent(cuerpoHTML, "text/html; charset=utf-8");
            Transport.send(mensaje);
            System.out.println("Correo enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo: " + e.getMessage());
        }
    }

    // ===== HTML DEL TICKET DE VENTA =====
    private static String construirHTMLVenta(
            String nombre, String folio, String fecha, String fechaEntrega,
            String productos, String total, String metodoPago, String estadoPago, String vendedor) {

        return "<!DOCTYPE html><html><body style='font-family:Segoe UI,Arial,sans-serif;"
                + "background:#fff8f0;margin:0;padding:0'>"
                + "<div style='max-width:580px;margin:30px auto;background:#ffffff;"
                + "border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1)'>"
                // Encabezado
                + "<div style='background:#d28c64;padding:30px;text-align:center'>"
                + "<h1 style='color:white;margin:0;font-size:26px;letter-spacing:2px'>🧁 Vanabby</h1>"
                + "<p style='color:#fff3e8;margin:5px 0 0;font-size:14px'>Repostería Artesanal</p>"
                + "</div>"
                // Cuerpo
                + "<div style='padding:30px'>"
                + "<h2 style='color:#b05a28;margin-top:0'>¡Gracias por tu pedido, " + nombre + "!</h2>"
                + "<p style='color:#555'>Hemos registrado tu compra. Aquí están los detalles:</p>"
                // Datos del folio
                + "<table style='width:100%;border-collapse:collapse;margin:15px 0'>"
                + fila(" Folio", folio)
                + fila(" Fecha de compra", fecha)
                + fila(" Fecha de entrega", fechaEntrega)
                + fila(" Atendido por", vendedor)
                + "</table>"
                // Productos
                + "<div style='background:#fff3e8;border-radius:8px;padding:15px;margin:15px 0'>"
                + "<h3 style='color:#b05a28;margin-top:0;font-size:16px'>🛒 Productos Comprados:</h3>"
                + "<p style='color:#555;margin:0;white-space:pre-line;line-height:1.5'>" + productos + "</p>"
                + "</div>"
                // Total y pago
                + "<table style='width:100%;border-collapse:collapse;margin:15px 0'>"
                + fila(" Método de pago", metodoPago)
                + fila(" Estado del pago", estadoPago)
                + "</table>"
                + "<div style='background:#d28c64;border-radius:8px;padding:15px;text-align:center;margin:15px 0'>"
                + "<span style='color:white;font-size:22px;font-weight:bold'>Total: $" + total + "</span>"
                + "</div>"
                + "<p style='color:#888;font-size:12px;text-align:center;margin-top:25px'>"
                + "Si tienes alguna duda, contáctanos.<br>"
                + "<strong style='color:#d28c64'>Repostería Vanabby</strong></p>"
                + "</div>"
                // Pie
                + "<div style='background:#f5e6d8;padding:15px;text-align:center'>"
                + "<p style='color:#b05a28;margin:0;font-size:12px'>Este es un correo automático, por favor no respondas a este mensaje.</p>"
                + "</div>"
                + "</div></body></html>";
    }

  // ── MÉTODO PARA ENVIAR CORREO DE RECUPERACIÓN CON CONTRASEÑA TEMPORAL ──
    public static void enviarCorreoRecuperacion(String correoDestino, String nombreUsuario, String passTemporal) throws Exception {
        
        // Pon tus credenciales reales
        final String miCorreo = "marcojimenezjuarez407@gmail.com"; 
        final String miContrasena = "bmaw zior cnef vdtv"; 

        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(miCorreo, miContrasena);
            }
        });

        javax.mail.Message mensaje = new javax.mail.internet.MimeMessage(session);
        mensaje.setFrom(new javax.mail.internet.InternetAddress(miCorreo, "Repostería Vanabby - Soporte"));
        mensaje.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(correoDestino));
        mensaje.setSubject("Restablecimiento de Contraseña - Vanabby");

        // Diseño del correo mostrando la contraseña temporal
        String contenidoHTML = "<h2>Hola, " + nombreUsuario + "</h2>"
                + "<p>Hemos recibido una solicitud para recuperar tu contraseña en el sistema de <b>Repostería Vanabby</b>.</p>"
                + "<p>Se ha generado una nueva contraseña temporal para tu cuenta. Utilízala para iniciar sesión:</p>"
                + "<h3 style='color: #0056b3; background-color: #f0f8ff; padding: 12px; display: inline-block; border-radius: 5px; border: 1px solid #b3d7ff; font-family: monospace;'>" 
                + passTemporal + "</h3>"
                + "<p>Te recomendamos iniciar sesión y cambiarla por una nueva lo antes posible.</p>"
                + "<br><p><i>Si no solicitaste este cambio, contacta a tu Administrador inmediatamente.</i></p>";

        mensaje.setContent(contenidoHTML, "text/html; charset=utf-8");
        javax.mail.Transport.send(mensaje);
    }
    // ===== HTML DEL TICKET DE ANTICIPO =====
    private static String construirHTMLAnticipo(
            String nombre, String folio, String fecha, String productos,
            String montoAnticipo, String totalPedido, String restante,
            String concepto, String vendedor) {

        return "<!DOCTYPE html><html><body style='font-family:Segoe UI,Arial,sans-serif;"
                + "background:#fff8f0;margin:0;padding:0'>"
                + "<div style='max-width:580px;margin:30px auto;background:#ffffff;"
                + "border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1)'>"
                // Encabezado
                + "<div style='background:#d28c64;padding:30px;text-align:center'>"
                + "<h1 style='color:white;margin:0;font-size:26px;letter-spacing:2px'>🧁 Vanabby</h1>"
                + "<p style='color:#fff3e8;margin:5px 0 0;font-size:14px'>Repostería Artesanal</p>"
                + "</div>"
                // Cuerpo
                + "<div style='padding:30px'>"
                + "<h2 style='color:#b05a28;margin-top:0'>Anticipo registrado, " + nombre + "</h2>"
                + "<p style='color:#555'>Tu anticipo ha sido registrado exitosamente:</p>"
                + "<table style='width:100%;border-collapse:collapse;margin:15px 0'>"
                + fila(" Folio", folio)
                + fila(" Fecha", fecha)
                + fila(" Concepto", concepto)
                + fila(" Registrado por", vendedor)
                + "</table>"
                // PRODUCTOS COMPRADOS
                + "<div style='background:#fff3e8;border-radius:8px;padding:15px;margin:15px 0'>"
                + "<h3 style='color:#b05a28;margin-top:0;font-size:16px'>🛒 Pedido:</h3>"
                + "<p style='color:#555;margin:0;white-space:pre-line;line-height:1.5'>" + productos + "</p>"
                + "</div>"
                // Montos
                + "<div style='background:#fff3e8;border-radius:8px;padding:20px;margin:15px 0'>"
                + "<table style='width:100%'>"
                + "<tr><td style='color:#555;padding:6px 0'>Total del pedido:</td>"
                + "<td style='text-align:right;font-weight:bold;color:#333'>$" + totalPedido + "</td></tr>"
                + "<tr><td style='color:#555;padding:6px 0'>Anticipo pagado:</td>"
                + "<td style='text-align:right;font-weight:bold;color:#2a9d5c'>$" + montoAnticipo + "</td></tr>"
                + "<tr><td colspan='2'><hr style='border:1px solid #e0c4a8;margin:8px 0'></td></tr>"
                + "<tr><td style='color:#b05a28;font-weight:bold;padding:6px 0'>Restante a pagar:</td>"
                + "<td style='text-align:right;font-weight:bold;font-size:18px;color:#b05a28'>$" + restante + "</td></tr>"
                + "</table></div>"
                + "<p style='color:#888;font-size:12px;text-align:center;margin-top:25px'>"
                + "Guarda este correo como comprobante de tu anticipo.<br>"
                + "<strong style='color:#d28c64'>Repostería Vanabby</strong></p>"
                + "</div>"
                + "<div style='background:#f5e6d8;padding:15px;text-align:center'>"
                + "<p style='color:#b05a28;margin:0;font-size:12px'>Este es un correo automático, por favor no respondas a este mensaje.</p>"
                + "</div>"
                + "</div></body></html>";
    }

    // ===== UTILIDAD: fila de tabla HTML =====
    private static String fila(String etiqueta, String valor) {
        return "<tr>"
                + "<td style='padding:8px;color:#888;font-size:13px;width:45%'>" + etiqueta + "</td>"
                + "<td style='padding:8px;color:#333;font-weight:bold;font-size:13px'>" + valor + "</td>"
                + "</tr>";
    }
}