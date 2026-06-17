package com.meetingroom.dao;

import com.meetingroom.model.Equipment;
import com.meetingroom.util.DBConnection;

import java.sql.*;

public class EquipmentDAO {

    public Equipment findById(int id) {
        String sql = "SELECT * FROM equipment WHERE id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Equipment e = new Equipment();
                    e.setId(rs.getInt("id"));
                    e.setRoomId(rs.getInt("room_id"));
                    e.setName(rs.getString("name"));
                    e.setQuantity(rs.getInt("quantity"));
                    return e;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Equipment equip) {
        String sql = "INSERT INTO equipment (room_id, name, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, equip.getRoomId());
            stmt.setString(2, equip.getName());
            stmt.setInt(3, equip.getQuantity());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) equip.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Equipment equip) {
        String sql = "UPDATE equipment SET name=?, quantity=? WHERE id=?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, equip.getName());
            stmt.setInt(2, equip.getQuantity());
            stmt.setInt(3, equip.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM equipment WHERE id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByRoomId(int roomId) {
        String sql = "DELETE FROM equipment WHERE room_id = ?";
        try (Connection conn = DBConnection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
