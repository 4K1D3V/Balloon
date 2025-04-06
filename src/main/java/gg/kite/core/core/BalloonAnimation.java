package gg.kite.core.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import gg.kite.core.Main;
import gg.kite.core.api.ParticleManager;
import gg.kite.core.api.SoundManager;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.listeners.ListenerManager;
import gg.kite.core.util.LocationUtil;
import gg.kite.core.util.Message;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BalloonAnimation extends BukkitRunnable {

    private final Player initiator;
    private final Balloon balloon;
    private final Location destination;
    private final List<Player> passengers;
    private final Random random = new Random();
    private final boolean isRaining;
    private final ListenerManager listenerManager;
    private int ticks = 0;
    private boolean turbulenceWarningSent = false;

    public BalloonAnimation(@NotNull Player initiator, Balloon balloon, Location destination, List<Player> passengers, ListenerManager listenerManager) {
        this.initiator = initiator;
        this.balloon = balloon;
        this.destination = destination;
        this.passengers = passengers;
        this.isRaining = initiator.getWorld().hasStorm() && initiator.getWorld().isThundering();
        this.listenerManager = listenerManager;
    }

    public void start() {
        SoundManager.playLaunchSound(initiator, initiator.getLocation());
        passengers.forEach(p -> SoundManager.playLaunchSound(p, p.getLocation()));
        runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    @Override
    public void run() {
        int maxTicks = balloon.getVariant().getSpeed() * 20;
        if (ticks < maxTicks) {
            Location start = initiator.getLocation().add(0, 1, 0);
            ConfigManager.Destination destConfig = Main.getInstance().getConfigManager().getDestination(Main.getInstance().getConfigManager().getDestinationName(destination));
            Location control1 = destConfig.getControl1().clone();
            Location control2 = destConfig.getControl2().clone();
            control1.setWorld(start.getWorld());
            control2.setWorld(start.getWorld());
            Location end = destination.clone();

            double windScale = Main.getInstance().getConfigManager().getWindStrength() * (isRaining ? Main.getInstance().getConfigManager().getWindRainMultiplier() : 1.0);
            double windX = random.nextGaussian() * 0.5 * windScale;
            double windY = random.nextGaussian() * 0.2 * windScale;
            double windZ = random.nextGaussian() * 0.5 * windScale;
            control1.add(windX, windY, windZ);
            control2.add(windX * 0.5, windY * 0.5, windZ * 0.5);

            double t = (double) ticks / maxTicks;
            Location newLoc;

            if (t >= 0.8) {
                double landingProgress = (t - 0.8) / 0.2;
                Location bezierLoc = LocationUtil.cubicBezier(start, control1, control2, end, 0.8 + (0.2 * landingProgress));
                newLoc = LocationUtil.interpolate(bezierLoc, end, Math.sin(landingProgress * Math.PI / 2));
            } else {
                newLoc = LocationUtil.cubicBezier(start, control1, control2, end, t);
            }

            boolean willTurbulence = windScale > 1.5 && random.nextDouble() < 0.1;
            if (willTurbulence && !turbulenceWarningSent) {
                Message.send(initiator, "turbulence-warning");
                passengers.forEach(p -> Message.send(p, "turbulence-warning"));
                turbulenceWarningSent = true;
            } else if (willTurbulence && turbulenceWarningSent) {
                newLoc.add(random.nextGaussian() * 0.3, random.nextGaussian() * 0.2, random.nextGaussian() * 0.3);
                SoundManager.playGaspSound(initiator, newLoc);
                passengers.forEach(p -> SoundManager.playGaspSound(p, newLoc));
                listenerManager.damageBalloon(initiator, 1);
                turbulenceWarningSent = false;
                if (listenerManager.getBalloonHealth(initiator) <= 0) {
                    abortTrip(newLoc);
                    return;
                }
            } else {
                turbulenceWarningSent = false;
            }

            balloon.getHorse().teleport(newLoc);
            updateBalloonVisual(newLoc.clone().add(0, 1, 0), windX, windZ);
            updateBasketVisual(newLoc.clone().add(0, 0.5, 0), windX, windZ);

            double bobOffset = Math.sin(ticks * 0.2) * 0.1;
            initiator.teleport(newLoc.clone().add(0, bobOffset, 0));
            passengers.forEach(p -> p.teleport(newLoc.clone().add(0, bobOffset, 0)));

            if (ticks % 40 == 0 && ticks > maxTicks / 2) {
                ItemStack coal = new ItemStack(Material.COAL, 1);
                if (!initiator.getInventory().containsAtLeast(coal, 1)) {
                    int reserve = listenerManager.getFuelReserve(initiator);
                    if (reserve > 0) {
                        listenerManager.consumeFuelReserve(initiator, 1);
                        initiator.sendMessage(ChatColor.YELLOW + "Used 1 coal from reserve. Remaining: " + (reserve - 1));
                    } else {
                        Message.send(initiator, "low-fuel");
                        if (random.nextDouble() < 0.05) {
                            abortTrip(newLoc);
                            return;
                        }
                    }
                }
            }

            if (ticks % 20 == 0) {
                int secondsLeft = (maxTicks - ticks) / 20;
                String hud = ChatColor.GREEN + "Health: " + listenerManager.getBalloonHealth(initiator) +
                        " | Fuel: " + listenerManager.getFuelReserve(initiator) +
                        " | Time: " + secondsLeft + "s";
                initiator.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(hud));
                passengers.forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(hud)));
            }

            ParticleManager.spawnFlame(initiator, newLoc.clone().add(0, -0.5, 0));
            passengers.forEach(p -> ParticleManager.spawnFlame(p, newLoc.clone().add(0, -0.5, 0)));
            if (ticks % 5 == 0) {
                ParticleManager.spawnCloud(initiator, newLoc);
                passengers.forEach(p -> ParticleManager.spawnCloud(p, newLoc));
                Location windLoc = newLoc.clone().add(Math.sin(ticks * 0.2) * 2 + windX, 0, Math.cos(ticks * 0.2) * 2 + windZ);
                ParticleManager.spawnSmoke(initiator, windLoc);
                passengers.forEach(p -> ParticleManager.spawnSmoke(p, windLoc));
                SoundManager.playWindSound(initiator, newLoc);
                passengers.forEach(p -> SoundManager.playWindSound(p, newLoc));
            }

            if (ticks % 20 == 0) {
                int secondsLeft = (maxTicks - ticks) / 20;
                Message.send(initiator, "launching", "%seconds%", String.valueOf(secondsLeft));
                passengers.forEach(p -> Message.send(p, "launching", "%seconds%", String.valueOf(secondsLeft)));
            }
            ticks++;
        } else {
            initiator.teleport(destination);
            passengers.forEach(p -> p.teleport(destination));
            balloon.destroy();
            listenerManager.clearFuelReserve(initiator);
            String destName = Main.getInstance().getConfigManager().getDestinationName(destination);
            Message.send(initiator, "arrived", "%destination%", destName);
            passengers.forEach(p -> Message.send(p, "arrived", "%destination%", destName));
            SoundManager.playLandSound(initiator, destination);
            passengers.forEach(p -> SoundManager.playLandSound(p, destination));
            cancel();
        }
    }

    private void updateBalloonVisual(@NotNull Location newLoc, double windX, double windZ) {
        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                balloon.getBalloonId(), new Vector3d(newLoc.getX(), newLoc.getY(), newLoc.getZ()), 0f, 0f, false
        );
        float tiltX = (float) (windZ * 5);
        float tiltZ = (float) (-windX * 5);
        EntityData rotationData = new EntityData(11, EntityDataTypes.QUATERNION, new float[]{tiltX, 0, tiltZ, 1});
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(balloon.getBalloonId(), Arrays.asList(rotationData));

        PacketEvents.getAPI().getPlayerManager().sendPacket(initiator, teleportPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(initiator, metadataPacket);
        passengers.forEach(p -> {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, teleportPacket);
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, metadataPacket);
        });
    }

    private void updateBasketVisual(@NotNull Location newLoc, double windX, double windZ) {
        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                balloon.getBasketId(), new Vector3d(newLoc.getX(), newLoc.getY(), newLoc.getZ()), 0f, 0f, false
        );
        float tiltX = (float) (windZ * 5);
        float tiltZ = (float) (-windX * 5);
        EntityData rotationData = new EntityData(11, EntityDataTypes.QUATERNION, new float[]{tiltX, 0, tiltZ, 1});
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(balloon.getBasketId(), Arrays.asList(rotationData));

        PacketEvents.getAPI().getPlayerManager().sendPacket(initiator, teleportPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(initiator, metadataPacket);
        passengers.forEach(p -> {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, teleportPacket);
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, metadataPacket);
        });
    }

    private void abortTrip(@NotNull Location crashLoc) {
        balloon.destroy();
        listenerManager.clearFuelReserve(initiator);
        listenerManager.markCrashed(initiator);
        initiator.sendMessage(ChatColor.RED + "Balloon crashed due to insufficient fuel or damage!");
        passengers.forEach(p -> p.sendMessage(ChatColor.RED + "Balloon crashed due to insufficient fuel or damage!"));

        Particle explosionParticle = new Particle(ParticleTypes.EXPLOSION);
        WrapperPlayServerParticle explosionPacket = new WrapperPlayServerParticle(
                explosionParticle,
                true,
                new Vector3d((float) crashLoc.getX(), (float) crashLoc.getY(), (float) crashLoc.getZ()),
                new Vector3f(0f, 0f, 0f),
                0f,
                1
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(initiator, explosionPacket);
        passengers.forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, explosionPacket));
        initiator.playSound(crashLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        passengers.forEach(p -> p.playSound(crashLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f));

        ejectPlayer(initiator, crashLoc);
        passengers.forEach(p -> ejectPlayer(p, crashLoc));

        cancel();
    }

    private void ejectPlayer(@NotNull Player player, @NotNull Location startLoc) {
        player.teleport(startLoc.clone().add(0, 2, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false));
        new BukkitRunnable() {
            int fallTicks = 0;
            @Override
            public void run() {
                if (fallTicks >= 100 || player.isOnGround()) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                Particle cloudParticle = new Particle(ParticleTypes.CLOUD);
                WrapperPlayServerParticle parachutePacket = new WrapperPlayServerParticle(
                        cloudParticle,
                        true,
                        new Vector3d((float) loc.getX(), (float) loc.getY() + 2, (float) loc.getZ()),
                        new Vector3f(0.5f, 0f, 0.5f),
                        0.05f,
                        5
                );
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, parachutePacket);
                passengers.forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, parachutePacket));
                fallTicks++;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }
}