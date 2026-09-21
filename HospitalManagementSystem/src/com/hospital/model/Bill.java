package com.hospital.model;

import java.time.LocalDate;


public class Bill {

    private int billId;
    private int patientId;
    private double roomCharge;
    private double doctorCharge;
    private double medicineCharge;
    private double testCharge;
    private double totalAmount;
    private LocalDate billDate;

    private String patientName; 

    public Bill() {
    }

    public Bill(int patientId, double roomCharge, double doctorCharge,
                double medicineCharge, double testCharge) {
        this.patientId = patientId;
        this.roomCharge = roomCharge;
        this.doctorCharge = doctorCharge;
        this.medicineCharge = medicineCharge;
        this.testCharge = testCharge;
        this.totalAmount = calculateTotal();
        this.billDate = LocalDate.now();
    }

    public Bill(int billId, int patientId, double roomCharge, double doctorCharge,
                double medicineCharge, double testCharge, double totalAmount, LocalDate billDate) {
        this.billId = billId;
        this.patientId = patientId;
        this.roomCharge = roomCharge;
        this.doctorCharge = doctorCharge;
        this.medicineCharge = medicineCharge;
        this.testCharge = testCharge;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
    }

  
    public double calculateTotal() {
        return roomCharge + doctorCharge + medicineCharge + testCharge;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public double getRoomCharge() {
        return roomCharge;
    }

    public void setRoomCharge(double roomCharge) {
        this.roomCharge = roomCharge;
    }

    public double getDoctorCharge() {
        return doctorCharge;
    }

    public void setDoctorCharge(double doctorCharge) {
        this.doctorCharge = doctorCharge;
    }

    public double getMedicineCharge() {
        return medicineCharge;
    }

    public void setMedicineCharge(double medicineCharge) {
        this.medicineCharge = medicineCharge;
    }

    public double getTestCharge() {
        return testCharge;
    }

    public void setTestCharge(double testCharge) {
        this.testCharge = testCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
