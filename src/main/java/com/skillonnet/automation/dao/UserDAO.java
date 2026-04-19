package com.skillonnet.automation.dao;

import com.skillonnet.automation.db.DBConnection;
import com.skillonnet.automation.db.DatabaseException;
import com.skillonnet.automation.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class UserDAO {

    private static final Set<String> ROLES = Set.of("Clinical", "Receptionist", "Medical_Records");

    private final DBConnection db;

    public UserDAO() {
        this(DBConnection.getInstance());
    }

    public UserDAO(DBConnection db) {
        this.db = db;
    }

    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = db.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                if (!password.equals(rs.getString("password_hash"))) {
                    return Optional.empty();
                }
                String role = rs.getString("role");
                if (!ROLES.contains(role)) {
                    return Optional.empty();
                }
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRole(role);
                return Optional.of(u);
            }
        } catch (SQLException e) {
            throw new DatabaseException("authenticate failed", e);
        }
    }
}
