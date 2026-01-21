package fr.nihilis.scoreboardnotifier;

import fr.nihilis.scoreboardnotifier.events.GameEvent;
import fr.nihilis.scoreboardnotifier.events.GameEventBus;
import fr.nihilis.scoreboardnotifier.events.LeaderboardChangedEvent;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;

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

    private static final String OBJECTIVE_NAME = "housePoint";
    private static final LocalTime DAILY_MESSAGE_TIME = LocalTime.of(18, 30);

    private final DiscordNotifier discord;

    // Dernier état connu du leaderboard
    private LeaderboardState lastState = null;

    // Daily
    private LocalDate lastDailyMessageDate = null;

    public LeaderboardService(DiscordNotifier discord) {
        this.discord = discord;

        // Enregistrement en tant que listener
        GameEventBus.register(this::onGameEvent);
    }

    //Events handling

    private void onGameEvent(GameEvent event) {
        if (event instanceof LeaderboardChangedEvent(LeaderboardState newState)) {
            handleLeaderboardChange(newState);
        }
    }

    private void handleLeaderboardChange(LeaderboardState newState) {
        // Premier état reçu
        if (lastState == null) {
            lastState = newState;
            return;
        }

        // Aucun changement → rien à faire
        if (newState.equals(lastState)) {
            return;
        }

        // Leader unique
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

        lastState = newState;
    }

    //Daily notif

    public void checkDailyMessage() {
        if (lastState == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Pas encore l'heure
        if (now.toLocalTime().isBefore(DAILY_MESSAGE_TIME)) return;

        // Déjà envoyé aujourd’hui
        if (lastDailyMessageDate != null && lastDailyMessageDate.equals(today)) {
            return;
        }

        // Leader unique
        if (lastState.isSingleLeader()) {
            String leader = lastState.singleLeader();

            String message = leader +
                    " est toujours en tête du tournoi des 3 maisons. " +
                    "Il est temps de se mettre au travail les loosers !";

            discord.sendDailyLeader(leader, message);
            System.out.println("[ScoreboardNotifier] Daily: " + message);
        }

        // Égalité
        else if (lastState.isTie()) {
            String joined = String.join(", ", lastState.leaders());

            String message =
                    "Toujours une égalité en tête entre " + joined +
                            " (" + lastState.score() + " points). Y'a du taf !";

            discord.sendDailyTie(new ArrayList<>(lastState.leaders()), lastState.score());
            System.out.println("[ScoreboardNotifier] Daily: " + message);
        }

        lastDailyMessageDate = today;
    }

    //Calcul leader

    public static LeaderboardState computeLeaderboard(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        Optional<ScoreboardObjective> objective = scoreboard.getObjectives()
                .stream()
                .filter(o -> o.getName().equals(OBJECTIVE_NAME))
                .findFirst();

        if (objective.isEmpty()) {
            return new LeaderboardState(Set.of(), Integer.MIN_VALUE);
        }

        ScoreboardObjective obj = objective.get();

        int bestScore = Integer.MIN_VALUE;
        Set<String> leaders = new HashSet<>();

        for (String faction : FACTIONS) {
            ReadableScoreboardScore score =
                    scoreboard.getScore(ScoreHolder.fromName(faction), obj);

            if (score == null) continue;

            int value = score.getScore();

            if (value > bestScore) {
                bestScore = value;
                leaders.clear();
                leaders.add(faction);
            } else if (value == bestScore) {
                leaders.add(faction);
            }
        }

        return new LeaderboardState(leaders, bestScore);
    }
}
