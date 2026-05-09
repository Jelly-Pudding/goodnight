package com.jellypudding.goodnight.commands;

import com.jellypudding.goodnight.managers.VoteManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GoodnightCommand implements CommandExecutor {

    private final VoteManager voteManager;

    public GoodnightCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        World overworld = voteManager.getOverworld();
        if (overworld == null) {
            player.sendMessage(Component.text("Cannot find the overworld.", NamedTextColor.RED));
            return true;
        }

        if (!voteManager.isNight(overworld)) {
            player.sendMessage(Component.text("You cannot say goodnight during the day.", NamedTextColor.RED));
            return true;
        }

        if (voteManager.hasVoted(player)) {
            player.sendMessage(Component.text("You have already said goodnight.", NamedTextColor.RED));
            return true;
        }

        voteManager.addVote(player);

        String customMessage = args.length > 0 ? String.join(" ", args) : null;
        Bukkit.getServer().broadcast(buildVoteMessage(player, customMessage));

        voteManager.trySkipNight(overworld);
        return true;
    }

    private Component buildVoteMessage(Player player, String customMessage) {
        Component base = player.displayName()
                .append(Component.text(" says goodnight", NamedTextColor.WHITE));
        if (customMessage != null) {
            return base.append(Component.text(" " + customMessage, NamedTextColor.WHITE));
        }
        return base.append(buildProgressSuffix());
    }

    private Component buildProgressSuffix() {
        int total    = voteManager.getTotalOnline();
        int voted    = voteManager.getVoterCount();
        int required = voteManager.getRequiredCount();
        int needed   = Math.max(0, required - voted);

        if (needed > 0) {
            double progress = total > 0
                    ? ((double) voted / total) / voteManager.getSleepPercentage() * 100
                    : 0;
            return Component.text(
                    String.format(" (%d more needed to make it day, %.1f%% / 100%%)", needed, progress),
                    NamedTextColor.GRAY);
        }

        return Component.text(" (Making it ", NamedTextColor.WHITE)
                .append(Component.text("day", NamedTextColor.YELLOW))
                .append(Component.text(")!", NamedTextColor.WHITE));
    }
}
