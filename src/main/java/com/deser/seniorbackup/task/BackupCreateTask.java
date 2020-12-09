package com.deser.seniorbackup.task;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.manager.BackupManager;
import org.bukkit.Bukkit;

public class BackupCreateTask implements Runnable {
    private final SeniorBackup main;
    private final BackupManager backup;

    public BackupCreateTask(final SeniorBackup main) {
        this.main = main;
        this.backup = main.getBackupFactory().getBackup();
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(backup::createBackup);
    }
}
