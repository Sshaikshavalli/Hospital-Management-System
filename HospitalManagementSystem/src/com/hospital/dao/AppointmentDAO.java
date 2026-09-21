package com.hospital.dao;

import com.hospital.db.DBConnection;
import com.hospital.model.Appointment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


public class AppointmentDAO {

    private static final String JOIN_SELECT =
            "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, "
                    + "a.reason, a.status, p.name AS patient_name, d.name AS doctor_name, d.specialization "
                    + "FROM appointments a "
                    + "JOIN patients p ON a.patient_id = p.patient_id "
                    + "JOIN doctors d ON a.doctor_id = d.doctor_id ";

    public int bookAppointment(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setDate(3, Date.valueOf(a.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(a.getAppointmentTime()));
            ps.setString(5, a.getReason());
            ps.setString(6, a.getStatus());

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

    public boolean updateAppointment(Appointment a) throws SQLException {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, "
                + "appointment_time = ?, reason = ?, status = ? WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setDate(3, Date.valueOf(a.getAppointmentDate()));
            ps.setTime(4, Time.valueOf(a.getAppointmentTime()));
            ps.setString(5, a.getReason());
            ps.setString(6, a.getStatus());
            ps.setInt(7, a.getAppointmentId());

            return ps.executeUpdate() > 0;
        }
    }

    /** Cancels an appointment by setting its status, rather than deleting the row (keeps history). */
    public boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAppointment(int appointmentId) throws SQLException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    /** All appointments, most recent first, with patient/doctor names via JOIN. */
    public List<Appointment> getAllAppointments() throws SQLException {
        String sql = JOIN_SELECT + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        return runJoinQuery(sql, null);
    }

    /** Appointments scheduled for today - used on both the dashboard and this screen. */
    public List<Appointment> getTodaysAppointments() throws SQLException {
        String sql = JOIN_SELECT + "WHERE a.appointment_date = CURDATE() ORDER BY a.appointment_time";
        return runJoinQuery(sql, null);
    }

    public int countTodaysAppointments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Full appointment history for one patient - JOIN + WHERE + ORDER BY. */
    public List<Appointment> getPatientAppointmentHistory(int patientId) throws SQLException {
        String sql = JOIN_SELECT + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC";
        return runJoinQuery(sql, patientId);
    }

    /** Search across patient name / doctor name / status - used by the Search button. */
    public List<Appointment> searchAppointments(String keyword) throws SQLException {
        String sql = JOIN_SELECT
                + "WHERE p.name LIKE ? OR d.name LIKE ? OR a.status LIKE ? "
                + "ORDER BY a.appointment_date DESC";
        List<Appointment> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJoinRow(rs));
                }
            }
        }
        return list;
    }

    private List<Appointment> runJoinQuery(String sql, Integer patientIdParam) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (patientIdParam != null) {
                ps.setInt(1, patientIdParam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJoinRow(rs));
                }
            }
        }
        return list;
    }

    private Appointment mapJoinRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment(
                rs.getInt("appointment_id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getDate("appointment_date").toLocalDate(),
                rs.getTime("appointment_time").toLocalTime(),
                rs.getString("reason"),
                rs.getString("status")
        );
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setSpecialization(rs.getString("specialization"));
        return a;
    }
}
