package com.jellypudding.goodnight;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class GoodnightPlugin extends JavaPlugin {

    private final Set<Player> playersWhoSlept = new HashSet<>();
    private double sleepPercentage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getLogger().info("Goodnight plugin has been enabled. Sweet dreams!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodnight plugin has been disabled.");
    }

    private void loadConfigValues() {
        FileConfiguration config = this.getConfig();
        sleepPercentage = config.getDouble("sleep-percentage", 33.0) / 100;  // Convert to decimal
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("goodnight") && sender instanceof Player player) {
            World world = player.getWorld();
            long time = world.getTime();
            if (time < 12300 || time > 23850) {  // Check if it's daytime
                player.sendMessage(Component.text("You cannot say goodnight during the day!", NamedTextColor.RED));
                return true;
            }

            if (!playersWhoSlept.contains(player)) {
                playersWhoSlept.add(player);

                String customMessage = args.length > 0 ? String.join(" ", args) : null;
                Component message = createGoodnightMessage(player, customMessage);
                Bukkit.getServer().broadcast(message);

                checkIfEnoughPlayers();
            } else {
                player.sendMessage(Component.text("You have already said goodnight!", NamedTextColor.RED));
            }
            return true;
        }
        return false;
    }

    private Component createGoodnightMessage(Player player, String customMessage) {
        Component playerName = player.displayName();
        Component baseMessage = Component.text(" says goodnight", NamedTextColor.WHITE);

        if (customMessage != null) {
            return playerName.append(baseMessage).append(Component.text(" " + customMessage, NamedTextColor.WHITE));
        } else {
            Component infoMessage = getDefaultInfoMessage();
            return playerName.append(baseMessage).append(infoMessage);
        }
    }

    private @NotNull Component getDefaultInfoMessage() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        int sleepingPlayers = playersWhoSlept.size();
        int requiredPlayers = (int) Math.ceil(totalPlayers * sleepPercentage);
        int morePlayersNeeded = Math.max(0, requiredPlayers - sleepingPlayers);

        double currentPercentage = totalPlayers > 0 ? (double) sleepingPlayers / totalPlayers * 100 : 0;
        double requiredPercentage = sleepPercentage * 100;

        // Scale the percentages to be out of 100%
        double scaledCurrentPercentage = (currentPercentage / requiredPercentage) * 100;
        double scaledRequiredPercentage = 100.0;

        Component infoMessage;
        if (morePlayersNeeded > 0) {
            infoMessage = Component.text(String.format(" (%d more needed to make it day, %.1f%% / %.1f%%)",
                    morePlayersNeeded, scaledCurrentPercentage, scaledRequiredPercentage), NamedTextColor.GRAY);
        } else {
            infoMessage = Component.text(" (Making it ")
                    .append(Component.text("day", NamedTextColor.GREEN))
                    .append(Component.text(")!"));
        }
        return infoMessage;
    }

    private void checkIfEnoughPlayers() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        int sleepingPlayers = playersWhoSlept.size();

        if (totalPlayers > 0 && (double) sleepingPlayers / totalPlayers >= sleepPercentage) {
            //Component dayMessage = Component.text("Enough players have slept! Changing to day time...", NamedTextColor.GREEN);
            //Bukkit.getServer().broadcast(dayMessage);

            Bukkit.getWorlds().forEach(world -> world.setTime(0));  // Set the time to day (0 ticks) for all worlds
            playersWhoSlept.clear();  // Reset the players who have slept
        }
    }
}