package com.deser.seniorbackup.factory;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.task.BackupCreateTask;
import com.deser.seniorbackup.task.BackupDeleteTask;
import lombok.Getter;

@Getter
public class TaskFactory {
    private final SeniorBackup main;
    private final BackupCreateTask backupCreateTask;
    private final BackupDeleteTask backupDeleteTask;

    public TaskFactory(final SeniorBackup main) {
        this.main = main;
        this.backupCreateTask = new BackupCreateTask(main);
        this.backupDeleteTask = new BackupDeleteTask(main);
    }
}
