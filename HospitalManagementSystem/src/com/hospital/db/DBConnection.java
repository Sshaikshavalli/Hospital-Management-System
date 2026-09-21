package com.hospital.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Shaik@123"; // <-- change to your MySQL root password

    static {
        try {
          
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL JDBC Driver not found on classpath. "
                            + "Add mysql-connector-j-x.x.x.jar to the project build path.", e);
        }
    }

   
    private DBConnection() {
    }

  
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
