package com.hospital.model;

import java.time.LocalDate;


public class Patient {

    private int patientId;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String disease;
    private String bloodGroup;
    private LocalDate admissionDate;

    public Patient() {
    }

   
    public Patient(String name, int age, String gender, String phone, String address,
                   String disease, String bloodGroup) {
        this(0, name, age, gender, phone, address, disease, bloodGroup, LocalDate.now());
    }

   
    public Patient(int patientId, String name, int age, String gender, String phone, String address,
                   String disease, String bloodGroup, LocalDate admissionDate) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.disease = disease;
        this.bloodGroup = bloodGroup;
        this.admissionDate = admissionDate;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    @Override
    public String toString() {
        return "Patient{" + patientId + ", " + name + ", age=" + age + "}";
    }
}
