package gg.kite.core.api;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    public static void playLaunchSound(Player player, Location location) {
        player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
    }

    public static void playLandSound(Player player, Location location) {
        player.playSound(location, Sound.ENTITY_HORSE_LAND, 1.0f, 1.0f);
    }

    public static void playWindSound(Player player, Location location) {
        player.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.5f);
    }

    public static void playGaspSound(Player player, Location location) {
        player.playSound(location, Sound.ENTITY_PLAYER_HURT, 0.7f, 1.2f);
    }
}