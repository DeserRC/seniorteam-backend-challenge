package com.deser.seniorbackup;

import com.deser.seniorbackup.command.impl.BackupCommand;
import com.deser.seniorbackup.factory.BackupFactory;
import com.deser.seniorbackup.factory.ConnectionFactory;
import com.deser.seniorbackup.factory.MessageFactory;
import com.deser.seniorbackup.factory.TaskFactory;
import com.deser.seniorbackup.listener.InventoryListener;
import com.deser.seniorbackup.manager.ConfigManager;
import com.deser.seniorbackup.manager.InventoryManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bukkit.event.HandlerList.unregisterAll;

@Getter
public class SeniorBackup extends JavaPlugin {
    @Getter
    private static SeniorBackup instance;

    private ExecutorService executor;
    private ScheduledExecutorService scheduled;
    private ConfigManager configManager;
    private InventoryManager inventoryManager;

    private MessageFactory messageFactory;
    private ConnectionFactory connectionFactory;
    private BackupFactory backupFactory;
    private TaskFactory taskFactory;

    @Override
    public void onEnable() {
        instance = this;

        executor = newCachedThreadPool();
        scheduled = newSingleThreadScheduledExecutor();

        configManager = new ConfigManager(this);

        messageFactory = new MessageFactory(this);
        connectionFactory = new ConnectionFactory(this);
        backupFactory = new BackupFactory(this);
        taskFactory = new TaskFactory(this);

        inventoryManager = new InventoryManager(this);

        loadCommands();
        loadListeners();
        loadTasks();
    }

    @Override
    public void onDisable() {
        executor.shutdown();
        scheduled.shutdown();
        connectionFactory.getDataBase().closeConnection();
        unregisterAll();
    }

    private void loadCommands() {
        new BackupCommand(this);
    }

    private void loadListeners() {
        new InventoryListener(this);
    }

    private void loadTasks() {
        final int create = configManager.getConfig("Tasks.Create");
        scheduled.execute(taskFactory.getBackupDeleteTask());
        scheduled.scheduleAtFixedRate(taskFactory.getBackupCreateTask(), 10, 10, SECONDS);
    }
}
