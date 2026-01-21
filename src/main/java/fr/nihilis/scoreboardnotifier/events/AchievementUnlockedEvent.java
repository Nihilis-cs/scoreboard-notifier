package fr.nihilis.scoreboardnotifier.events;

public record AchievementUnlockedEvent(
        String player,
        String achievementId,
        String description
) implements GameEvent {}
