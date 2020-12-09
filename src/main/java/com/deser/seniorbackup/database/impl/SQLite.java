package com.deser.seniorbackup.database.impl;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.database.DataBase;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class SQLite implements DataBase {
    private final SeniorBackup main;
    private final File file;
    private Connection connection;

    public SQLite(SeniorBackup main) {
        this.main = main;
        this.file = new File(main.getDataFolder(), "/backup.db");
        openConnection();
    }

    @Override
    public synchronized void openConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.printf("Not possible connect to SQLite: %s", e.getMessage());
            Bukkit.getPluginManager().disablePlugin(main);
        }
    }

    @Override
    public synchronized void closeConnection() {
        if (!isConnect()) return;

        try {
            connection.close();
        } catch (SQLException e) {
            System.err.printf("An error occurred while close SQLite: %s", e.getMessage());
        }
    }

    @Override
    public synchronized void createTables() {
        try (final PreparedStatement stm = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `backup` (`uuid` VARCHAR(36) PRIMARY KEY, `inventories` MEDIUMTEXT NOT NULL)")) {
            stm.executeUpdate();
        } catch (SQLException e) {
            System.err.printf("It was not possible create the table in SQLite: %s", e.getMessage());
            Bukkit.getPluginManager().disablePlugin(main);
        }
    }

    @Override
    public boolean isConnect() {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            System.err.printf("Not possible check if the connection with SQLite is open: %s", e.getMessage());
            return false;
        }
    }
}
