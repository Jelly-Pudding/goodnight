package com.jellypudding.goodnight.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class VoteManager {

    private static final long MORNING_TIME = 1000; // tick 1000 = 6:00 AM

    // Valid night window: anything outside the "day" band (500, 11000)
    private static final long DAY_START = 500;
    private static final long DAY_END   = 11000;

    private final Set<Player> voters = new HashSet<>();
    private final double sleepPercentage;

    public VoteManager(double sleepPercentage) {
        this.sleepPercentage = sleepPercentage;
    }

    public boolean addVote(Player player) {
        return voters.add(player);
    }

    public void removeVote(Player player) {
        voters.remove(player);
    }

    public boolean hasVoted(Player player) {
        return voters.contains(player);
    }

    public int getTotalOnline() {
        return Bukkit.getOnlinePlayers().size();
    }

    public int getVoterCount() {
        Set<Player> online = new HashSet<>(Bukkit.getOnlinePlayers());
        return (int) voters.stream().filter(online::contains).count();
    }

    public int getRequiredCount() {
        return (int) Math.ceil(getTotalOnline() * sleepPercentage);
    }

    public double getSleepPercentage() {
        return sleepPercentage;
    }

    public World getOverworld() {
        return Bukkit.getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);
    }

    public boolean isNight(World world) {
        long t = world.getTime() % 24000;
        return t <= DAY_START || t >= DAY_END;
    }

    public boolean hasReachedThreshold() {
        int total = getTotalOnline();
        return total > 0 && (double) getVoterCount() / total >= sleepPercentage;
    }

    public boolean trySkipNight(World overworld) {
        if (!hasReachedThreshold()) return false;
        long timeInDay = overworld.getTime() % 24000;
        overworld.setTime(overworld.getTime() + (24000 - timeInDay + MORNING_TIME));
        voters.clear();
        return true;
    }
}
