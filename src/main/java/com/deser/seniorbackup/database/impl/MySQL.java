package com.deser.seniorbackup.database.impl;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.database.DataBase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class MySQL implements DataBase {
    private final SeniorBackup main;
    private final String host, database, user, password;
    private Connection connection;

    public MySQL(final SeniorBackup main, final String host, final String user, final String database, final String password) {
        this.main = main;
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        openConnection();
    }

    @Override
    public synchronized void openConnection() {
        try {
            final HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
            config.setUsername(user);
            config.setPassword(password);

            config.setMinimumIdle(1);
            config.setMaximumPoolSize(20);

            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            config.addDataSourceProperty("autoReconnect", "true");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            connection = new HikariDataSource(config).getConnection();
            createTables();
        } catch (SQLException e) {
            System.err.printf("Not possible connect to MySQL: %s", e.getMessage());
            Bukkit.getPluginManager().disablePlugin(main);
        }
    }

    @Override
    public synchronized void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.printf("An error occurred while close MySQL: %s", e.getMessage());
        }
    }

    @Override
    public synchronized void createTables() {
        try (final PreparedStatement stm = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `backup` (`uuid` VARCHAR(36) PRIMARY KEY, `inventories` MEDIUMTEXT NOT NULL)")) {
            stm.executeUpdate();
        } catch (SQLException e) {
            System.err.printf("It was not possible create the table in MySQL: %s", e.getMessage());
            Bukkit.getPluginManager().disablePlugin(main);
        }
    }

    @Override
    public boolean isConnect() {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            System.err.printf("Not possible check if the connection with MySQL is open: %s", e.getMessage());
            return false;
        }
    }
}
