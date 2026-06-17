package com.meetingroom.dao;

import com.meetingroom.model.ReservationLog;
import com.meetingroom.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationLogDAO {

    public boolean create(int reservationId, int adminId, String action, String note) {
        String sql = "INSERT INTO reservation_log (reservation_id, admin_id, action, note, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            stmt.setInt(2, adminId);
            stmt.setString(3, action);
            stmt.setString(4, note);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ReservationLog> findByReservationId(int reservationId) {
        List<ReservationLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM reservation_log WHERE reservation_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReservationLog log = new ReservationLog();
                    log.setId(rs.getInt("id"));
                    log.setReservationId(rs.getInt("reservation_id"));
                    log.setAdminId(rs.getInt("admin_id"));
                    log.setAction(rs.getString("action"));
                    log.setNote(rs.getString("note"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
