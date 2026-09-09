package dao;


import conexion.ConexionBD;
import java.sql.*;

public class UsuarioDAO {

    public ResultSet validarUsuario(String usuario, String pass) throws SQLException {
        Connection cn = ConexionBD.getConexion();
        if (cn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        // Nota: En un sistema real, usa PreparedStatements con hashing (BCrypt) 
        String sql = "SELECT empleado_id, rol_id, bloqueado FROM empleado WHERE nombre_usuario = ? AND clave_hash = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, usuario);
        ps.setString(2, pass);
        return ps.executeQuery();
    }
    
    public void registrarIntentoFallido(String usuario, boolean bloquear) throws SQLException {
        // Lógica para actualizar intentos fallidos 
    }
}