package gg.kite.core.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationUtil {

    public static @NotNull Location cubicBezier(@NotNull Location p0, @NotNull Location p1, @NotNull Location p2, @NotNull Location p3, double t) {
        double x = Math.pow(1 - t, 3) * p0.getX() + 3 * Math.pow(1 - t, 2) * t * p1.getX() + 3 * (1 - t) * Math.pow(t, 2) * p2.getX() + Math.pow(t, 3) * p3.getX();
        double y = Math.pow(1 - t, 3) * p0.getY() + 3 * Math.pow(1 - t, 2) * t * p1.getY() + 3 * (1 - t) * Math.pow(t, 2) * p2.getY() + Math.pow(t, 3) * p3.getY();
        double z = Math.pow(1 - t, 3) * p0.getZ() + 3 * Math.pow(1 - t, 2) * t * p1.getZ() + 3 * (1 - t) * Math.pow(t, 2) * p2.getZ() + Math.pow(t, 3) * p3.getZ();
        return new Location(p0.getWorld(), x, y, z);
    }

    public static @NotNull Location interpolate(@NotNull Location start, @NotNull Location end, double t) {
        double x = start.getX() + (end.getX() - start.getX()) * t;
        double y = start.getY() + (end.getY() - start.getY()) * t;
        double z = start.getZ() + (end.getZ() - start.getZ()) * t;
        return new Location(start.getWorld(), x, y, z);
    }
}