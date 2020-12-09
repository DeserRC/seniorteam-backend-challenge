package com.deser.seniorbackup.builder;

import com.deser.seniorbackup.model.PlayerInventoryArgument;
import com.deser.seniorbackup.model.impl.PlayerInventory;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryBuilder {
    private ItemStack[] items;
    private ItemStack[] armors;

    public PlayerInventoryBuilder items(final ItemStack[] items) {
        this.items = items;
        return this;
    }

    public PlayerInventoryBuilder armors(final ItemStack[] armors) {
        this.armors = armors;
        return this;
    }

    public PlayerInventoryArgument build() {
        return new PlayerInventory(items, armors);
    }
}
