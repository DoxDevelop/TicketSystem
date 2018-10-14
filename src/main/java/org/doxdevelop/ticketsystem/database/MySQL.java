package org.doxdevelop.ticketsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private Connection conn;


    public MySQL(String hostname, String port, String database, String username, String password) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (conn != null && !conn.isClosed()) {
            return conn;
        }
        Class.forName("com.mysql.jdbc.Driver");
        return conn = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user, this.password);
    }

}