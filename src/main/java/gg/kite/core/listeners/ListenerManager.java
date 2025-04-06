package gg.kite.core.listeners;

import gg.kite.core.Main;
import gg.kite.core.core.Balloon;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ListenerManager implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> fuelReserves = new HashMap<>();
    private final Map<UUID, Long> crashCooldowns = new HashMap<>();
    private final Map<UUID, Integer> balloonHealth = new HashMap<>();

    public ListenerManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getGuiManager().handleClick(event);
    }

    @EventHandler
    public void onEntityInteract(@NotNull PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.HORSE && event.getRightClicked().getCustomName() != null) {
            Player player = event.getPlayer();
            if (isOnCooldown(player)) {
                Message.send(player, "cooldown", "%seconds%", String.valueOf(getCooldownRemaining(player)));
                return;
            }
            if (isOnCrashCooldown(player)) {
                player.sendMessage(ChatColor.RED + "Balloon is on crash cooldown! Wait " + getCrashCooldownRemaining(player) + " seconds.");
                return;
            }
            if (getBalloonHealth(player) <= 0) {
                ConfigManager.BalloonVariant variant = plugin.getConfigManager().getBalloonVariant("red_small"); // Default variant
                if (repairBalloon(player, variant)) {
                    balloonHealth.put(player.getUniqueId(), variant.getMaxHealth());
                    player.sendMessage(ChatColor.GREEN + "Balloon repaired!");
                } else {
                    int damage = variant.getMaxHealth() - getBalloonHealth(player);
                    int coalCost = (int) Math.ceil(plugin.getConfigManager().getRepairCostCoal() * (damage / (double) variant.getMaxHealth()));
                    int woolCost = (int) Math.ceil(plugin.getConfigManager().getRepairCostWool() * (damage / (double) variant.getMaxHealth()));
                    player.sendMessage(ChatColor.RED + "You need " + coalCost + " coal and " + woolCost + " wool to repair the balloon!");
                    return;
                }
            }
            plugin.getGuiManager().openGUI(player);
            event.setCancelled(true);
        }
    }

    public boolean canUseBalloon(Player player, Balloon balloon) {
        if (isOnCooldown(player)) {
            Message.send(player, "cooldown", "%seconds%", String.valueOf(getCooldownRemaining(player)));
            return false;
        }
        if (isOnCrashCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Balloon is on crash cooldown! Wait " + getCrashCooldownRemaining(player) + " seconds.");
            return false;
        }
        int health = getBalloonHealth(player);
        if (health <= 0) {
            player.sendMessage(ChatColor.RED + "Balloon needs repairs before it can fly!");
            return false;
        }
        double windScale = plugin.getConfigManager().getWindStrength() * (player.getWorld().hasStorm() && player.getWorld().isThundering() ? plugin.getConfigManager().getWindRainMultiplier() : 1.0);
        int baseFuel = balloon.getVariant().getFuel() + (windScale > plugin.getConfigManager().getFuelWindThreshold() ? plugin.getConfigManager().getFuelWindExtra() : 0);
        ItemStack coal = new ItemStack(Material.COAL, baseFuel);
        if (!player.getInventory().containsAtLeast(coal, coal.getAmount())) {
            Message.send(player, "no-fuel");
            return false;
        }

        player.getInventory().removeItem(coal);
        int reserveFuel = Math.min(plugin.getConfigManager().getFuelReserveMax(), player.getInventory().all(Material.COAL).values().stream().mapToInt(ItemStack::getAmount).sum());
        if (reserveFuel > 0) {
            ItemStack reserveCoal = new ItemStack(Material.COAL, reserveFuel);
            player.getInventory().removeItem(reserveCoal);
            fuelReserves.put(player.getUniqueId(), reserveFuel);
        }
        balloonHealth.put(player.getUniqueId(), balloon.getVariant().getMaxHealth());
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + plugin.getConfigManager().getCooldown() * 1000);
        return true;
    }

    public int getFuelReserve(@NotNull Player player) {
        return fuelReserves.getOrDefault(player.getUniqueId(), 0);
    }

    public void consumeFuelReserve(@NotNull Player player, int amount) {
        int current = fuelReserves.getOrDefault(player.getUniqueId(), 0);
        fuelReserves.put(player.getUniqueId(), Math.max(0, current - amount));
    }

    public void clearFuelReserve(@NotNull Player player) {
        fuelReserves.remove(player.getUniqueId());
    }

    public void markCrashed(@NotNull Player player) {
        crashCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + plugin.getConfigManager().getCrashCooldown() * 1000);
        balloonHealth.put(player.getUniqueId(), 0);
    }

    public boolean isOnCrashCooldown(@NotNull Player player) {
        Long endTime = crashCooldowns.get(player.getUniqueId());
        return endTime != null && System.currentTimeMillis() < endTime;
    }

    public int getCrashCooldownRemaining(@NotNull Player player) {
        Long endTime = crashCooldowns.get(player.getUniqueId());
        return endTime != null ? (int) ((endTime - System.currentTimeMillis()) / 1000) : 0;
    }

    public int getBalloonHealth(@NotNull Player player) {
        return balloonHealth.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getBalloonVariant("red_small").getMaxHealth());
    }

    public void damageBalloon(Player player, int amount) {
        int current = getBalloonHealth(player);
        balloonHealth.put(player.getUniqueId(), Math.max(0, current - amount));
    }

    public boolean repairBalloon(Player player, ConfigManager.@NotNull BalloonVariant variant) {
        int damage = variant.getMaxHealth() - getBalloonHealth(player);
        if (damage <= 0) return true;
        int coalCost = (int) Math.ceil(plugin.getConfigManager().getRepairCostCoal() * (damage / (double) variant.getMaxHealth()));
        int woolCost = (int) Math.ceil(plugin.getConfigManager().getRepairCostWool() * (damage / (double) variant.getMaxHealth()));
        ItemStack coal = new ItemStack(Material.COAL, coalCost);
        ItemStack wool = new ItemStack(Material.WHITE_WOOL, woolCost);
        if (player.getInventory().containsAtLeast(coal, coalCost) && player.getInventory().containsAtLeast(wool, woolCost)) {
            player.getInventory().removeItem(coal);
            player.getInventory().removeItem(wool);
            return true;
        }
        return false;
    }

    private boolean isOnCooldown(@NotNull Player player) {
        Long endTime = cooldowns.get(player.getUniqueId());
        return endTime != null && System.currentTimeMillis() < endTime;
    }

    private int getCooldownRemaining(@NotNull Player player) {
        Long endTime = cooldowns.get(player.getUniqueId());
        return endTime != null ? (int) ((endTime - System.currentTimeMillis()) / 1000) : 0;
    }
}