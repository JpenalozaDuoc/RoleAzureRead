package apirestazureread.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.relation.RoleNotFoundException;

import apirestazureread.connection.DatabaseConnection;
import apirestazureread.model.Role;

public class RoleService {

    // Modificación en RoleService para soportar paginación
    public List<Role> getAllRoles(int limit, int offset) throws SQLException {
        List<Role> roles = new ArrayList<>();
    
        // Corregir la consulta para Oracle, utilizando ROW_NUMBER()
        String sql = "SELECT * FROM ( " +
                     "    SELECT r.*, ROW_NUMBER() OVER (ORDER BY r.id) AS rn " +
                     "    FROM roles r " +
                     ") WHERE rn BETWEEN ? AND ?";

   
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
    
            // Calcular el rango de filas a obtener
            int startRow = offset + 1;  // Oracle usa 1 como índice de filas
            int endRow = offset + limit;
    
            stmt.setInt(1, startRow);  // Establecer el valor para la primera fila
            stmt.setInt(2, endRow);    // Establecer el valor para la última fila
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String name = rs.getString("role_name");
                    Role role = new Role(id, name);
                    roles.add(role);
                }
            }
        }
    
        return roles;
    }

    // Obtener un rol por su ID
    public Role getRoleById(Long id) throws SQLException, RoleNotFoundException {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Long roleId = rs.getLong("id");
                String name = rs.getString("role_name");
                return new Role(roleId, name);
            } else {
                throw new RoleNotFoundException("Role not found with ID " + id);
            }
        }
    }

}
