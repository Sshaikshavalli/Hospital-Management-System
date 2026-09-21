package com.hospital.model;


public class Room {

    public static final String AVAILABLE = "AVAILABLE";
    public static final String OCCUPIED = "OCCUPIED";
    public static final String MAINTENANCE = "MAINTENANCE";

    private int roomId;
    private String roomNumber;
    private String roomType;
    private double pricePerDay;
    private String status;

    public Room() {
    }

    public Room(String roomNumber, String roomType, double pricePerDay, String status) {
        this(0, roomNumber, roomType, pricePerDay, status);
    }

    public Room(int roomId, String roomNumber, String roomType, double pricePerDay, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
