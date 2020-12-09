package com.deser.seniorbackup.factory;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.database.DataBase;
import com.deser.seniorbackup.database.impl.MySQL;
import com.deser.seniorbackup.database.impl.SQLite;
import com.deser.seniorbackup.manager.ConfigManager;
import com.deser.seniorbackup.dao.BackupDAO;
import lombok.Getter;

@Getter
public class ConnectionFactory {
    private final SeniorBackup main;
    private final ConfigManager config;
    private final DataBase dataBase;
    private final BackupDAO dao;

    public ConnectionFactory(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();

        final boolean useMySQL = config.getConfig("MySQL.Use");
        if (useMySQL) {
            final String host = config.getConfig("MySQL.Host");
            final String user = config.getConfig("MySQL.User");
            final String database = config.getConfig("MySQL.DataBase");
            final String password = config.getConfig("MySQL.Password");
            this.dataBase = new MySQL(main, host, user, database, password);
        } else this.dataBase = new SQLite(main);
        this.dao = new BackupDAO(main, dataBase);
    }
}