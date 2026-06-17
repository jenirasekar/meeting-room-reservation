package com.meetingroom.model;

public class Equipment {
    private int id;
    private int roomId;
    private String name;
    private int quantity;

    public Equipment() {}

    public Equipment(int id, int roomId, String name, int quantity) {
        this.id = id;
        this.roomId = roomId;
        this.name = name;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
