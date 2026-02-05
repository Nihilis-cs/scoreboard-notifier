package fr.nihilis.scoreboardnotifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DiscordNotifier {

    private final String webhookUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    public DiscordNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendLeaderChange(String faction, String message) {
        int color = getFactionColor(faction);

        String json = """
        {
          "content": "",
          "embeds": [
            {
              "title": "🏆 Tournoi des 3 maisons",
              "description": "%s",
              "color": %d,
              "author": {
                "name": "Dukumon"
              },
              "footer": {
                "text": "Serveur Minecraft"
              }
            }
          ]
        }
        """.formatted(
                escape(message),
                color
        );

        sendRaw(json);
    }

    public void sendTie(List<String> factions, int score) {
        String factionList = String.join(", ", factions);

        String description = """
            ⚖️ **Égalité en tête du tournoi des 3 maisons !**
            
            %s sont à **%d points**.
            Il va falloir redoubler d'efforts 💪
            """.formatted(factionList, score);

        String json = """
        {
          "content": "",
          "embeds": [
            {
              "title": "⚖️ Tournoi des 3 maisons",
              "description": "%s",
              "color": %d,
              "author": {
                "name": "Dukumon"
              },
              "footer": {
                "text": "Serveur Minecraft"
              }
            }
          ]
        }
        """.formatted(
                escape(description),
                0xF1C40F
        );

        sendRaw(json);
    }

    private void sendRaw(String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getFactionColor(String faction) {
        return switch (faction) {
            case "Salador" -> 0xE74C3C;      // rouge
            case "Caradaigle" -> 0x3498DB;  // bleu
            case "Bulbitard" -> 0x2ECC71;   // vert
            default -> 0xF1C40F;            // or
        };
    }

    private String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    public void sendDailyLeader(String faction, String message) {
        int color = getFactionColor(faction);

        String json = """
        {
          "content": "",
          "embeds": [
            {
              "title": "📅 Rapport quotidien - Tournoi des 3 maisons",
              "description": "%s",
              "color": %d,
              "author": {
                "name": "Dukumon"
              },
              "footer": {
                "text": "Serveur Minecraft"
              },
              "timestamp": "%s"
            }
          ]
        }
        """.formatted(
                escape(message),
                color,
                java.time.Instant.now().toString()
        );

        sendRaw(json);
    }

    public void sendDailyTie(List<String> factions, int score) {
        String factionList = String.join(", ", factions);

        String description = """
            📅 **Rapport quotidien du tournoi des 3 maisons**
            
            %s sont toujours à égalité avec **%d points**.
            Il va falloir redoubler d'efforts ! 💪
            """.formatted(factionList, score);

        String json = """
        {
          "content": "",
          "embeds": [
            {
              "title": "📅 Rapport quotidien - Tournoi des 3 maisons",
              "description": "%s",
              "color": %d,
              "author": {
                "name": "Dukumon"
              },
              "footer": {
                "text": "Serveur Minecraft"
              },
              "timestamp": "%s"
            }
          ]
        }
        """.formatted(
                escape(description),
                0xF1C40F,
                java.time.Instant.now().toString()
        );

        sendRaw(json);
    }

}
