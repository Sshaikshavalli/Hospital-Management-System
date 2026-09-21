package com.hospital.dao;

import com.hospital.db.DBConnection;
import com.hospital.model.Patient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the patients table. All SQL for patients lives here -
 * PatientFrame never touches JDBC directly.
 */
public class PatientDAO {

    public int addPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender, phone, address, disease, blood_group, admission_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getAddress());
            ps.setString(6, p.getDisease());
            ps.setString(7, p.getBloodGroup());
            ps.setDate(8, Date.valueOf(p.getAdmissionDate()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return 0;
        }
    }

    public boolean updatePatient(Patient p) throws SQLException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, address = ?, "
                + "disease = ?, blood_group = ?, admission_date = ? WHERE patient_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getAddress());
            ps.setString(6, p.getDisease());
            ps.setString(7, p.getBloodGroup());
            ps.setDate(8, Date.valueOf(p.getAdmissionDate()));
            ps.setInt(9, p.getPatientId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        }
    }

    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** SELECT ... ORDER BY - full patient list, most recently admitted first. */
    public List<Patient> getAllPatients() throws SQLException {
        String sql = "SELECT * FROM patients ORDER BY patient_id DESC";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** SELECT ... WHERE name LIKE ? - partial name search. */
    public List<Patient> searchByName(String namePart) throws SQLException {
        String sql = "SELECT * FROM patients WHERE name LIKE ? ORDER BY name";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + namePart + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** SELECT COUNT(*) - used on the dashboard. */
    public int countPatients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("disease"),
                rs.getString("blood_group"),
                rs.getDate("admission_date").toLocalDate()
        );
    }
}
