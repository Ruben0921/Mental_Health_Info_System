package com.skillonnet.automation.dao;

import com.skillonnet.automation.db.DBConnection;
import com.skillonnet.automation.db.DatabaseException;
import com.skillonnet.automation.model.Medication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inserts medication catalog rows.
 */
public class MedicationDAO {

    private final DBConnection db;

    public MedicationDAO() {
        this(DBConnection.getInstance());
    }

    public MedicationDAO(DBConnection db) {
        this.db = db;
    }

    /** @return generated {@code medication_id} */
    public int insert(Medication m) {
        String sql = "INSERT INTO medication (name) VALUES (?)";
        try (Connection conn = db.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("No generated key for medication insert");
                }
                int id = keys.getInt(1);
                m.setMedicationId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new DatabaseException("insert medication failed", e);
        }
    }
}
