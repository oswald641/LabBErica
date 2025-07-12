//https://www.tembo.io/docs/getting-started/postgres_guides/connecting-to-postgres-with-java
package com.theknife.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHandler {
    private Connection connection = null;

    public boolean connect(String jdbcUrl, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection(jdbcUrl, username, password);

            System.out.println("Successfully connected to the database");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Connection to database failed: " + e.getMessage());
            return false;
        }
    }

    public int initDB() throws SQLException, IOException {
        //checks if the db is initialized
        boolean initialized = true;
        try {
            connection.createStatement().execute("SELECT 1 FROM users");
        } catch (SQLException e) {
            initialized = false;
        }

        if(initialized)
            return 2;

        //loads the sql file
        InputStream is = getClass().getResourceAsStream("/init-db.sql");
        
        if(is == null) {
            System.err.println("Sql file \"init-db.sql\" not found");
            return 1;
        }

        String statement = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        
        //creates the tables
        connection.createStatement().execute(statement);
        return 0;
    }
}
