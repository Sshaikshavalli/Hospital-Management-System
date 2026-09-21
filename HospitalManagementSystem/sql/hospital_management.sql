

CREATE DATABASE IF NOT EXISTS hospital_management;
USE hospital_management;

-- Drop tables in FK-safe order (children first) if re-running the script
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------
-- Table: users
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NOT NULL DEFAULT 'ADMIN'
);

-- ------------------------------------------------------------
-- Table: patients
-- ------------------------------------------------------------
CREATE TABLE patients (
    patient_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    age             INT NOT NULL,
    gender          VARCHAR(10) NOT NULL,
    phone           VARCHAR(15),
    address         VARCHAR(255),
    disease         VARCHAR(150),
    blood_group     VARCHAR(5),
    admission_date  DATE NOT NULL DEFAULT (CURRENT_DATE)
);

-- ------------------------------------------------------------
-- Table: doctors
-- ------------------------------------------------------------
CREATE TABLE doctors (
    doctor_id       INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    specialization  VARCHAR(100) NOT NULL,
    phone           VARCHAR(15),
    email           VARCHAR(100) UNIQUE,
    experience      INT NOT NULL DEFAULT 0,
    available       VARCHAR(3) NOT NULL DEFAULT 'YES'   -- 'YES' or 'NO'
);

-- ------------------------------------------------------------
-- Table: appointments  (many-to-one -> patients, many-to-one -> doctors)
-- ------------------------------------------------------------
CREATE TABLE appointments (
    appointment_id     INT AUTO_INCREMENT PRIMARY KEY,
    patient_id         INT NOT NULL,
    doctor_id          INT NOT NULL,
    appointment_date   DATE NOT NULL,
    appointment_time   TIME NOT NULL,
    reason             VARCHAR(255),
    status             VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED / COMPLETED / CANCELLED
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_appointment_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- Table: rooms
-- ------------------------------------------------------------
CREATE TABLE rooms (
    room_id         INT AUTO_INCREMENT PRIMARY KEY,
    room_number     VARCHAR(10) NOT NULL UNIQUE,
    room_type       VARCHAR(30) NOT NULL,
    price_per_day   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' -- AVAILABLE / OCCUPIED / MAINTENANCE
);

-- ------------------------------------------------------------
-- Table: bills (many-to-one -> patients)
-- ------------------------------------------------------------
CREATE TABLE bills (
    bill_id          INT AUTO_INCREMENT PRIMARY KEY,
    patient_id       INT NOT NULL,
    room_charge      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    doctor_charge    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    medicine_charge  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    test_charge      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    bill_date        DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_bill_patient
        FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Default admin login (plain text password - see README security note)
INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');

-- 5 patients
INSERT INTO patients (name, age, gender, phone, address, disease, blood_group, admission_date) VALUES
('Rohan Sharma',  34, 'Male',   '9876543210', 'Bengaluru', 'Fever',         'B+',  '2026-09-01'),
('Priya Nair',    28, 'Female', '9876500001', 'Chennai',   'Diabetes',      'O+',  '2026-09-02'),
('Amit Verma',    45, 'Male',   '9876500002', 'Delhi',     'Hypertension',  'A+',  '2026-09-03'),
('Sneha Reddy',   30, 'Female', '9876500003', 'Hyderabad', 'Asthma',        'AB+', '2026-09-04'),
('Karan Mehta',   52, 'Male',   '9876500004', 'Mumbai',    'Fracture',      'O-',  '2026-09-05');

-- 5 doctors
INSERT INTO doctors (name, specialization, phone, email, experience, available) VALUES
('Dr. Anil Kumar',   'Cardiology',        '9123456780', 'anil.kumar@hospital.com',   12, 'YES'),
('Dr. Meera Iyer',   'Neurology',         '9123456781', 'meera.iyer@hospital.com',    8, 'YES'),
('Dr. Suresh Rao',   'Orthopedics',       '9123456782', 'suresh.rao@hospital.com',   15, 'YES'),
('Dr. Divya Menon',  'Pediatrics',        '9123456783', 'divya.menon@hospital.com',   6, 'NO'),
('Dr. Vikram Singh', 'General Medicine',  '9123456784', 'vikram.singh@hospital.com', 10, 'YES');

-- 5 rooms
INSERT INTO rooms (room_number, room_type, price_per_day, status) VALUES
('101',   'General',       1000.00, 'AVAILABLE'),
('102',   'General',       1000.00, 'OCCUPIED'),
('201',   'Semi-Private',  2000.00, 'AVAILABLE'),
('301',   'Private',       3500.00, 'AVAILABLE'),
('ICU-1', 'ICU',           8000.00, 'MAINTENANCE');

-- 3 appointments
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status) VALUES
(1, 1, CURDATE(),      '10:00:00', 'Chest pain checkup',       'SCHEDULED'),
(2, 5, CURDATE(),      '11:30:00', 'Routine diabetes checkup', 'SCHEDULED'),
(3, 3, '2026-09-18',   '09:00:00', 'Knee pain',                'COMPLETED');

-- 2 bills
INSERT INTO bills (patient_id, room_charge, doctor_charge, medicine_charge, test_charge, total_amount, bill_date) VALUES
(1, 2000.00, 500.00, 300.00, 700.00, 3500.00, CURDATE()),
(3,    0.00, 800.00, 200.00, 500.00, 1500.00, '2026-09-18');
