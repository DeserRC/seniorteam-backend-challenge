package com.deser.seniorbackup.util.impl;

import com.deser.seniorbackup.util.MessagesUtil;
import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.manager.ConfigManager;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import static com.deser.seniorbackup.util.ReflectionUtil.*;

public class BarUtil extends MessagesUtil {
    private final SeniorBackup main;
    private final ConfigManager config;

    private final Constructor<?> ppocCon;

    private final Method a;

    private final Object type;

    @SneakyThrows
    public BarUtil(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();

        final Class<?> cmtClass;
        final Class<?> icbcClass = getNMS("IChatBaseComponent");
        final Class<?> ppocClass = getNMS("PacketPlayOutChat");

        if (isEqualsOrMoreRecent(12)) {
            cmtClass = getNMS("ChatMessageType");
            type = cmtClass.getEnumConstants() [2];
        } else {
            cmtClass = byte.class;
            type = (byte) 2;
        }

        if (icbcClass.getDeclaredClasses().length > 0) {
            a = icbcClass.getDeclaredClasses() [0].getMethod("a", String.class);
        } else a = getNMS("ChatSerializer").getMethod("a", String.class);

        if (isEqualsOrMoreRecent(16)) {
            ppocCon = ppocClass.getConstructor(icbcClass, cmtClass, UUID.class);
        } else ppocCon = ppocClass.getConstructor(icbcClass, cmtClass);
    }

    @Override @SafeVarargs
    public final <T, U> void send(final CommandSender sender, final String path, final Map.Entry<T, U>... map) {
        send(sender, path, config.getMessage(), map);
    }

    @Override @SafeVarargs @SneakyThrows
    public final <T, U> void send(final CommandSender sender, final String path, final FileConfiguration file, final Map.Entry<T, U>... map) {
        if (!(sender instanceof Player)) return;

        final Player p = (Player) sender;
        String message = path;

        final boolean containsInConfig = config.contains(path, file);
        if (containsInConfig) {
            final boolean useBar = config.get(path + ".Use", file);
            if (!useBar) return;
            message = config.get(path + ".Message", file);
        }

        for (Map.Entry<T, U> entry : map) {
            message = message.replace(entry.getKey().toString(), entry.getValue().toString());
        }

        if (isEqualsOrMoreRecent(16)) {
            final Object chatBase = a.invoke(null, "{\"text\":\"" + message + "\"}");
            final Object packet = ppocCon.newInstance(chatBase, type, p.getUniqueId());
            sendPacket(p, packet);
            return;
        }
        final Object chatBase = a.invoke(null, "{\"text\":\"" + message + "\"}");
        final Object packet = ppocCon.newInstance(chatBase, type);
        sendPacket(p, packet);
    }
}