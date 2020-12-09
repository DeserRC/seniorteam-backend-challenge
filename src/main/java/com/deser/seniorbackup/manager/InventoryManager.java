package com.deser.seniorbackup.manager;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.builder.ItemBuilder;
import com.deser.seniorbackup.holder.BackupHolder;
import com.deser.seniorbackup.holder.EditHolder;
import com.deser.seniorbackup.model.BackupArgument;
import com.deser.seniorbackup.model.PlayerInventoryArgument;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.deser.seniorbackup.util.ReflectionUtil.*;
import static java.util.stream.Collectors.toList;
import static org.bukkit.Material.*;

public class InventoryManager {
    private final SeniorBackup main;
    private final ConfigManager config;
    private final BackupManager backup;

    private final Method asNMSCopy;
    private final Method getLong;
    private final Method getTag;
    private final Method hasKey;

    @SneakyThrows
    public InventoryManager(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();
        this.backup = main.getBackupFactory().getBackup();

        final Class<?> craftItemStackClazz = getOBC("inventory.CraftItemStack");
        final Class<?> itemStackClazz = getNMS("ItemStack");
        final Class<?> compoundClazz = getNMS("NBTTagCompound");

        this.asNMSCopy = getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
        this.getLong = getMethod(compoundClazz, "getLong", String.class);
        this.getTag = getMethod(itemStackClazz, "getTag");
        this.hasKey = getMethod(compoundClazz, "hasKey", String.class);
    }

    public void openBackups(final Player p, final OfflinePlayer op) {
        String title = config.getConfig("Inventory.Title");
        final int size = config.getConfig("Inventory.Size");

        title = title.replace("{player}", op.getName());

        final BackupArgument backupArg = backup.getBackups(op);
        final BackupHolder backupHolder = new BackupHolder(op, backupArg, 1, null);
        final Inventory inv = Bukkit.createInventory(backupHolder, size, title);

        setDecoration(op, inv, "Inventory.Decoration", build("{player}", op.getName()));
        final int waitItemAmount = setTemplate(op, inv, backupArg,0);
        setCustomItems(op, inv, "Inventory.Custom-Items", 1, waitItemAmount);

        backupHolder.setWaitItemAmount(waitItemAmount);
        p.openInventory(inv);
    }

    public void previousPage(final Player p, final Inventory inv)   {
        final BackupHolder backupHolder = (BackupHolder) inv.getHolder();
        final OfflinePlayer op = backupHolder.getTarget();
        final BackupArgument backupArg = backupHolder.getBackupArg();
        final int page = backupHolder.getPage() - 1;
        int waitItemAmount = backupHolder.getWaitItemAmount();

        backupHolder.setPage(page);
        waitItemAmount = (waitItemAmount / page + 1) * page;

        inv.clear();

        setDecoration(op, inv, "Inventory.Decoration", build("{player}", op.getName()));
        waitItemAmount = setTemplate(op, inv, backupArg, waitItemAmount);
        setCustomItems(op, inv, "Inventory.Custom-Items", page, waitItemAmount);

        backupHolder.setWaitItemAmount(waitItemAmount * page);
        p.openInventory(inv);
    }

    public void nextPage(final Player p, final Inventory inv) {
        final BackupHolder backupHolder = (BackupHolder) inv.getHolder();
        final OfflinePlayer op = backupHolder.getTarget();
        final BackupArgument backupArg = backupHolder.getBackupArg();
        final int page = backupHolder.getPage() + 1;
        int waitItemAmount = backupHolder.getWaitItemAmount();

        backupHolder.setPage(page);

        inv.clear();

        setDecoration(op, inv, "Inventory.Decoration", build("{player}", op.getName()));
        waitItemAmount = setTemplate(op, inv, backupArg, waitItemAmount);
        setCustomItems(op, inv, "Inventory.Custom-Items", page, waitItemAmount);

        backupHolder.setWaitItemAmount(waitItemAmount * page);
        p.openInventory(inv);
    }

    public void openEdit(final Player p, final long time, final PlayerInventoryArgument invArg, final BackupHolder backupHolder) {
        String title = config.getConfig("Inventory.Title");
        final OfflinePlayer op = backupHolder.getTarget();

        title = title.replace("{player}", op.getName())
                .replace("{date}", config.dateFormat(time));

        final EditHolder editHolder = new EditHolder(op, time, invArg);
        final Inventory inv = Bukkit.createInventory(editHolder, 45, title);

        setItems(inv, invArg);
        setCustomItems(op, inv, "Inventory-Edit.Custom-Items", 0, 0);
        p.openInventory(inv);
    }

