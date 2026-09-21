package com.hospital.dao;

import com.hospital.db.DBConnection;
import com.hospital.model.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the doctors table.
 */
public class DoctorDAO {

    public int addDoctor(Doctor d) throws SQLException {
        String sql = "INSERT INTO doctors (name, specialization, phone, email, experience, available) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.setString(3, d.getPhone());
            ps.setString(4, d.getEmail());
            ps.setInt(5, d.getExperience());
            ps.setString(6, d.availableAsDbString());

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

    public boolean updateDoctor(Doctor d) throws SQLException {
        String sql = "UPDATE doctors SET name = ?, specialization = ?, phone = ?, email = ?, "
                + "experience = ?, available = ? WHERE doctor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.setString(3, d.getPhone());
            ps.setString(4, d.getEmail());
            ps.setInt(5, d.getExperience());
            ps.setString(6, d.availableAsDbString());
            ps.setInt(7, d.getDoctorId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteDoctor(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;
        }
    }

    public Doctor getDoctorById(int doctorId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        String sql = "SELECT * FROM doctors ORDER BY name";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Returns only doctors currently marked available - used to populate the appointment combo box. */
    public List<Doctor> getAvailableDoctors() throws SQLException {
        String sql = "SELECT * FROM doctors WHERE available = 'YES' ORDER BY name";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** SELECT ... WHERE specialization LIKE ? */
    public List<Doctor> searchBySpecialization(String specializationPart) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE specialization LIKE ? ORDER BY name";
        List<Doctor> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + specializationPart + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int countDoctors() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getString("name"),
                rs.getString("specialization"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getInt("experience"),
                "YES".equalsIgnoreCase(rs.getString("available"))
        );
    }
}
