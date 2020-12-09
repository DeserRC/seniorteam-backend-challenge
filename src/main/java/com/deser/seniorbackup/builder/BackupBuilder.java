package com.deser.seniorbackup.builder;

import com.deser.seniorbackup.model.BackupArgument;
import com.deser.seniorbackup.model.PlayerInventoryArgument;
import com.deser.seniorbackup.model.impl.Backup;

import java.util.LinkedHashMap;
import java.util.Map;

public class BackupBuilder {
    private long[] times;
    private PlayerInventoryArgument[] inventories;

    public BackupBuilder times(final long... times) {
        this.times = times;
        return this;
    }

    public BackupBuilder inventories(final PlayerInventoryArgument... inventories) {
        this.inventories = inventories;
        return this;
    }

    public BackupArgument build() {
        final Map<Long, PlayerInventoryArgument> backups = new LinkedHashMap<>();
        for (Long time : times) { for (PlayerInventoryArgument inventory : inventories) {
            backups.put(time, inventory);
        } }
        return new Backup(backups);
    }
}
