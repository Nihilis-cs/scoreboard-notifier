package fr.nihilis.scoreboardnotifier.events;

import fr.nihilis.scoreboardnotifier.LeaderboardState;

public record LeaderboardChangedEvent(
        LeaderboardState newState
) implements GameEvent {}

