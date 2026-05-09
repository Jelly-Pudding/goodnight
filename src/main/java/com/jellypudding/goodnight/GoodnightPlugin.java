package com.jellypudding.goodnight;

import com.jellypudding.goodnight.commands.GoodnightCommand;
import com.jellypudding.goodnight.listeners.GoodnightListener;
import com.jellypudding.goodnight.managers.VoteManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GoodnightPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        double sleepPercentage = getConfig().getDouble("sleep-percentage", 66.6) / 100.0;

        VoteManager voteManager = new VoteManager(sleepPercentage);

        Objects.requireNonNull(getCommand("goodnight"))
                .setExecutor(new GoodnightCommand(voteManager));

        getServer().getPluginManager().registerEvents(
                new GoodnightListener(this, voteManager), this);

        new Metrics(this, 27564);

        getLogger().info("Goodnight plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodnight plugin has been disabled.");
    }
}
