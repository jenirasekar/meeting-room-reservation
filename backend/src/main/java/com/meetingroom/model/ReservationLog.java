package com.meetingroom.model;

import java.time.LocalDateTime;

public class ReservationLog {
    private int id;
    private int reservationId;
    private int adminId;
    private String action;
    private String note;
    private LocalDateTime createdAt;

    public ReservationLog() {}

    public ReservationLog(int id, int reservationId, int adminId,
                          String action, String note, LocalDateTime createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.adminId = adminId;
        this.action = action;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
