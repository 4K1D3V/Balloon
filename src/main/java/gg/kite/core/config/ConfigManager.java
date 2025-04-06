package gg.kite.core.config;

import com.google.common.collect.Maps;
import gg.kite.core.Main;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class ConfigManager {

    private final Main plugin;

    private Map<String, Destination> destinations = Maps.newHashMap();

    private Map<String, BalloonVariant> balloonVariants = Maps.newHashMap();

    private int cooldown;

    private double windStrength;

    private double windRainMultiplier;

    private double fuelWindThreshold;

    private int fuelWindExtra;

    private int fuelReserveMax;

    private int repairCostCoal;

    private int repairCostWool;

    private int crashCooldown;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        for (String key : config.getConfigurationSection("destinations").getKeys(false)) {
            double x = config.getDouble("destinations." + key + ".x");
            double y = config.getDouble("destinations." + key + ".y");
            double z = config.getDouble("destinations." + key + ".z");
            double c1x = config.getDouble("destinations." + key + ".control1.x");
            double c1y = config.getDouble("destinations." + key + ".control1.y");
            double c1z = config.getDouble("destinations." + key + ".control1.z");
            double c2x = config.getDouble("destinations." + key + ".control2.x");
            double c2y = config.getDouble("destinations." + key + ".control2.y");
            double c2z = config.getDouble("destinations." + key + ".control2.z");
            destinations.put(key.toLowerCase(), new Destination(new Location(null, x, y, z), new Location(null, c1x, c1y, c1z), new Location(null, c2x, c2y, c2z)));
        }

        for (String key : config.getConfigurationSection("balloons").getKeys(false)) {
            String color = config.getString("balloons." + key + ".color");
            double size = config.getDouble("balloons." + key + ".size");
            int capacity = config.getInt("balloons." + key + ".capacity");
            int speed = config.getInt("balloons." + key + ".speed");
            String basket = config.getString("balloons." + key + ".basket", "OAK_PLANKS");
            int fuel = config.getInt("balloons." + key + ".fuel", 1);
            int maxHealth = config.getInt("balloons." + key + ".max_health", 10);
            balloonVariants.put(key.toLowerCase(), new BalloonVariant(key, Material.valueOf(color), size, capacity, speed, Material.valueOf(basket), fuel, maxHealth));
        }

        cooldown = config.getInt("cooldown", 60);
        windStrength = config.getDouble("wind_strength", 1.0);
        windRainMultiplier = config.getDouble("wind_rain_multiplier", 2.0);
        fuelWindThreshold = config.getDouble("fuel_wind_threshold", 1.5);
        fuelWindExtra = config.getInt("fuel_wind_extra", 1);
        fuelReserveMax = config.getInt("fuel_reserve_max", 2);
        repairCostCoal = config.getInt("repair_cost.coal", 1);
        repairCostWool = config.getInt("repair_cost.wool", 2);
        crashCooldown = config.getInt("crash_cooldown", 300);
    }

    public Destination getDestination(String name) {
        return destinations.get(name.toLowerCase());
    }

    public BalloonVariant getBalloonVariant(String name) {
        return balloonVariants.get(name.toLowerCase());
    }

    public String getDestinationName(Location loc) {
        return destinations.entrySet().stream()
                .filter(e -> e.getValue().location.equals(loc))
                .map(Map.Entry::getKey)
                .findFirst().orElse("unknown");
    }

    @Getter
    public static class Destination {

        private final Location location;

        private final Location control1;

        private final Location control2;

        public Destination(Location location, Location control1, Location control2) {
            this.location = location;
            this.control1 = control1;
            this.control2 = control2;
        }

        public void setWorld(World world) {
            location.setWorld(world);
        }

        public Location getControl1() {
            return control1;
        }

        public Location getControl2() {
            return control2;
        }

        public Location getLocation() {
            return location;
        }
    }


    public static class BalloonVariant {
        private final String name;

        private final Material color;
        private final double size;
        private final int capacity;
        private final int speed;
        private final Material basket;
        private final int fuel;
        private final int maxHealth;

        public BalloonVariant(String name, Material color, double size, int capacity, int speed, Material basket, int fuel, int maxHealth) {
            this.name = name;
            this.color = color;
            this.size = size;
            this.capacity = capacity;
            this.speed = speed;
            this.basket = basket;
            this.fuel = fuel;
            this.maxHealth = maxHealth;
        }

        public String getName() {
            return name;
        }
        public Material getColor() {
            return color;
        }

        public double getSize() {
            return size;
        }

        public int getSpeed() {
            return speed;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getFuel() {
            return fuel;
        }
        public int getMaxHealth() {
            return maxHealth;
        }
    }

    public Map<String, Destination> getDestinations() {
        return destinations;
    }

    public Main getPlugin() {
        return plugin;
    }

    public Map<String, BalloonVariant> getBalloonVariants() {
        return balloonVariants;
    }

    public int getCooldown() {
        return cooldown;
    }

    public double getWindStrength() {
        return windStrength;
    }

    public double getWindRainMultiplier() {
        return windRainMultiplier;
    }

    public double getFuelWindThreshold() {
        return fuelWindThreshold;
    }

    public int getFuelWindExtra() {
        return fuelWindExtra;
    }

    public int getFuelReserveMax() {
        return fuelReserveMax;
    }

    public int getRepairCostCoal() {
        return repairCostCoal;
    }

    public int getRepairCostWool() {
        return repairCostWool;
    }

    public int getCrashCooldown() {
        return crashCooldown;
    }
}