package com.deser.seniorbackup.manager;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.builder.BackupBuilder;
import com.deser.seniorbackup.builder.PlayerInventoryBuilder;
import com.deser.seniorbackup.dao.BackupDAO;
import com.deser.seniorbackup.model.BackupArgument;
import com.deser.seniorbackup.model.PlayerInventoryArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class BackupManager {
    private final SeniorBackup main;
    private final BackupDAO dao;

    public BackupManager(final SeniorBackup main) {
        this.main = main;
        this.dao = main.getConnectionFactory().getDao();
    }

    public void createBackup(final Player p) {
        final String uuid = p.getUniqueId().toString();
        final ItemStack[] items = p.getInventory().getContents();
        final ItemStack[] armors = p.getInventory().getArmorContents();
        final PlayerInventoryArgument invArg = new PlayerInventoryBuilder()
                .items(items)
                .armors(armors)
                .build();

        final CompletableFuture<BackupArgument> data = dao.getArgument(uuid);
        data.thenAcceptAsync(backupArg -> {
            final long time = System.currentTimeMillis();
            if (backupArg == null) {
                backupArg = new BackupBuilder()
                        .times(time)
                        .inventories(invArg)
                        .build();
            } else backupArg.getBackups().put(time, invArg);
            dao.replace(uuid, backupArg);
        });
    }

    public PlayerInventoryArgument replaceItems(final OfflinePlayer op, final long time, final ItemStack[] items, final ItemStack[] armors) {
        final String uuid = op.getUniqueId().toString();
        final PlayerInventoryArgument newInvArg = new PlayerInventoryBuilder()
                .items(items)
                .armors(armors)
                .build();

        final CompletableFuture<BackupArgument> data = dao.getArgument(uuid);
        data.thenAcceptAsync(backupArg -> {
            if (backupArg == null) return;
            if (!backupArg.getBackups().containsKey(time)) return;
            backupArg.getBackups().put(time, newInvArg);
            dao.replace(uuid, backupArg);
        }); return newInvArg;
    }

    public boolean containsBackup(final OfflinePlayer p) {
        final String uuid = p.getUniqueId().toString();
        return dao.isExists(uuid).join();
    }

    public BackupArgument getBackups(final OfflinePlayer p) {
        final String uuid = p.getUniqueId().toString();
        return dao.getArgument(uuid).join();
    }
}
