package za.co.wethinkcode.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbConfig {

    private static final String DB_URL = "jdbc:sqlite:robot-worlds.db";

    public static void main(String[] args) {
        try {
            //load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // connect to db
            try (Connection connection = DriverManager.getConnection(DB_URL)){
                if (connection != null) {
                    System.out.println("Connected to " + DB_URL);

                    // read sql scripts from the file
                    String sqlscript = Files.readString(Paths.get("schema.sql"));

                    try (Statement statement = connection.createStatement()) {
                        String[] statements = sqlscript.split(";");
                        // loop through the '.sql' file and read all statements.
                        for (String stmt : statements) {
                            if (!stmt.trim().isEmpty()) {
                                statement.execute(stmt);
                            }
                        }
                        System.out.println("db schema has been applied.");
                    }
                }

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } catch (ClassNotFoundException e) {
            System.err.println("Couldn't load JDBC driver");
            e.printStackTrace();
        }
        catch (SQLException e) {
            System.out.println("Database connection failed");
            e.printStackTrace();
        }
    }
}