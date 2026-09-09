/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.*;
/**
 *
 * @author Usuario1
 */
public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/pame4"; // URL de la base de datos
    private static final String USER = "root"; // Usuario de la base de datos
    private static final String PASSWORD = "eli"; // Contraseña de la base de datos
    
    // Método para obtener la conexión
    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado. Asegúrate de tener el JAR mysql-connector-java en el classpath: " + e.getMessage());
        }
    }
}
