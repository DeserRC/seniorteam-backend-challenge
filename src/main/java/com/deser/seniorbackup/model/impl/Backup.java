package com.deser.seniorbackup.model.impl;

import com.deser.seniorbackup.model.BackupArgument;
import com.deser.seniorbackup.model.PlayerInventoryArgument;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class Backup extends BackupArgument {
    public Backup(final Map<Long, PlayerInventoryArgument> backups) {
        super(backups);
    }
}
