package gg.kite.core.command;

import gg.kite.core.Main;
import gg.kite.core.util.Message;
import lombok.Getter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class BalloonCommand implements TabExecutor {

    @Getter
    private final Main plugin;

    public BalloonCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().openGUI(player);
            return true;
        }

        String destination = args[0].toLowerCase();
        var destinationData = plugin.getConfigManager().getDestination(destination);

        if (destinationData == null) {
            String availableDestinations = plugin.getConfigManager().getDestinations().keySet().stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
            Message.send(player, "invalid-destination", "%destinations%", availableDestinations);
            return true;
        }

        Message.send(player, "destination-selected", "%destination%", destination);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getConfigManager().getDestinations().keySet().stream()
                    .filter(dest -> dest.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}