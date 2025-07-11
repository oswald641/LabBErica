//https://www.tembo.io/docs/getting-started/postgres_guides/connecting-to-postgres-with-java
package com.theknife.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHandler {
    String jdbcUrl = "jdbc:postgresql://localhost:5432/theknife";
    String username = "postgres";
    String password = "postgres";
    Connection connection = null;

    public DBHandler() {
        try {
            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection(jdbcUrl, username, password);

            System.out.println("Successfully connected to the database");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connection to database failed: " + e.getMessage());
        }
    }
}
