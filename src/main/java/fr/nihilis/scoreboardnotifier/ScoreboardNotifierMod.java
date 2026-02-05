package fr.nihilis.scoreboardnotifier;

import fr.nihilis.config.ConfigManager;
import fr.nihilis.config.ModConfig;
import fr.nihilis.scoreboardnotifier.events.GameEventBus;
import fr.nihilis.scoreboardnotifier.events.LeaderboardChangedEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class ScoreboardNotifierMod implements ModInitializer {

    private static LeaderboardService leaderboardService;
    public static MinecraftServer SERVER;
    private static LeaderboardState lastKnownState = null;

    @Override
    public void onInitialize() {
        System.out.println("[ScoreboardNotifier] Mod loaded");

        ModConfig config = ConfigManager.load();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            SERVER = null;
        });

        if (!config.enabled || config.discordWebhookUrl.isBlank()) {
            System.out.println("Discord webhook disabled or not configured");
            return;
        }

        DiscordNotifier discord = new DiscordNotifier(config.discordWebhookUrl);
        leaderboardService = new LeaderboardService(discord);

        // Déclenchement automatique des événements de changement de leaderboard
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Vérifier les changements de leaderboard toutes les 10 ticks (0.5 seconde)
            if (server.getTicks() % 10 == 0) {
                LeaderboardState currentState = LeaderboardService.computeLeaderboard(server);
                if (lastKnownState == null || !currentState.equals(lastKnownState)) {
                    GameEventBus.post(new LeaderboardChangedEvent(currentState));
                    lastKnownState = currentState;
                }
            }
            
            // Vérifier le message quotidien
            leaderboardService.checkDailyMessage();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            System.out.println("[ScoreboardNotifier] Server stopped");
        });

        System.out.println("[ScoreboardNotifier] Mod initialized");
    }

    public static LeaderboardService getLeaderboardService() {
        return leaderboardService;
    }
}

