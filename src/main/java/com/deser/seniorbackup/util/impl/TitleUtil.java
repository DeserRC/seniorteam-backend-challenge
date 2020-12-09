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

import static com.deser.seniorbackup.util.ReflectionUtil.getNMS;
import static com.deser.seniorbackup.util.ReflectionUtil.sendPacket;

public class TitleUtil extends MessagesUtil {
    private final SeniorBackup main;
    private final ConfigManager config;

    private static Constructor<?> ppotTimeCon;
    private static Constructor<?> ppotTextCon;

    private static Method a;

    private static Object time;
    private static Object typeTitle;
    private static Object typeSubTitle;

    @SneakyThrows
    public TitleUtil(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();

        final Class<?> ppotClass = getNMS("PacketPlayOutTitle");
        final Class<?> icbcClass = getNMS("IChatBaseComponent");
        final Class<?> etaClass;

        if (ppotClass.getDeclaredClasses().length > 0) {
            etaClass = ppotClass.getDeclaredClasses()[0];
        } else etaClass = getNMS("EnumTitleAction");

        if (icbcClass.getDeclaredClasses().length > 0) {
            a = icbcClass.getDeclaredClasses()[0].getMethod("a", String.class);
        } else {
            a = getNMS("ChatSerializer").getMethod("a", String.class);
        }

        ppotTimeCon = ppotClass.getConstructor(etaClass, icbcClass, int.class, int.class, int.class);
        ppotTextCon = ppotClass.getConstructor(etaClass, icbcClass);

        time = etaClass.getField("TIMES").get(null);
        typeTitle = etaClass.getField("TITLE").get(null);
        typeSubTitle = etaClass.getField("SUBTITLE").get(null);
    }

    @Override @SafeVarargs
    public final <T, U> void send(final CommandSender sender, final String path, final Map.Entry<T, U>... map) {
        send(sender, path, config.getMessage(), map);
    }

    @Override @SafeVarargs @SneakyThrows
    public final <T, U> void send(final CommandSender sender, final String path, final FileConfiguration file, final Map.Entry<T, U>... map) {
        if (!(sender instanceof Player)) return;

        final Player p = (Player) sender;
        String title = path;
        String subtitle = path;

        final boolean containsInConfig = config.contains(path, file);
        if (containsInConfig) {
            final boolean useTitle = config.get(path + ".Use", file);
            if (!useTitle) return;
            title = config.get(path + ".Title", file);
            subtitle = config.get(path + ".SubTitle", file);
        }

        for (Map.Entry<T, U> entry : map) {
            title = title.replace(entry.getKey().toString(), entry.getValue().toString());
            subtitle = subtitle.replace(entry.getKey().toString(), entry.getValue().toString());
        }

        final Object timePacket = ppotTimeCon.newInstance(time, null, 10, 10, 10);
        sendPacket(p, timePacket);

        if (title != null) {
            final Object chatBase = a.invoke(null, "{\"text\":\"" + title + "\"}");
            final Object packet = ppotTextCon.newInstance(typeTitle, chatBase);
            sendPacket(p, packet);
        }

        if (subtitle != null) {
            final Object chatBase = a.invoke(null,"{\"text\":\"" + subtitle + "\"}");
            final Object packet = ppotTextCon.newInstance(typeSubTitle, chatBase);
            sendPacket(p, packet);
        }
    }
}