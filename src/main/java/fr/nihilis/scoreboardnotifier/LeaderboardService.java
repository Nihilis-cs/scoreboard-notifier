package fr.nihilis.scoreboardnotifier;

import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        //Recuperer le scoreboard

        Scoreboard scoreboard = server.getScoreboard();
        //TODO on récupère les objectifs liés au datapack academy_houses (specifiquement les points de maison)
        Optional<ScoreboardObjective> objective = scoreboard.getObjectives().
                stream().filter(u -> u.getDisplayName().contains(Text.of("housePoint"))).findFirst();
        if (objective.isEmpty()) return;

        //TODO on défini la maison leader
        //1-recuperer la liste des maisons
        //2-Comparer les 3
        //3-ressortir la premiere
        String leader = null;
        int bestScore = Integer.MIN_VALUE;

        Collection<ScoreHolder> factions = scoreboard.getKnownScoreHolders(); //TODO filtrer par academy_houses

        for (ScoreHolder faction : factions) {
            ScoreboardScore score = scoreboard.getScore(faction, objective);
            if (score != null && score.getScore() > bestScore) {
                bestScore = score.getScore();
                leader = String.valueOf(faction.getDisplayName());
            }
        }
        //Une fois le leader définit on envoie le message pour le webhook
        if (leader != null && !leader.equals(currentLeader)) {
            currentLeader = leader;

            String message = leader +
                    " passe en tête du tournoi des 3 maisons !";
            discord.sendLeaderChange(leader, message);

            System.out.println("[ScoreboardNotifier] " + message);
        }
    }
}