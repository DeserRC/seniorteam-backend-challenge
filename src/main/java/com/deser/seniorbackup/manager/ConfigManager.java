package com.deser.seniorbackup.manager;

import com.deser.seniorbackup.SeniorBackup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Getter
public class ConfigManager {
    private final SeniorBackup main;
    private final FileConfiguration config;
    private final FileConfiguration message;
    private final SimpleDateFormat sdf;

    public ConfigManager(final SeniorBackup main) {
        this.main = main;
        this.config = new YamlConfiguration();
        this.message = new YamlConfiguration();
        this.sdf = new SimpleDateFormat("MM-dd HH:mm");
        load();
    }

    public <T> T getConfig(final String path) {
        return get(path, config);
    }

    public <T> T getMessage(final String path) {
        return get(path, message);
    }

    public <T> T get(final String path, final FileConfiguration config) {
        final T result = (T) config.get(path, ChatColor.DARK_RED + "Ocorreu um erro ao carregar a mensagem: " + ChatColor.YELLOW + path);
        if (result instanceof String) {
            return (T) result.toString().replace("&", "\u00A7");
        }
        return result;
    }

    public List<String> getListConfig(final String path) {
        return getList(path, config);
    }

    public List<String> getListMessage(final String path) {
        return getList(path, message);
    }

    public List<String> getList(final String path, final FileConfiguration config) {
        final List<String> list = config.getStringList(path);
        return list.stream().map(line -> line.replace("&", "\u00A7")).collect(toList());
    }

    public Set<String> getKeys(final String path, final FileConfiguration config) {
        return config.getConfigurationSection(path).getKeys(false);
    }

    public boolean contains(final String path, final FileConfiguration config) {
        return config.contains(path);
    }

    public String dateFormat(final long time) {
        Date date = new Date(time);
        return sdf.format(date);
    }

    @SneakyThrows
    public void load() {
        // Config.yml
        final File fileConfig = new File(main.getDataFolder(), "config.yml");
        if (!fileConfig.exists()) {
            main.saveResource("config.yml", false);
        }
        config.load(fileConfig);

        // Message.yml
        final File fileMessage = new File(main.getDataFolder(), "message.yml");
        if (!fileMessage.exists()) {
            main.saveResource("message.yml", false);
        }
        message.load(fileMessage);
    }
}