package gg.kite.core.util;

import gg.kite.core.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Message {

    private static FileConfiguration messages;
    private static String prefix;

    public static void loadMessages(@NotNull Main plugin) {
        File file = new File(plugin.getDataFolder(), "message.yml");
        if (!file.exists()) {
            plugin.saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", ""));
    }

    public static void send(Player player, String key, String @NotNull ... placeholders) {
        String message = messages.getString(key, "Message not found: " + key);
        message = ChatColor.translateAlternateColorCodes('&', prefix + message);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        player.sendMessage(message);
    }
}