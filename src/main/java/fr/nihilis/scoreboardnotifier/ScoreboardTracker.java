package fr.nihilis.scoreboardnotifier;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardPlayerScore;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScoreboardTracker {

    private final DiscordNotifier discord;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private String currentLeader = null;

    // Les factions “fictives”
    private final List<String> factions = Arrays.asList("Salador", "Caradaigle", "Bulbitard");

    public ScoreboardTracker(DiscordNotifier discord) {
        this.discord = discord;
    }

    public void start() {
        // Vérifie toutes les 5 secondes
        scheduler.scheduleAtFixedRate(this::checkLeaderboard, 5, 5, TimeUnit.SECONDS);
    }

    private void checkLeaderboard() {
        try {
            // On récupère le serveur
            MinecraftServer server = net.minecraft.server.MinecraftServer.getServer(); // singleton
            if (server == null) return;

            ServerWorld world = server.getOverworld();
            Scoreboard sb = world.getScoreboard();

            String newLeader = null;
            int maxScore = Integer.MIN_VALUE;

            // Cherche le leader parmi les factions
            for (String faction : factions) {
                ScoreboardPlayerScore score = sb.getPlayerScore(faction, sb.getObjective("Tournament"));
                if (score != null && score.getScore() > maxScore) {
                    maxScore = score.getScore();
                    newLeader = faction;
                }
            }

            // Si le leader a changé
            if (newLeader != null && !newLeader.equals(currentLeader)) {
                currentLeader = newLeader;
                String message = currentLeader + " passe en tête du tournoi des 3 maisons!";
                discord.send(message);
                System.out.println(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
