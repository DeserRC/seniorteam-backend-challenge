package com.deser.seniorbackup.listener;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.holder.BackupHolder;
import com.deser.seniorbackup.holder.EditHolder;
import com.deser.seniorbackup.manager.BackupManager;
import com.deser.seniorbackup.manager.ConfigManager;
import com.deser.seniorbackup.manager.InventoryManager;
import com.deser.seniorbackup.model.BackupArgument;
import com.deser.seniorbackup.model.PlayerInventoryArgument;
import com.deser.seniorbackup.util.impl.BarUtil;
import com.deser.seniorbackup.util.impl.ChatUtil;
import com.deser.seniorbackup.util.impl.TitleUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    private final SeniorBackup main;
    private final ConfigManager config;
    private final InventoryManager inventory;
    private final BackupManager backup;
    private final ChatUtil chat;
    private final BarUtil bar;
    private final TitleUtil title;

    public InventoryListener(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();
        this.inventory = main.getInventoryManager();
        this.backup = main.getBackupFactory().getBackup();
        this.chat = main.getMessageFactory().getChat();
        this.bar = main.getMessageFactory().getBar();
        this.title = main.getMessageFactory().getTitle();
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onInventoryBackupsClickEvent(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        final Inventory inv = e.getClickedInventory();

        if (inv == null) return;
        if (!(inv.getHolder() instanceof BackupHolder)) return;
        e.setCancelled(true);

        final BackupHolder backupHolder = (BackupHolder) inv.getHolder();
        final OfflinePlayer target = backupHolder.getTarget();
        final BackupArgument backupArg = backupHolder.getBackupArg();
        final int slot = e.getSlot();

        final ItemStack item = inv.getItem(slot);
        if (item == null) return;

        final int slotPrevious = config.getConfig("Inventory.Custom-Items.Previous-Page.Slot");
        if (slot == slotPrevious) {
            inventory.previousPage(p, inv);
            return;
        }

        final int slotNext = config.getConfig("Inventory.Custom-Items.Next-Page.Slot");
        if (slot == slotNext) {
            inventory.nextPage(p, inv);
            return;
        }

        if (!inventory.containsNBTTag(item)) return;
        final long time = inventory.getNBTTAG(item);
        final PlayerInventoryArgument invArg = backupArg.getBackups().get(time);

        final boolean rightClick = e.getClick().isRightClick();
        if (rightClick) {
            inventory.openEdit(p, time, invArg, backupHolder);
            return;
        }

        final ItemStack[] contents = invArg.getContents();
        final ItemStack[] armorContents = invArg.getArmorContents();
        p.getInventory().setContents(contents);
        p.getInventory().setArmorContents(armorContents);

        chat.send(p,"Backup.Success",
                chat.build("{player}", target.getName()),
                chat.build("{date}", config.dateFormat(time)));
        bar.send(p,"Backup.Success-Bar",
                chat.build("{player}", target.getName()),
                chat.build("{date}", config.dateFormat(time)));
        title.send(p,"Backup.Success-Title",
                chat.build("{player}", target.getName()),
                chat.build("{date}", config.dateFormat(time)));
    }

    @EventHandler
    public void onInventoryEditClickEvent(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        final Inventory inv = e.getClickedInventory();

        if (inv == null) return;
        if (!(inv.getHolder() instanceof EditHolder)) return;

        final EditHolder editHolder = (EditHolder) inv.getHolder();
        final OfflinePlayer op = editHolder.getTarget();
        final int slot = e.getSlot();

        final int slotBack = config.getConfig("Inventory-Edit.Custom-Items.Back.Slot");
        if (slot != slotBack) return;

        e.setCancelled(true);
        inventory.openBackups(p, op);
    }

    @EventHandler
    public void onInventoryEditCloseEvent(InventoryCloseEvent e) {
        final Inventory inv = e.getInventory();

        if (inv == null) return;
        if (!(inv.getHolder() instanceof EditHolder)) return;

        final EditHolder editHolder = (EditHolder) inv.getHolder();
        final OfflinePlayer op = editHolder.getTarget();

        final long time = editHolder.getTime();
        final ItemStack[] invContents = inv.getContents().clone();
        final ItemStack[] contents = new ItemStack[36];
        final ItemStack[] armorContents = new ItemStack[4];

        for (int slot=0; slot<40; slot++) {
            ItemStack item = invContents[slot];
            if (slot < 36) contents[slot] = item;
            else armorContents[slot - 36] = item;
        }

        PlayerInventoryArgument invArg = backup.replaceItems(op, time, contents, armorContents);
        editHolder.setInvArg(invArg);
    }
}
