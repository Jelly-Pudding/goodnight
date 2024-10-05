package com.jellypudding.goodnight;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class GoodnightPlugin extends JavaPlugin implements Listener {

    private final Set<Player> playersWhoSlept = new HashSet<>();
    private double sleepPercentage;
    private static final long MORNING_TIME = 1000; // 6:00 AM in Minecraft time

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Goodnight plugin has been enabled. Sweet dreams!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodnight plugin has been disabled.");
    }

    private void loadConfigValues() {
        FileConfiguration config = this.getConfig();
        sleepPercentage = config.getDouble("sleep-percentage", 66.6) / 100;  // Convert to decimal
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("goodnight") && sender instanceof Player player) {
            World overworld = getOverworld();
            if (overworld == null) {
                player.sendMessage(Component.text("Cannot find the overworld!", NamedTextColor.RED));
                return true;
            }

            long time = overworld.getTime();
            long timeInDay = time % 24000;
            if (timeInDay > 500 && timeInDay < 11000) {  // Check if it's daytime in the overworld
                player.sendMessage(Component.text("You cannot say goodnight during the day!", NamedTextColor.RED));
                return true;
            }

            if (!playersWhoSlept.contains(player)) {
                playersWhoSlept.add(player);

                String customMessage = args.length > 0 ? String.join(" ", args) : null;
                int[] sleepData = calculateSleepData();
                Component message = createGoodnightMessage(player, customMessage, sleepData);
                Bukkit.getServer().broadcast(message);

                updateTimeIfNeeded(overworld, sleepData[0], sleepData[1], false);
            } else {
                player.sendMessage(Component.text("You have already said goodnight!", NamedTextColor.RED));
            }
            return true;
        }
        return false;
    }

    private Component createGoodnightMessage(Player player, String customMessage, int[] sleepData) {
        Component playerName = player.displayName();
        Component baseMessage = Component.text(" says goodnight", NamedTextColor.WHITE);

        if (customMessage != null) {
            return playerName.append(baseMessage).append(Component.text(" " + customMessage, NamedTextColor.WHITE));
        } else {
            Component infoMessage = getDefaultInfoMessage(sleepData[0], sleepData[1], sleepData[2]);
            return playerName.append(baseMessage).append(infoMessage);
        }
    }

    private int[] calculateSleepData() {
        Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        int totalPlayers = onlinePlayers.size();
        int sleepingPlayers = (int) playersWhoSlept.stream()
                .filter(onlinePlayers::contains)
                .count();
        int requiredPlayers = (int) Math.ceil(totalPlayers * sleepPercentage);
        return new int[]{totalPlayers, sleepingPlayers, requiredPlayers};
    }

    private @NotNull Component getDefaultInfoMessage(int totalPlayers, int sleepingPlayers, int requiredPlayers) {
        int morePlayersNeeded = Math.max(0, requiredPlayers - sleepingPlayers);
        if (morePlayersNeeded > 0) {
            double currentPercentage = totalPlayers > 0 ? (double) sleepingPlayers / totalPlayers * 100 : 0;
            double requiredPercentage = sleepPercentage * 100;
            return Component.text(String.format(" (%d more needed to make it day, %.1f%% / %.1f%%)",
                    morePlayersNeeded, currentPercentage, requiredPercentage), NamedTextColor.GRAY);
        } else {
            return Component.text(" (Making it ", NamedTextColor.WHITE)
                    .append(Component.text("day", NamedTextColor.GREEN))
                    .append(Component.text(")!", NamedTextColor.WHITE));
        }
    }

    private void updateTimeIfNeeded(World overworld, int totalPlayers, int sleepingPlayers, boolean isPlayerLeaveEvent) {
        if (totalPlayers > 0 && (double) sleepingPlayers / totalPlayers >= sleepPercentage) {
            long currentTime = overworld.getTime();
            long timeInDay = currentTime % 24000;

            long timeToAdd = 24000 - timeInDay + MORNING_TIME;
            long newTime = currentTime + timeToAdd;

            overworld.setTime(newTime);

            if (isPlayerLeaveEvent) {
                double percentageNeeded = sleepPercentage * 100;
                Component message = Component.text("A player left and made it morning! ", NamedTextColor.WHITE)
                        .append(Component.text(String.format("(%.1f%% needed, %d/%d players voted)",
                                percentageNeeded, sleepingPlayers, totalPlayers), NamedTextColor.GRAY));
                Bukkit.getServer().broadcast(message);
            }
            playersWhoSlept.clear();  // Reset the players who have slept
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        boolean wasPlayerSleeping = playersWhoSlept.remove(player);

        // Delay the sleep check by 1 tick to ensure the player is fully disconnected
        Bukkit.getScheduler().runTask(this, () -> {
            World overworld = getOverworld();
            if (overworld != null) {
                long time = overworld.getTime();
                long timeInDay = time % 24000;
                if (timeInDay > 500 && timeInDay < 11000) {
                    return;
                }

                // Calculate sleep data after the player has been removed
                int[] sleepData = calculateSleepData();
                // Recalculate sleep percentage
                double currentSleepPercentage = sleepData[0] > 0 ? (double) sleepData[1] / sleepData[0] : 0;

                if (currentSleepPercentage >= sleepPercentage) {
                    updateTimeIfNeeded(overworld, sleepData[0], sleepData[1], true);
                }
            }
        });
    }

    private World getOverworld() {
        return Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);
    }
}