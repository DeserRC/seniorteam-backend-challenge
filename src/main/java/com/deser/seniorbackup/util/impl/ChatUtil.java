package com.deser.seniorbackup.util.impl;

import com.deser.seniorbackup.util.MessagesUtil;
import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.manager.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class ChatUtil extends MessagesUtil {
    private final SeniorBackup main;
    private final ConfigManager config;

    public ChatUtil(final SeniorBackup main) {
        this.main = main;
        this.config = main.getConfigManager();
    }

    @Override @SafeVarargs
    public final <T, U> void send(final CommandSender sender, final String path, final Map.Entry<T, U>... map) {
        send(sender, path, config.getMessage(), map);
    }

    @Override @SafeVarargs
    public final <T, U> void send(final CommandSender sender, final String path, final FileConfiguration file, final Map.Entry<T, U>... map) {
        String message = path;

        final boolean containsInConfig = config.contains(path, file);
        if (containsInConfig) {
            final boolean containsUse = config.contains(path + ".Use", file);
            final boolean useChat = containsUse ? config.get(path + ".Use", file) : false;
            if (!containsUse) message = config.get(path, file);
            else if (useChat) message = config.get(path + ".Message", file);
            else return;
        }

        for (Map.Entry<T, U> entry : map) {
            message = message.replace(entry.getKey().toString(), entry.getValue().toString());
        }
        sender.sendMessage(message);
    }
}
