package gg.kite.core.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import gg.kite.core.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class Balloon {
    private final Player owner;
    private final ConfigManager.BalloonVariant variant;
    private Horse horse;
    private final int balloonId;
    private final int basketId;

    public Balloon(Player owner, ConfigManager.BalloonVariant variant) {
        this.owner = owner;
        this.variant = variant;
        this.balloonId = UUID.randomUUID().hashCode(); // Ensuring unique ID generation
        this.basketId = UUID.randomUUID().hashCode();
    }

    public void spawn(Location location) {
        horse = owner.getWorld().spawn(location, Horse.class, h -> {
            h.setCustomName(ChatColor.translateAlternateColorCodes('&', "Balloon"));
            h.setInvisible(true);
            h.setAI(false);
            h.setSilent(true);
        });

        WrapperPlayServerSpawnEntity balloonSpawn = new WrapperPlayServerSpawnEntity(
                balloonId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND,
                new Vector3d(location.getX(), location.getY() + 1, location.getZ()),
                0f, 0f, 0f, 0, Optional.of(new Vector3d(0, 0, 0))
        );
        WrapperPlayServerSpawnEntity basketSpawn = new WrapperPlayServerSpawnEntity(
                basketId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND,
                new Vector3d(location.getX(), location.getY() + 0.5, location.getZ()),
                0f, 0f, 0f, 0, Optional.of(new Vector3d(0, 0, 0))
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(owner, balloonSpawn);
        PacketEvents.getAPI().getPlayerManager().sendPacket(owner, basketSpawn);
    }

    public void destroy() {
        if (horse != null) {
            horse.remove();
            horse = null;
        }

        PacketEvents.getAPI().getPlayerManager()
                .sendPacket(owner, new WrapperPlayServerDestroyEntities(balloonId, basketId));
    }

    public ConfigManager.BalloonVariant getVariant() {
        return variant;
    }

    public Horse getHorse() {
        return horse;
    }

    public int getBalloonId() {
        return balloonId;
    }

    public int getBasketId() {
        return basketId;
    }
}