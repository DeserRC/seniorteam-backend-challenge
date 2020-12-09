package com.deser.seniorbackup.task;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.dao.BackupDAO;
import com.deser.seniorbackup.manager.ConfigManager;
import com.deser.seniorbackup.model.BackupArgument;

import static java.util.concurrent.TimeUnit.DAYS;

public class BackupDeleteTask implements Runnable {
    private final SeniorBackup main;
    private final ConfigManager config;
    private final BackupDAO dao;

    public BackupDeleteTask(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();
        this.dao = main.getConnectionFactory().getDao();
    }

    @Override
    public void run() {
        final int purge = config.getConfig("Tasks.Purge");
        dao.getAll().thenAccept(r -> {
            for (String uuid : r.keySet()) {
                final BackupArgument backupArg = r.get(uuid);
                for (Long time : backupArg.getBackups().keySet()) {
                    if (time + DAYS.toMillis(purge) <= System.currentTimeMillis()) {
                        r.get(uuid).getBackups().remove(time);
                    }
                }

                if (backupArg.getBackups().isEmpty()) {
                    dao.delete(uuid);
                } else dao.replace(uuid, backupArg);
            }
        });
    }
}
