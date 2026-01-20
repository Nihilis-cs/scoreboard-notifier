package fr.nihilis.scoreboardnotifier;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;

public class LeaderboardService {

    private static final List<String> FACTIONS = List.of(
            "Salador",
            "Caradaigle",
            "Bulbitard"
    );

    private static final String OBJECTIVE_NAME = "Tournament";

    private final DiscordNotifier discord;
    private String currentLeader = null;
    private int tickCounter = 0;

    public LeaderboardService(DiscordNotifier discord) {
        this.discord = discord;
    }

    public void onServerTick(MinecraftServer server) {
        // On check toutes les 5 secondes (20 ticks = 1s)
        tickCounter++;
        if (tickCounter < 100) return;
        tickCounter = 0;

        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) return;

        String leader = null;
        int bestScore = Integer.MIN_VALUE;

        for (String faction : FACTIONS) {
            ScoreboardScore score = scoreboard.getPlayerScores(faction, objective);
            if (score != null && score.getScore() > bestScore) {
                bestScore = score.getScore();
                leader = faction;
            }
        }

        if (leader != null && !leader.equals(currentLeader)) {
            currentLeader = leader;

            String message = leader +
                    " passe en tête du tournoi des 3 maisons !";
            discord.send(message);

            System.out.println("[ScoreboardNotifier] " + message);
        }
    }
}