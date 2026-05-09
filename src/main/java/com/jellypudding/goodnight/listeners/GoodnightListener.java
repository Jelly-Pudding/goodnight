package com.jellypudding.goodnight.listeners;

import com.jellypudding.goodnight.managers.VoteManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class GoodnightListener implements Listener {

    private final Plugin plugin;
    private final VoteManager voteManager;

    public GoodnightListener(Plugin plugin, VoteManager voteManager) {
        this.plugin = plugin;
        this.voteManager = voteManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        voteManager.removeVote(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            World overworld = voteManager.getOverworld();
            if (overworld == null || !voteManager.isNight(overworld)) return;

            int total = voteManager.getTotalOnline();
            int voted = voteManager.getVoterCount();

            if (voteManager.trySkipNight(overworld)) {
                broadcastLeaveSkip(player, voted, total);
            }
        });
    }

    private void broadcastLeaveSkip(Player player, int voted, int total) {
        double percentageNeeded = voteManager.getSleepPercentage() * 100;
        Component message = player.displayName()
                .append(Component.text(" left and made it morning! ", NamedTextColor.WHITE))
                .append(Component.text(
                        String.format("(%.1f%% needed, %d/%d players voted)", percentageNeeded, voted, total),
                        NamedTextColor.GRAY));
        Bukkit.getServer().broadcast(message);
    }
}
