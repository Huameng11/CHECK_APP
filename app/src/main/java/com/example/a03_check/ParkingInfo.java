package com.example.a03_check;

public class ParkingInfo {
    private String parkingSpot;
    private String name;
    private String floor;
    private String licensePlate;
    private String phone;

    public ParkingInfo(String floor, String name, String parkingSpot, String licensePlate, String phone) {
        this.parkingSpot = parkingSpot;
        this.name = name;
        this.floor = floor;
        this.licensePlate = licensePlate;
        this.phone = phone;
    }

    public String getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(String parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}