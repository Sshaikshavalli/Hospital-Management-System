# CityCare Hospital Management System

A desktop **Hospital Management System** built with core Java technologies —
no frameworks, no magic — designed as a portfolio project for a Java fresher
to demonstrate OOP, JDBC, SQL, and layered application design.

---

## 1. Project Overview

CityCare HMS is a Swing desktop application backed by a MySQL database. It
lets hospital staff log in and manage patients, doctors, appointments, rooms,
and billing, with a dashboard that shows live statistics pulled straight from
the database.

The project is intentionally built **without Spring or Hibernate** so that
every JDBC call, SQL query, and Swing component is visible and easy to
explain in an interview.

---

## 2. Features

- Secure-ish login screen backed by the `users` table (see Security note below)
- Dashboard with live counts: total patients, total doctors, today's
  appointments, available rooms
- **Patient Management** — full CRUD, JTable listing, search by name,
  input validation (name, age, phone)
- **Doctor Management** — full CRUD, JTable listing, search by specialization
- **Appointment Management** — book/update/cancel appointments using
  JComboBox pickers for patient/doctor, appointment list shown via SQL JOIN
  (patient name, doctor name, specialization — never raw IDs)
- **Room Management** — full CRUD, allocate/release rooms, duplicate room
  number prevention, filter by AVAILABLE status
- **Billing** — generate/update/delete bills, total amount auto-calculated
  from room/doctor/medicine/test charges, SUM() of all billing shown live
- Consistent professional color scheme and layout across every screen
- Centralized JDBC connection handling (`DBConnection`) — no UI class ever
  opens its own connection or writes raw SQL

---

## 3. Technologies

| Layer          | Technology                      |
|----------------|----------------------------------|
| Language       | Java 17+                        |
| GUI            | Java Swing                      |
| Database       | MySQL 8                         |
| DB Connectivity| JDBC (MySQL Connector/J)        |
| IDE            | Eclipse                         |
| Architecture   | MVC + DAO (no Spring, no Hibernate) |

---

## 4. Project Structure

```
HospitalManagementSystem/
├── sql/
│   └── hospital_management.sql        <- run this first
├── lib/
│   └── (place mysql-connector-j-x.x.x.jar here)
├── src/
│   └── com/hospital/
│       ├── main/
│       │   └── Main.java
│       ├── db/
│       │   └── DBConnection.java
│       ├── model/
│       │   ├── User.java
│       │   ├── Patient.java
│       │   ├── Doctor.java
│       │   ├── Appointment.java
│       │   ├── Room.java
│       │   └── Bill.java
│       ├── dao/
│       │   ├── UserDAO.java
│       │   ├── PatientDAO.java
│       │   ├── DoctorDAO.java
│       │   ├── AppointmentDAO.java
│       │   ├── RoomDAO.java
│       │   └── BillDAO.java
│       └── ui/
│           ├── UITheme.java           <- shared colors/fonts, not part of the DAO flow
│           ├── LoginFrame.java
│           ├── DashboardFrame.java
│           ├── PatientFrame.java
│           ├── DoctorFrame.java
│           ├── AppointmentFrame.java
│           ├── RoomFrame.java
│           └── BillFrame.java
└── README.md
```

**Architecture flow** (strictly followed in every module):

```
UI (JFrame)  ->  DAO  ->  DBConnection  ->  MySQL
```

No SQL ever appears inside a `ui` class — every query lives in a DAO.

---

## 5. Database Setup

1. Make sure MySQL 8 is installed and running.
2. Open a terminal or MySQL Workbench and run:

   ```bash
   mysql -u root -p < sql/hospital_management.sql
   ```

   or paste the contents of `sql/hospital_management.sql` into Workbench and
   execute it. This will:
   - Create the `hospital_management` database
   - Create all 6 tables with primary keys, foreign keys, and constraints
   - Insert the default admin user, 5 patients, 5 doctors, 5 rooms,
     3 appointments, and 2 bills

---

## 6. MySQL Configuration

Open `src/com/hospital/db/DBConnection.java` and update these two constants
to match your local MySQL setup:

```java
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root"; // <-- change this
```

The connection URL defaults to `jdbc:mysql://localhost:3306/hospital_management`.
If your MySQL server runs on a different host/port, update `DB_URL` in the
same file.

---

## 7. JDBC Driver Setup (MySQL Connector/J)

1. Download **MySQL Connector/J** (the `mysql-connector-j-x.x.x.jar` file)
   from the official MySQL site.
2. Place the JAR in the `lib/` folder of this project.
3. In Eclipse, add it to the build path (see next section).

---

## 8. Eclipse Setup

1. Open Eclipse → `File` → `Import` → `General` → `Existing Projects into Workspace`.
2. Select the `HospitalManagementSystem` folder (or create a new Java Project
   and copy the `src` folder in).
3. Right-click the project → `Properties` → `Java Build Path` → `Libraries` tab.
4. Click `Add JARs...` (if the JAR is inside the project's `lib` folder) or
   `Add External JARs...` (if it's elsewhere) and select
   `mysql-connector-j-x.x.x.jar`.
5. Click `Apply and Close`.
6. Ensure the project's source folder is `src` and the package structure
   matches `com.hospital.*` exactly (it will, if you copied the `src` folder
   as-is).

---

## 9. How to Run

1. Complete steps 5–8 above (database created, driver on classpath).
2. In Eclipse, open `src/com/hospital/main/Main.java`.
3. Right-click → `Run As` → `Java Application`.
4. The login screen will appear.

**From the command line** (after compiling with the connector on the classpath):

```bash
javac -cp lib/mysql-connector-j-x.x.x.jar -d bin $(find src -name "*.java")
java -cp "bin:lib/mysql-connector-j-x.x.x.jar" com.hospital.main.Main
```
(On Windows, replace `:` with `;` in the `-cp` argument.)

---

## 10. Default Login Credentials

| Username | Password  | Role  |
|----------|-----------|-------|
| admin    | admin123  | ADMIN |

---

## 11. Screenshots

*(Add screenshots of the Login screen, Dashboard, Patient Management,
Appointment Management, and Billing screens here once you've run the
application.)*

- `screenshots/login.png`
- `screenshots/dashboard.png`
- `screenshots/patients.png`
- `screenshots/appointments.png`
- `screenshots/billing.png`

---

## 12. Future Enhancements

- Hash passwords (e.g. with BCrypt) instead of storing them in plain text
- Add role-based access control (e.g. RECEPTIONIST vs ADMIN sees different screens)
- Export patient bills to PDF
- Add pagination to JTables for large datasets
- Add unit tests for DAO classes using an in-memory database (H2)
- Add a REST API layer with Spring Boot as a "v2" of the project

---

## Security Note

This project stores passwords as **plain text** in the `users` table and
compares them directly in `UserDAO#validateLogin`. This is intentional for
an educational, fresher-portfolio project so the SQL/JDBC flow stays simple
and easy to explain. **In a production system**, passwords should never be
stored in plain text — instead, hash them with a strong algorithm such as
BCrypt or Argon2 at registration time, and compare hashes (not raw
passwords) at login time. Additionally, in production you would want to:
- Use environment variables or a config file for DB credentials instead of
  hardcoding them in `DBConnection.java`
- Enforce HTTPS/TLS if this were exposed as a web service
- Add account lockout after repeated failed login attempts

All SQL in this project uses `PreparedStatement` with parameter binding
(never string concatenation), which protects against SQL injection
regardless of the password storage scheme.
