package fr.nihilis.scoreboardnotifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ScoreboardNotifierMod implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("[ScoreboardNotifier] Mod loaded");

        DiscordNotifier discord = new DiscordNotifier("TON_WEBHOOK_ICI");
        LeaderboardService leaderboard = new LeaderboardService(discord);

        // Appelé à chaque tick serveur
        ServerTickEvents.END_SERVER_TICK.register(leaderboard::onServerTick);
    }
}
