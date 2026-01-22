package fr.nihilis.scoreboardnotifier;

import fr.nihilis.config.ConfigManager;
import fr.nihilis.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class ScoreboardNotifierMod implements ModInitializer {

    private static LeaderboardService leaderboardService;
    public static MinecraftServer SERVER;

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
        LeaderboardService leaderboardService = new LeaderboardService(discord);


        //
        ServerTickEvents.END_SERVER_TICK.register(server -> {
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

