package com.deser.seniorbackup.holder;

import com.deser.seniorbackup.model.PlayerInventoryArgument;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@AllArgsConstructor
@Data
public class EditHolder implements InventoryHolder {
    private final OfflinePlayer target;
    private final long time;
    private PlayerInventoryArgument invArg;

    @Override
    public Inventory getInventory() {
        return null;
    }
}
