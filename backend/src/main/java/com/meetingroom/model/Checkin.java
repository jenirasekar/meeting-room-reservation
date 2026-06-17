package com.meetingroom.model;

import java.time.LocalDateTime;

public class Checkin {
    private int id;
    private int reservationId;
    private LocalDateTime checkinTime;
    private String method;       // 'manual' or 'qr'

    public Checkin() {}

    public Checkin(int id, int reservationId, LocalDateTime checkinTime, String method) {
        this.id = id;
        this.reservationId = reservationId;
        this.checkinTime = checkinTime;
        this.method = method;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
