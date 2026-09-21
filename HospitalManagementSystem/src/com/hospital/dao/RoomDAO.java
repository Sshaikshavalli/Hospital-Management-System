package com.hospital.dao;

import com.hospital.db.DBConnection;
import com.hospital.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the rooms table.
 */
public class RoomDAO {

    public int addRoom(Room r) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_day, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomType());
            ps.setDouble(3, r.getPricePerDay());
            ps.setString(4, r.getStatus());

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

    public boolean updateRoom(Room r) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, price_per_day = ?, status = ? "
                + "WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomType());
            ps.setDouble(3, r.getPricePerDay());
            ps.setString(4, r.getStatus());
            ps.setInt(5, r.getRoomId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Room> getAllRooms() throws SQLException {
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        List<Room> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** WHERE status = 'AVAILABLE' - used both by Room screen "View Available" and dashboard stat. */
    public List<Room> getAvailableRooms() throws SQLException {
        String sql = "SELECT * FROM rooms WHERE status = 'AVAILABLE' ORDER BY room_number";
        List<Room> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Room> searchByRoomNumber(String numberPart) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_number LIKE ? ORDER BY room_number";
        List<Room> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + numberPart + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public boolean roomNumberExists(String roomNumber, int excludeRoomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ? AND room_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ps.setInt(2, excludeRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Marks a room OCCUPIED (e.g. when a patient is assigned to it). */
    public boolean allocateRoom(int roomId) throws SQLException {
        return setStatus(roomId, Room.OCCUPIED);
    }

    /** Marks a room AVAILABLE again (e.g. when a patient is discharged). */
    public boolean releaseRoom(int roomId) throws SQLException {
        return setStatus(roomId, Room.AVAILABLE);
    }

    private boolean setStatus(int roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAvailableRooms() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getString("room_type"),
                rs.getDouble("price_per_day"),
                rs.getString("status")
        );
    }
}
