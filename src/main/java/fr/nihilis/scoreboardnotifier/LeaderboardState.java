package fr.nihilis.scoreboardnotifier;

import java.util.Set;

public record LeaderboardState(
        Set<String> leaders,
        int score
) {
    public boolean isTie() {
        return leaders.size() > 1;
    }

    public boolean isSingleLeader() {
        return leaders.size() == 1;
    }

    public String singleLeader() {
        return leaders.iterator().next();
    }
}
