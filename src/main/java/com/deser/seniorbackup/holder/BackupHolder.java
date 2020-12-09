package com.deser.seniorbackup.holder;

import com.deser.seniorbackup.model.BackupArgument;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@AllArgsConstructor
@Data
public class BackupHolder implements InventoryHolder {
    private final OfflinePlayer target;
    private final BackupArgument backupArg;
    private Integer page;
    private Integer waitItemAmount;

    @Override
    public Inventory getInventory() {
        return null;
    }
}
