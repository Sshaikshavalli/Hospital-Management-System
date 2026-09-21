package com.hospital.dao;

import com.hospital.db.DBConnection;
import com.hospital.model.Bill;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the bills table.
 */
public class BillDAO {

    private static final String JOIN_SELECT =
            "SELECT b.bill_id, b.patient_id, b.room_charge, b.doctor_charge, b.medicine_charge, "
                    + "b.test_charge, b.total_amount, b.bill_date, p.name AS patient_name "
                    + "FROM bills b JOIN patients p ON b.patient_id = p.patient_id ";

    public int generateBill(Bill b) throws SQLException {
        String sql = "INSERT INTO bills (patient_id, room_charge, doctor_charge, medicine_charge, "
                + "test_charge, total_amount, bill_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, b.getPatientId());
            ps.setDouble(2, b.getRoomCharge());
            ps.setDouble(3, b.getDoctorCharge());
            ps.setDouble(4, b.getMedicineCharge());
            ps.setDouble(5, b.getTestCharge());
            ps.setDouble(6, b.calculateTotal());
            ps.setDate(7, Date.valueOf(b.getBillDate()));

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

    public boolean updateBill(Bill b) throws SQLException {
        String sql = "UPDATE bills SET patient_id = ?, room_charge = ?, doctor_charge = ?, "
                + "medicine_charge = ?, test_charge = ?, total_amount = ?, bill_date = ? WHERE bill_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, b.getPatientId());
            ps.setDouble(2, b.getRoomCharge());
            ps.setDouble(3, b.getDoctorCharge());
            ps.setDouble(4, b.getMedicineCharge());
            ps.setDouble(5, b.getTestCharge());
            ps.setDouble(6, b.calculateTotal());
            ps.setDate(7, Date.valueOf(b.getBillDate()));
            ps.setInt(8, b.getBillId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteBill(int billId) throws SQLException {
        String sql = "DELETE FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Bill> getAllBills() throws SQLException {
        String sql = JOIN_SELECT + "ORDER BY b.bill_date DESC, b.bill_id DESC";
        List<Bill> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Search bills by patient name - JOIN + LIKE. */
    public List<Bill> searchByPatientName(String namePart) throws SQLException {
        String sql = JOIN_SELECT + "WHERE p.name LIKE ? ORDER BY b.bill_date DESC";
        List<Bill> list = new ArrayList<>();

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

    /** SUM(total_amount) across all bills - shown on the Billing screen. */
    public double getTotalBillingAmount() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bills";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /** GROUP BY example: total billed amount per patient. */
    public List<Object[]> getTotalBilledPerPatient() throws SQLException {
        String sql = "SELECT p.name, SUM(b.total_amount) AS total "
                + "FROM bills b JOIN patients p ON b.patient_id = p.patient_id "
                + "GROUP BY p.patient_id, p.name ORDER BY total DESC";
        List<Object[]> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new Object[]{rs.getString("name"), rs.getDouble("total")});
            }
        }
        return result;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill(
                rs.getInt("bill_id"),
                rs.getInt("patient_id"),
                rs.getDouble("room_charge"),
                rs.getDouble("doctor_charge"),
                rs.getDouble("medicine_charge"),
                rs.getDouble("test_charge"),
                rs.getDouble("total_amount"),
                rs.getDate("bill_date").toLocalDate()
        );
        b.setPatientName(rs.getString("patient_name"));
        return b;
    }
}
