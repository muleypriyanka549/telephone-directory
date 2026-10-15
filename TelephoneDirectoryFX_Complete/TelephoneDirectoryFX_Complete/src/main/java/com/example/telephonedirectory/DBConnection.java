package com.example.telephonedirectory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/telephone_directory"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASSWORD = "root123"; // Change only if your MySQL password is different.

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
