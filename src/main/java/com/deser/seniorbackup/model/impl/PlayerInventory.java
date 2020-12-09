package com.deser.seniorbackup.model.impl;

import com.deser.seniorbackup.model.PlayerInventoryArgument;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor
public class PlayerInventory extends PlayerInventoryArgument {
    public PlayerInventory(final String items, final String armors) {
        super(items, armors);
    }

    public PlayerInventory(final ItemStack[] contents, final ItemStack[] armorContents) {
        super(contents, armorContents);
    }
}