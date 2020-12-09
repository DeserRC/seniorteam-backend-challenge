package com.deser.seniorbackup.command.impl;

import com.deser.seniorbackup.SeniorBackup;
import com.deser.seniorbackup.command.Command;
import com.deser.seniorbackup.manager.BackupManager;
import com.deser.seniorbackup.manager.ConfigManager;
import com.deser.seniorbackup.manager.InventoryManager;
import com.deser.seniorbackup.util.EventUtil;
import com.deser.seniorbackup.util.impl.BarUtil;
import com.deser.seniorbackup.util.impl.ChatUtil;
import com.deser.seniorbackup.util.impl.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class BackupCommand extends Command {
    private final SeniorBackup main;
    private final ConfigManager config;
    private final InventoryManager inventory;
    private final BackupManager backup;
    private final ChatUtil chat;
    private final BarUtil bar;
    private final TitleUtil title;

    public BackupCommand(SeniorBackup main) {
        super("backup", "backups", "search");
        this.main = main;
        this.config = main.getConfigManager();
        this.inventory = main.getInventoryManager();
        this.backup = main.getBackupFactory().getBackup();
        this.chat = main.getMessageFactory().getChat();
        this.bar = main.getMessageFactory().getBar();
        this.title = main.getMessageFactory().getTitle();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            chat.send(sender, "This command cannot be executed via the console");
            return false;
        }

        final Player p = (Player) sender;
        if (args.length > 1) {
            chat.send(p, "Backup.Invalid-Argument");
            bar.send(p, "Backup.Invalid-Argument-Bar");
            title.send(p, "Backup.Invalid-Argument-Title");
        }

        if (args.length == 1) {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
            if (!backup.containsBackup(op)) {
                chat.send(p,"Backup.Player-Not-Found", chat.build("{player}", args[0]));
                bar.send(p,"Backup.Player-Not-Found-Bar", chat.build("{player}", args[0]));
                title.send(p,"Backup.Player-Not-Found-Title", chat.build("{player}", args[0]));
            } else inventory.openBackups(p, op);
            return false;
        }

        final int time = config.getConfig("Tasks.Time-Out");
        chat.send(p,"Backup.Request", chat.build("{time}", time));
        bar.send(p,"Backup.Request-Bar", chat.build("{time}", time));
        title.send(p,"Backup.Request-Title", chat.build("{time}", time));

        new EventUtil.EventBuilder<>(main, AsyncPlayerChatEvent.class)
            .filter(event -> p.equals(event.getPlayer()))
            .execute(event -> {
                event.setCancelled(true);

                final String message = event.getMessage();
                final OfflinePlayer op = Bukkit.getOfflinePlayer(message);

                if (!backup.containsBackup(op)) {
                    chat.send(p,"Backup.Player-Not-Found", chat.build("{player}", message));
                    bar.send(p,"Backup.Player-Not-Found-Bar", chat.build("{player}", message));
                    title.send(p,"Backup.Player-Not-Found-Title", chat.build("{player}", message));
                } else inventory.openBackups(p, op);
            }).limit(time, () -> {
                chat.send(p,"Backup.Time-Expired");
                bar.send(p,"Backup.Time-Expired-Bar");
                title.send(p,"Backup.Time-Expired-Title");
        }).build();
        return false;
    }

}