    @SafeVarargs
    private final void setDecoration(final OfflinePlayer op, final Inventory inv, final String path, final Map.Entry<String, String>... map) {
        FileConfiguration file = config.getConfig();
        for (String name : config.getKeys(path, file)) {
            final String newPath = path + "." + name + ".";

            final int slot = config.getConfig(newPath + "Slot");
            final ItemStack item = getItem(op, file, newPath, map);
            inv.setItem(slot, item);
        }
    }

    private void setCustomItems(final OfflinePlayer op, final Inventory inv, final String path, final int page, final int waitItemAmount) {
        final FileConfiguration file = config.getConfig();
        for (String name : config.getKeys(path, file)) {
            if (name.equals("Previous-Page") && page == 1) continue;
            if (name.equals("Next-Page") && waitItemAmount == 0) continue;
            final String newPath = path + "." + name + ".";

            final int slot = config.getConfig(newPath + "Slot");
            final ItemStack item = getItem(op, file, newPath);
            inv.setItem(slot, item);
        }
    }

    private int setTemplate(final OfflinePlayer op, final Inventory inv, final BackupArgument backupArg, int waitItemAmount) {
        final Map<Long, PlayerInventoryArgument> backups = backupArg.getBackups();
        final FileConfiguration file = config.getConfig();

        int slot = 0;
        for (Long time : backups.keySet()) {
            if (waitItemAmount != 0) {
                waitItemAmount--;
                continue;
            }

            if (slot >= inv.getSize()) {
                return slot;
            }

            while (inv.getItem(slot) != null) { slot++;
                if (slot >= inv.getSize()) return slot;
            }

            ItemStack item = getItem(op, file, "Inventory.Template.",
                    build("{player}", op.getName()),
                    build("{date}", config.dateFormat(time)));
            item = new ItemBuilder(item)
                    .addNBTTag("date", time)
                    .toItemStack();
            inv.setItem(slot, item);
        }
        return 0;
    }

    public void setItems(final Inventory inv, final PlayerInventoryArgument invArg) {
        final ItemStack[] contents = invArg.getContents();
        final ItemStack[] armorContents = invArg.getArmorContents();

        for (int slot=0; slot<40; slot++) {
            ItemStack item;
            if (slot < 36) item = contents[slot];
            else item = armorContents[slot - 36];

            if (item == null) continue;
            inv.setItem(slot, item);
        }
    }

    @SafeVarargs
    private final ItemStack getItem(final OfflinePlayer op, final FileConfiguration file, final String path, final Map.Entry<String, String>... map) {
        String name             = config.get(path + "Name", file);
        final int amount        = config.get(path + "Amount", file);
        final boolean glow      = config.get(path + "Glow", file);
        List<String> lore       = config.getList(path + "Lore", file);
        final List<String> en   = config.getList(path + "Enchantment", file);

        for (Map.Entry<String, String> entry : map) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            name = name.replace(key, value);
            lore = lore.stream().map(line -> line
                    .replace(key, value))
                    .collect(toList());
        }

        final boolean isHead = config.get(path + "Head.Use", file);
        if (!isHead) {
            final Material material = getMaterial(config.get(path + "Material", file));
            final int data = config.get(path + "Data", file);
            return new ItemBuilder(material, amount)
                    .setData(data)
                    .setName(name)
                    .setGlow(glow)
                    .setLore(lore)
                    .addEnchant(en)
                    .toItemStack();
        }

        final String texture = config.get(path + "Head.Texture", file);
        if (texture.equals("")) return new ItemBuilder(SKULL_ITEM, amount)
                .setData(3)
                .setName(name)
                .setGlow(glow)
                .setLore(lore)
                .addEnchant(en)
                .setSkullOwner(op.getName())
                .toItemStack();

        final boolean contains = config.contains(path + "Head.UUID", file);
        final UUID uuid = contains ? UUID.fromString(config.get(path + "Head.UUID", file)) : UUID.randomUUID();
        return new ItemBuilder(SKULL_ITEM, amount)
                .setData(3)
                .setName(name)
                .setGlow(glow)
                .setLore(lore)
                .addEnchant(en)
                .setSkull(texture, uuid)
                .toItemStack();
    }

    @SneakyThrows
    public long getNBTTAG(final ItemStack item) {
        final Object nmsItem = invokeStatic(asNMSCopy, item);
        final Object compound = invoke(getTag, nmsItem);
        return (long) getLong.invoke(compound,"date");
    }

    @SneakyThrows
    public boolean containsNBTTag(final ItemStack item) {
        final Object nmsItem = invokeStatic(asNMSCopy, item);
        if (nmsItem == null) return false;

        final Object compound = invoke(getTag, nmsItem);
        if (compound == null) return false;
        return (boolean) invoke(hasKey, compound, "date");
    }

    private <T, U> Map.Entry<String, String> build(final T key, final U value) {
        return new AbstractMap.SimpleEntry<>(key.toString(), value.toString());
    }
}
