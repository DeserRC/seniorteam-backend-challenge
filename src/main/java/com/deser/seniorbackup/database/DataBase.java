package com.deser.seniorbackup.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataBase {
    void openConnection();
    void closeConnection();
    void createTables();
    boolean isConnect();
    Connection getConnection() throws SQLException;
}
