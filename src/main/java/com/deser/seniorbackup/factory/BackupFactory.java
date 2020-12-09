package com.deser.seniorbackup.factory;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.manager.BackupManager;
import lombok.Getter;

@Getter
public class BackupFactory {
    private final SeniorBackup main;
    private final BackupManager backup;

    public BackupFactory(final SeniorBackup main) {
        this.main = main;
        this.backup = new BackupManager(main);
    }
}
