package gg.kite.core.core;

import gg.kite.core.Main;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.config.ConfigManager.BalloonVariant;
import gg.kite.core.listeners.ListenerManager;
import gg.kite.core.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BalloonGUIManager {

    private final Main plugin;
    private final ListenerManager listenerManager;
    private final Map<UUID, BalloonGUIState> guiStates = new HashMap<>();

    public BalloonGUIManager(Main plugin) {
        this.plugin = plugin;
        this.listenerManager = new ListenerManager(plugin);
    }

    public void openGUI(@NotNull Player player) {
        BalloonGUIState state = guiStates.computeIfAbsent(player.getUniqueId(), k -> new BalloonGUIState(player));
        Inventory inventory = Bukkit.createInventory(null, 36, ChatColor.DARK_GRAY + "Balloon Travel Setup");
        populateInventory(inventory, state);
        player.openInventory(inventory);
    }

    private void populateInventory(Inventory inventory, @NotNull BalloonGUIState state) {
        Player player = state.getPlayer();

        int slot = 0;
        for (BalloonVariant variant : plugin.getConfigManager().getBalloonVariants().values()) {
            ItemStack item = new ItemStack(variant.getColor());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + variant.getName()));
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Size: " + variant.getSize(),
                    ChatColor.GRAY + "Capacity: " + variant.getCapacity(),
                    ChatColor.GRAY + "Speed: " + variant.getSpeed() + "s",
                    ChatColor.GRAY + "Fuel: " + variant.getFuel() + " coal",
                    ChatColor.GRAY + "Max Health: " + variant.getMaxHealth(),
                    ChatColor.YELLOW + "Click to select!"
            ));
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }

        slot = 9;
        for (String dest : plugin.getConfigManager().getDestinations().keySet()) {
            ItemStack item = new ItemStack(Material.COMPASS);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + dest));
            meta.setLore(Arrays.asList(ChatColor.YELLOW + "Click to select!"));
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }

        slot = 18;
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby != player && nearby.getLocation().distance(player.getLocation()) <= 10 && slot < 27) {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + nearby.getName()));
                meta.setLore(Arrays.asList(
                        state.getSelectedPassengers().contains(nearby) ? ChatColor.GREEN + "Selected" : ChatColor.YELLOW + "Click to add/remove passenger!"
                ));
                item.setItemMeta(meta);
                inventory.setItem(slot++, item);
            }
        }

        ItemStack windItem = new ItemStack(Material.ARROW);
        ItemMeta windMeta = windItem.getItemMeta();
        windMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Wind Conditions"));
        double windScale = plugin.getConfigManager().getWindStrength() * (player.getWorld().hasStorm() && player.getWorld().isThundering() ? plugin.getConfigManager().getWindRainMultiplier() : 1.0);
        Random random = new Random();
        double windX = random.nextGaussian() * 0.5 * windScale;
        double windZ = random.nextGaussian() * 0.5 * windScale;
        String direction = getWindDirection(windX, windZ);
        windMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Strength: " + String.format("%.1f", windScale),
                ChatColor.GRAY + "Direction: " + direction
        ));
        windItem.setItemMeta(windMeta);
        inventory.setItem(34, windItem);

        ItemStack fuelItem = new ItemStack(Material.COAL);
        ItemMeta fuelMeta = fuelItem.getItemMeta();
        fuelMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Fuel Reserve"));
        int reserve = listenerManager.getFuelReserve(player);
        fuelMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Reserve: " + reserve + "/" + plugin.getConfigManager().getFuelReserveMax(),
                ChatColor.GRAY + "Inventory: " + player.getInventory().all(Material.COAL).values().stream().mapToInt(ItemStack::getAmount).sum()
        ));
        fuelItem.setItemMeta(fuelMeta);
        inventory.setItem(33, fuelItem);

        ItemStack checklistItem = new ItemStack(Material.PAPER);
        ItemMeta checklistMeta = checklistItem.getItemMeta();
        checklistMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Pre-Flight Checklist"));
        List<String> checklist = new ArrayList<>();
        checklist.add(listenerManager.isOnCrashCooldown(player) ? ChatColor.RED + "✗ Crash Cooldown: " + listenerManager.getCrashCooldownRemaining(player) + "s" : ChatColor.GREEN + "✓ No Crash Cooldown");
        int health = listenerManager.getBalloonHealth(player);
        checklist.add(health > 0 ? ChatColor.GREEN + "✓ Health: " + health : ChatColor.RED + "✗ Health: " + health + " (Repair needed)");
        int totalFuel = reserve + player.getInventory().all(Material.COAL).values().stream().mapToInt(ItemStack::getAmount).sum();
        int minFuel = state.getSelectedVariant() != null ? state.getSelectedVariant().getFuel() : 1;
        checklist.add(totalFuel >= minFuel ? ChatColor.GREEN + "✓ Fuel: " + totalFuel : ChatColor.RED + "✗ Fuel: " + totalFuel + " (Need " + minFuel + ")");
        checklistMeta.setLore(checklist);
        checklistItem.setItemMeta(checklistMeta);
        inventory.setItem(32, checklistItem);

        ItemStack confirm = new ItemStack(Material.EMERALD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm Travel");
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(35, confirm);
    }

    private String getWindDirection(double windX, double windZ) {
        double angle = Math.toDegrees(Math.atan2(windZ, windX));
        if (angle < 0) angle += 360;
        if (angle >= 337.5 || angle < 22.5) return "East";
        if (angle >= 22.5 && angle < 67.5) return "Northeast";
        if (angle >= 67.5 && angle < 112.5) return "North";
        if (angle >= 112.5 && angle < 157.5) return "Northwest";
        if (angle >= 157.5 && angle < 202.5) return "West";
        if (angle >= 202.5 && angle < 247.5) return "Southwest";
        if (angle >= 247.5 && angle < 292.5) return "South";
        if (angle >= 292.5 && angle < 337.5) return "Southeast";
        return "Unknown";
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public Map<UUID, BalloonGUIState> getGuiStates() {
        return guiStates;
    }

    public Main getPlugin() {
        return plugin;
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !event.getView().getTitle().contains("Balloon Travel Setup")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        BalloonGUIState state = guiStates.get(player.getUniqueId());
        if (state == null) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        if (event.getSlot() < 9) { // Balloon
            state.setSelectedVariant(plugin.getConfigManager().getBalloonVariant(name));
            player.sendMessage(ChatColor.GREEN + "Selected balloon: " + name);
            updateInventory(player, state);
        } else if (event.getSlot() < 18) { // Destination
            state.setSelectedDestination(name);
            player.sendMessage(ChatColor.GREEN + "Selected destination: " + name);
            updateInventory(player, state);
        } else if (event.getSlot() < 27) { // Passenger
            Player target = Bukkit.getPlayerExact(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
            if (target != null) {
                if (state.getSelectedPassengers().contains(target)) {
                    state.getSelectedPassengers().remove(target);
                    player.sendMessage(ChatColor.YELLOW + "Removed passenger: " + target.getName());
                } else if (state.getSelectedVariant() != null && state.getSelectedPassengers().size() < state.getSelectedVariant().getCapacity() - 1) {
                    state.getSelectedPassengers().add(target);
                    player.sendMessage(ChatColor.YELLOW + "Added passenger: " + target.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "Capacity limit reached!");
                }
                updateInventory(player, state);
            }
        } else if (event.getSlot() == 35) { // Confirm
            if (state.getSelectedVariant() == null || state.getSelectedDestination() == null) {
                Message.send(player, "select-balloon-first");
                return;
            }
            if (listenerManager.isOnCrashCooldown(player) || listenerManager.getBalloonHealth(player) <= 0) {
                player.sendMessage(ChatColor.RED + "Cannot launch until checklist is cleared!");
                return;
            }
            player.closeInventory();
            Balloon balloon = new Balloon(player, state.getSelectedVariant());
            if (listenerManager.canUseBalloon(player, balloon)) {
                balloon.spawn(player.getLocation().add(0, 1, 0));
                ConfigManager.Destination dest = plugin.getConfigManager().getDestination(state.getSelectedDestination());
                dest.setWorld(player.getWorld());
                new BalloonAnimation(player, balloon, (Location) dest.getLocation(), state.getSelectedPassengers(), listenerManager).start();
            }
            guiStates.remove(player.getUniqueId());
        }
    }

    private void updateInventory(@NotNull Player player, BalloonGUIState state) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        inventory.clear();
        populateInventory(inventory, state);
    }

    private static class BalloonGUIState {
        private final Player player;
        private BalloonVariant selectedVariant;
        private String selectedDestination;
        private final List<Player> selectedPassengers = new ArrayList<>();

        public BalloonGUIState(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }
        public void setSelectedVariant(BalloonVariant variant) { this.selectedVariant = variant; }
        public BalloonVariant getSelectedVariant() { return selectedVariant; }
        public void setSelectedDestination(String destination) { this.selectedDestination = destination; }
        public String getSelectedDestination() { return selectedDestination; }
        public List<Player> getSelectedPassengers() { return selectedPassengers; }
    }
}