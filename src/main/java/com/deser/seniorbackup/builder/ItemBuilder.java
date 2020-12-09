package com.deser.seniorbackup.builder;

import com.deser.seniorbackup.type.EnchantType;
import com.deser.seniorbackup.util.ReflectionUtil;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Material.SKULL_ITEM;
import static org.bukkit.enchantments.Enchantment.DURABILITY;
import static org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS;

public class ItemBuilder {
    private final ItemStack itemStack;

    public ItemBuilder(final Material material, final int amount) {
        this.itemStack = new ItemStack(material, amount);
    }

    public ItemBuilder(final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemBuilder setData(final int data) {
        itemStack.setDurability((short) data);
        return this;
    }

    public ItemBuilder setName(final String name) {
        if (name == null || name.equals("")) return this;
        final ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(final List<String> lore) {
        if (lore == null || lore.size() == 0) return this;
        final ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(final List<String> enchantment) {
        if (enchantment == null) return this;
        for (String en : enchantment) {
            final String[] splitEnchant = en.split(":");
            final Enchantment enchant = EnchantType.getEnchant(splitEnchant[0]);
            if (enchant == null) continue;
            final int level = Integer.parseInt(splitEnchant[1]);
            itemStack.addUnsafeEnchantment(enchant, level);
        }
        return this;
    }

    public ItemBuilder setGlow(final boolean glow) {
        if (!glow) return this;
        itemStack.addUnsafeEnchantment(DURABILITY, 1);
        final ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return this;
    }

    @SneakyThrows
    public ItemBuilder setSkull(String texture, final UUID uuid) {
        if (itemStack.getType() != SKULL_ITEM) return this;

        texture = "http://textures.minecraft.net/texture/" + texture;
        final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        final Class<?> profileClass = ReflectionUtil.getClazz("com.mojang.authlib.", "GameProfile");
        final Class<?> propertyClass = ReflectionUtil.getClazz("com.mojang.authlib.properties.", "Property");

        final Constructor<?> profileCon = ReflectionUtil.getCon(profileClass, UUID.class, String.class);
        final Constructor<?> propertyCon = ReflectionUtil.getCon(propertyClass, String.class, String.class);
        final Field propertiesField = ReflectionUtil.getDcField(profileClass, "properties");

        final String encoded = Base64.getEncoder().encodeToString(String.format("{textures:{SKIN:{url:\"%s\"}}}", new Object[] { texture }).getBytes());
        final Object profile = ReflectionUtil.instance(profileCon, uuid, null);
        final Object property = ReflectionUtil.instance(propertyCon, "textures", encoded);

        final Class<?> propertiesClass = ReflectionUtil.getType(propertiesField);

        final Method put = ReflectionUtil.getMethod(propertiesClass,"put", Object.class, Object.class);
        ReflectionUtil.invoke(put, propertiesField.get(profile),"textures", property);

        final Field profileField = ReflectionUtil.getDcField(meta.getClass(), "profile");
        profileField.set(meta, profile);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkullOwner(final String owner) {
        final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(owner);
        itemStack.setItemMeta(meta);
        return this;
    }

    @SneakyThrows
    public ItemBuilder addNBTTag(final String key, final long value) {
        final Class<?> craftItemStackClazz = ReflectionUtil.getOBC("inventory.CraftItemStack");
        final Class<?> itemStackClazz = ReflectionUtil.getNMS("ItemStack");
        final Class<?> compoundClazz = ReflectionUtil.getNMS("NBTTagCompound");

        final Method asNMSCopy = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
        final Method hasTag = ReflectionUtil.getMethod(itemStackClazz, "hasTag");
        final Method getTag = ReflectionUtil.getMethod(itemStackClazz, "getTag");

        final Object nmsItem = ReflectionUtil.invokeStatic(asNMSCopy, itemStack);
        final boolean isExist = (boolean) ReflectionUtil.invoke(hasTag, nmsItem);
        final Object compound;

        if (isExist) compound = ReflectionUtil.invoke(getTag, nmsItem);
        else compound = compoundClazz.newInstance();

        final Class<?> tagClazz = ReflectionUtil.getNMS("NBTTagLong");
        final Class<?> baseClazz = ReflectionUtil.getNMS("NBTBase");

        final Constructor<?> tagCon = ReflectionUtil.getCon(tagClazz, long.class);

        final Method set = ReflectionUtil.getMethod(compoundClazz, "set", String.class, baseClazz);
        final Method setTag = ReflectionUtil.getMethod(itemStackClazz, "setTag", compoundClazz);
        final Method getItemMeta = ReflectionUtil.getMethod(craftItemStackClazz, "getItemMeta", itemStackClazz);

        final Object tag = ReflectionUtil.instance(tagCon, value);
        ReflectionUtil.invoke(set, compound, key, tag);
        ReflectionUtil.invoke(setTag, nmsItem, compound);

        final ItemMeta meta = (ItemMeta) ReflectionUtil.invokeStatic(getItemMeta, nmsItem);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStack toItemStack() {
        return itemStack;
    }
}