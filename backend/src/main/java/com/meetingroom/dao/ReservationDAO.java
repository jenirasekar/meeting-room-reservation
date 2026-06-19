package com.meetingroom.dao;

import com.meetingroom.model.Reservation;
import com.meetingroom.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public List<Reservation> findAll(Integer userId, String statusFilter) {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, u.username, rm.name AS room_name " +
            "FROM reservation r " +
            "JOIN user u ON r.user_id = u.id " +
            "JOIN room rm ON r.room_id = rm.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND r.user_id = ?");
            params.add(userId);
        }
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql.append(" AND r.status = ?");
            params.add(statusFilter);
        }
        sql.append(" ORDER BY r.created_at DESC");

        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapReservationWithJoin(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Reservation findById(int id) {
        String sql = "SELECT r.*, u.username, rm.name AS room_name " +
                     "FROM reservation r " +
                     "JOIN user u ON r.user_id = u.id " +
                     "JOIN room rm ON r.room_id = rm.id WHERE r.id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapReservationWithJoin(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check for conflicting reservations for a room in a time range.
     * Returns the list of overlapping approved/pending reservations.
     */
    public List<Reservation> findConflicts(int roomId, LocalDateTime start, LocalDateTime end, Integer excludeId) {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM reservation WHERE room_id = ? " +
            "AND status IN ('approved', 'pending') " +
            "AND start_time < ? AND end_time > ?"
        );
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }

        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setInt(1, roomId);
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            stmt.setTimestamp(3, Timestamp.valueOf(start));
            if (excludeId != null) {
                stmt.setInt(4, excludeId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.setId(rs.getInt("id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setRoomId(rs.getInt("room_id"));
                    r.setTitle(rs.getString("title"));
                    r.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    r.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get booked time slots for a room on a specific date.
     * Returns approved/pending reservations within that day.
     */
    public List<Reservation> findBookedSlots(int roomId, LocalDate date) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE room_id = ? " +
                     "AND status IN ('approved', 'pending') " +
                     "AND DATE(start_time) = ? " +
                     "ORDER BY start_time";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.setId(rs.getInt("id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setRoomId(rs.getInt("room_id"));
                    r.setTitle(rs.getString("title"));
                    r.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    r.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean create(Reservation res) {
        String sql = "INSERT INTO reservation (user_id, room_id, title, start_time, end_time, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, 'pending', NOW())";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, res.getUserId());
            stmt.setInt(2, res.getRoomId());
            stmt.setString(3, res.getTitle());
            stmt.setTimestamp(4, Timestamp.valueOf(res.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(res.getEndTime()));
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) res.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE reservation SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE status = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countToday() {
        String sql = "SELECT COUNT(*) FROM reservation WHERE DATE(created_at) = CURDATE()";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Reservation mapReservationWithJoin(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setRoomId(rs.getInt("room_id"));
        r.setTitle(rs.getString("title"));
        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) r.setStartTime(startTs.toLocalDateTime());
        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) r.setEndTime(endTs.toLocalDateTime());
        r.setStatus(rs.getString("status"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) r.setCreatedAt(createdTs.toLocalDateTime());
        // Joined fields
        try { r.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        try { r.setRoomName(rs.getString("room_name")); } catch (SQLException ignored) {}
        return r;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Added for auto-cancellation
    public List<Integer> findNoShows(java.time.LocalDateTime cutoff) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT r.id FROM reservation r " +
                     "LEFT JOIN checkin c ON c.reservation_id = r.id " +
                     "WHERE r.status = 'approved' " +
                     "AND r.start_time < ? " +
                     "AND c.id IS NULL";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(cutoff));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
}
