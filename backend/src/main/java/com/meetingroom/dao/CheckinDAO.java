package com.meetingroom.dao;

import com.meetingroom.model.Checkin;
import com.meetingroom.util.DBConnection;

import java.sql.*;

public class CheckinDAO {

    public Checkin findByReservationId(int reservationId) {
        String sql = "SELECT * FROM checkin WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCheckin(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Checkin checkin) {
        String sql = "INSERT INTO checkin (reservation_id, checkin_time, method) VALUES (?, NOW(), ?)";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, checkin.getReservationId());
            stmt.setString(2, checkin.getMethod() != null ? checkin.getMethod() : "manual");
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) checkin.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countToday() {
        String sql = "SELECT COUNT(*) FROM checkin WHERE DATE(checkin_time) = CURDATE()";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Checkin mapCheckin(ResultSet rs) throws SQLException {
        Checkin c = new Checkin();
        c.setId(rs.getInt("id"));
        c.setReservationId(rs.getInt("reservation_id"));
        Timestamp ts = rs.getTimestamp("checkin_time");
        if (ts != null) c.setCheckinTime(ts.toLocalDateTime());
        c.setMethod(rs.getString("method"));
        return c;
    }
}
