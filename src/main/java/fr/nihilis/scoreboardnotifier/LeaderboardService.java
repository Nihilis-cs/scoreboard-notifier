package fr.nihilis.scoreboardnotifier;

import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class LeaderboardService {

    private static final List<String> FACTIONS = List.of(
            "Salador",
            "Caradaigle",
            "Bulbitard"
    );

    private static final String OBJECTIVE_NAME = "Tournament";
    private static final int TICK_INTERVAL = 200;

    private final DiscordNotifier discord;
    private String currentLeader = null;
    private int tickCounter = 0;
    private LeaderboardState lastState = null;

    private LocalDate lastDailyMessageDate = null;
    private static final LocalTime DAILY_MESSAGE_TIME = LocalTime.of(18, 30);

    public LeaderboardService(DiscordNotifier discord) {
        this.discord = discord;
    }

    public void onServerTick(MinecraftServer server) {
        // On check toutes les 5 secondes (20 ticks = 1s)
        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;
        //Recuperer le scoreboard et l'objective
        // TODO Ici ca peut merder sur la récupération de l'objective

        Scoreboard scoreboard = server.getScoreboard();
        Optional<ScoreboardObjective> objective = scoreboard.getObjectives().
                stream().filter(u -> u.getName().equals("housePoint")).findFirst();
        if (objective.isEmpty()) return;
        ScoreboardObjective obj = objective.orElseThrow();

        //1-recuperer la liste des maisons
        //2-Comparer les 3
        //3-ressortir la premiere
        List<String> leaders = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;

        Collection<ScoreHolder> factions = scoreboard.getKnownScoreHolders(); //TODO filtrer par academy_houses
        //On définit la faction leader en comparant les valeurs les unes aux autres
        for (ScoreHolder faction : factions) {
            ReadableScoreboardScore score = scoreboard.getScore(faction, obj);
            if (score == null) continue;

            int value = score.getScore();
            String factionName = faction.getNameForScoreboard();

            if (value > bestScore) {
                bestScore = value;
                leaders.clear();
                leaders.add(factionName);
            } else if (value == bestScore) {
                leaders.add(factionName);
            }
        }

        Set<String> leaderSet = new HashSet<>(leaders);
        LeaderboardState newState = new LeaderboardState(leaderSet, bestScore);

        // Premier passage (serveur qui démarre)
        if (lastState == null) {
            lastState = newState;
            return;
        }

        // Aucun changement → on ne fait RIEN
        if (newState.equals(lastState)) {
            return;
        }

        //Envoi des messages
        if (newState.isSingleLeader()) {
            String leader = newState.singleLeader();

            String message = leader +
                    " passe en tête du tournoi des 3 maisons !";

            discord.sendLeaderChange(leader, message);
            System.out.println("[ScoreboardNotifier] " + message);
        }

        // Égalité
        else if (newState.isTie()) {
            String joined = String.join(", ", newState.leaders());
            String message =
                    "Égalité en tête du tournoi entre : " +
                            joined + " (" + newState.score() + " points)";

            discord.sendTie(new ArrayList<>(newState.leaders()), newState.score());
            System.out.println("[ScoreboardNotifier] " + message);
        }

        // On mémorise APRÈS avoir envoyé
        lastState = newState;

        checkDailyMessage(bestScore, leaders);
    }
    private void checkDailyMessage(int bestScore, List<String> leaders) {
        if (leaders.isEmpty() || bestScore == Integer.MIN_VALUE) return;
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Pas encore l'heure
        if (now.toLocalTime().isBefore(DAILY_MESSAGE_TIME)) {
            return;
        }

        // Déjà envoyé aujourd'hui
        if (lastDailyMessageDate != null && lastDailyMessageDate.equals(today)) {
            return;
        }

        // Leader unique
        if (leaders.size() == 1) {
            String leader = leaders.getFirst();

            String message = leader +
                    " est toujours en tête du tournoi des 3 maisons. " +
                    "Il est temps de se mettre au travail les loosers !";

            discord.sendDailyLeader(leader, message);
            System.out.println("[ScoreboardNotifier] Daily: " + message);
        }

        // Égalité
        else if (leaders.size() > 1) {
            String joined = String.join(", ", leaders);

            String message =
                    "Toujours une égalité en tête entre " + joined +
                            " (" + bestScore + " points). Rien n’est joué !";

            discord.sendDailyTie(leaders, bestScore);
            System.out.println("[ScoreboardNotifier] Daily: " + message);
        }

        // On mémorise l'envoi
        lastDailyMessageDate = today;
    }
}