package com.skillonnet.automation.dao;

import com.skillonnet.automation.db.DBConnection;
import com.skillonnet.automation.db.DatabaseException;
import com.skillonnet.automation.model.AdverseReaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdverseReactionDAO {

    private final DBConnection db;

    public AdverseReactionDAO() {
        this(DBConnection.getInstance());
    }

    public AdverseReactionDAO(DBConnection db) {
        this.db = db;
    }

    public boolean existsByPatientAndMedication(int patientId, int medicationId) {
        String sql = """
                SELECT 1 FROM adverse_reaction WHERE patient_id = ? AND medication_id = ?""";
        try (Connection conn = db.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, medicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("exists adverse reaction failed", e);
        }
    }

    public int insert(AdverseReaction r) {
        String sql = "INSERT INTO adverse_reaction (patient_id, medication_id, description) VALUES (?, ?, ?)";
        try (Connection conn = db.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getMedicationId());
            ps.setString(3, r.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("No generated key for adverse_reaction insert");
                }
                int id = keys.getInt(1);
                r.setReactionId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new DatabaseException("insert adverse_reaction failed", e);
        }
    }
}
