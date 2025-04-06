package gg.kite.core.api;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ParticleManager {

    public static void spawnFlame(Player player, Location location) {
        Particle particle = new Particle(ParticleTypes.FLAME);
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                particle,
                true,
                new Vector3d(location.getX(), location.getY(), location.getZ()),
                new Vector3f(0.1f, 0.1f, 0.1f),
                0f,
                1
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void spawnCloud(Player player, Location location) {
        Particle particle = new Particle(ParticleTypes.CLOUD);
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                particle,
                true,
                new Vector3d(location.getX(), location.getY(), location.getZ()),
                new Vector3f(0.5f, 0.5f, 0.5f),
                0f,
                1
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void spawnSmoke(Player player, Location location) {
        Particle particle = new Particle(ParticleTypes.SMOKE);
        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                particle,
                true,
                new Vector3d(location.getX(), location.getY(), location.getZ()),
                new Vector3f(0.2f, 0.2f, 0.2f),
                0f,
                1
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}