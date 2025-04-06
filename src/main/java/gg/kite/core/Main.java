package gg.kite.core;

import com.github.retrooper.packetevents.PacketEventsAPI;
import gg.kite.core.command.BalloonCommand;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.core.BalloonGUIManager;
import gg.kite.core.listeners.ListenerManager;
import gg.kite.core.util.Message;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {
    private final String pluginName = "Balloon";
    private final Logger LOGGER = getLogger();
    private static Main instance;
    private ConfigManager configManager;
    private BalloonGUIManager guiManager;
    private PacketEventsAPI packetEvents;

    @Override
    public void onLoad() {
        packetEvents = SpigotPacketEventsBuilder.build(this);
        packetEvents.load();
    }

    @Override
    public void onEnable() {
        instance = this;
        initializePacketEvents();
        initializeManagers();
        registerCommandsAndListeners();
        LOGGER.info(pluginName + " plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (packetEvents != null) {
            packetEvents.terminate();
        }
        LOGGER.info( pluginName + "plugin disabled!");
    }

    private void initializePacketEvents() {
        if (packetEvents == null) {
            packetEvents = SpigotPacketEventsBuilder.build(this);
        }
        packetEvents.init();
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        guiManager = new BalloonGUIManager(this);

        Message.loadMessages(this);
    }

    private void registerCommandsAndListeners() {
        getCommand("balloon").setExecutor(new BalloonCommand(this));
        new ListenerManager(this).registerListeners();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static Main getInstance() {
        return instance;
    }

    public BalloonGUIManager getGuiManager() {
        return guiManager;
    }
}