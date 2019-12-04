package org.deibic2s.ooka.rte.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    static final String DRIVER = "jdbc:sqlite:";
    private static String userHomeURL = null;
    private static boolean userHomeDbExist = false;
    private static boolean userHomeTableExist = false;

    static Connection getConnection(){
        return getConnection(true);
    }

    static Connection getConnection(boolean useHomeAsDefault) {

            if (!useHomeAsDefault || !userHomeTableExist) {
                userHomeURL = new File(new File(System.getProperty("user.home")), "rte.db").toURI().getPath();
                if (createDBIfNotExist(new File(userHomeURL))) {
                    userHomeDbExist = true;
                    userHomeTableExist = createTablesIfNotExist(new File(userHomeURL));
                }
            }
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
            if(useHomeAsDefault && userHomeDbExist && userHomeTableExist)
                return DriverManager.getConnection(DRIVER+userHomeURL);

            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error connecting to the database", ex);
        }
    }

    private static boolean createDBIfNotExist(File fileToDB){
        if(fileToDB.isFile() && fileToDB.canWrite())
            return true;

        try {
            return fileToDB.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }

    private static boolean createTablesIfNotExist(File fileToDB) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
            Connection c = DriverManager.getConnection(DRIVER + fileToDB.toURI().getPath());
            Statement stmt = c.createStatement();
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS datacomponent(" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "COMPONENTID INTEGER NOT NULL," +
                            "COMPONENTSTATE VARCHAR(20) NOT NULL," +
                            "PATHTOCOMPONENT VARCHAR(200) NOT NULL," +
                            "COMPONENTNAME VARCHAR(200))"
            );
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
