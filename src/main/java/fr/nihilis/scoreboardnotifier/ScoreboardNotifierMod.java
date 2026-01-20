package fr.nihilis.scoreboardnotifier;

import fr.nihilis.config.ConfigManager;
import fr.nihilis.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ScoreboardNotifierMod implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("[ScoreboardNotifier] Mod loaded");

        ModConfig config = ConfigManager.load();

        if (!config.enabled || config.discordWebhookUrl.isBlank()) {
            System.out.println("Discord webhook disabled or not configured");
            return;
        }

        DiscordNotifier discord = new DiscordNotifier(config.discordWebhookUrl);
        LeaderboardService leaderboard = new LeaderboardService(discord);

        // Appelé à chaque tick serveur
        ServerTickEvents.END_SERVER_TICK.register(leaderboard::onServerTick);
    }
}
