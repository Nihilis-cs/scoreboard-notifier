package fr.nihilis.scoreboardnotifier;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.scoreboard.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScoreboardTracker {

    private final DiscordNotifier discord;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private String currentLeader = null;

    // Les factions “fictives”
    private final List<String> FACTIONS = Arrays.asList("Salador", "Caradaigle", "Bulbitard");

    public ScoreboardTracker(DiscordNotifier discord) {
        this.discord = discord;
    }

    public void start() {
        // Vérifie toutes les 5 secondes
        scheduler.scheduleAtFixedRate(this::checkLeaderboard, 5, 5, TimeUnit.SECONDS);
    }

    private void checkLeaderboard() {
        try {
            MinecraftServer server = ScoreboardNotifierMod.SERVER;
            // On récupère le serveur
            if (server == null) return;

            ServerWorld world = server.getOverworld();
            Scoreboard sb = world.getScoreboard();

            Optional<ScoreboardObjective> objectiveOpt = sb.getObjectives()
                    .stream()
                    .filter(o -> o.getName().equals("housePoint"))
                    .findFirst();
            if (objectiveOpt.isEmpty()) return;

            ScoreboardObjective objective = objectiveOpt.get();

            String newLeader = null;
            int maxScore = Integer.MIN_VALUE;
            // Cherche le leader parmi les factions
            Collection<ScoreHolder> factions = sb.getKnownScoreHolders();
            for (ScoreHolder faction : factions) {
                ReadableScoreboardScore  score = sb.getScore(faction, objective);
                if (score != null && score.getScore() > maxScore) {
                    maxScore = score.getScore();
                    newLeader = faction.getNameForScoreboard();
                }
            }

            // Si le leader a changé
            if (newLeader != null && !newLeader.equals(currentLeader)) {
                currentLeader = newLeader;
                String message = currentLeader + " passe en tête du tournoi des 3 maisons!";
                discord.sendLeaderChange(currentLeader, message);
                System.out.println(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
