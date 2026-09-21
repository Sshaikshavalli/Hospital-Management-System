package com.hospital.model;


public class Doctor {

    private int doctorId;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    private int experience;
    private boolean available;

    public Doctor() {
    }

    public Doctor(String name, String specialization, String phone, String email,
                  int experience, boolean available) {
        this(0, name, specialization, phone, email, experience, available);
    }

    public Doctor(int doctorId, String name, String specialization, String phone, String email,
                  int experience, boolean available) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.experience = experience;
        this.available = available;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

  
    public String availableAsDbString() {
        return available ? "YES" : "NO";
    }

    @Override
    public String toString() {
  
        return doctorId + " - " + name + " (" + specialization + ")";
    }
}
