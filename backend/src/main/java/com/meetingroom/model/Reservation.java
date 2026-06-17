package com.meetingroom.model;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int userId;
    private int roomId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;       // 'pending', 'approved', 'rejected', 'cancelled', 'completed'
    private LocalDateTime createdAt;

    // Joined fields (not stored in reservation table)
    private String username;
    private String roomName;

    public Reservation() {}

    public Reservation(int id, int userId, int roomId, String title,
                       LocalDateTime startTime, LocalDateTime endTime,
                       String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}
