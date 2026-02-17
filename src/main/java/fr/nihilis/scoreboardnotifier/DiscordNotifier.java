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
        String customMessage = getCustomLeaderMessage(faction);

        String json = safeFormat("""
        {
          "content": "",
          "embeds": [
            {
              "title": "%s",
              "description": "%s",
              "color": %d,
              "author": {
                "name": "Dukumon"
              },
              "footer": {
                "text": "Dukumon Academy"
              }
            }
          ]
        }
        """,
                escape(getFactionTitle(faction)),
                escape(customMessage),
                color
        );

        sendRaw(json);
    }

    public void sendTie(List<String> factions, int score) {
        String factionList = String.join(", ", factions);

        String description = safeFormat("""
            ⚖️ **Égalité en tête du tournoi des 3 maisons !**
            
            %s sont à **%d points**.
            Il va falloir redoubler d'efforts 💪
            """, factionList, score);

        String json = safeFormat("""
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
                "text": "Dukumon Academy"
              }
            }
          ]
        }
        """,
                escape(description),
                0xF1C40F
        );

        sendRaw(json);
    }

    public void sendDailyLeader(String faction, String message) {
        int color = getFactionColor(faction);
        String customDailyMessage = getCustomDailyLeaderMessage(faction);

        String json = safeFormat("""
        {
          "content": "",
          "embeds": [
            {
              "title": "📅 %s",
              "description": "%s",
              "color": %d,
              "author": {
                "name": "Dukumon"
              },
              "footer": {
                "text": "Dukumon Academy"
              },
              "timestamp": "%s"
            }
          ]
        }
        """,
                escape(getFactionDailyTitle(faction)),
                escape(customDailyMessage),
                color,
                java.time.Instant.now().toString()
        );

        sendRaw(json);
    }

    public void sendDailyTie(List<String> factions, int score) {
        String factionList = String.join(", ", factions);

        String description = safeFormat("""
            📅 **Rapport quotidien du tournoi des 3 maisons**
            
            %s sont toujours à égalité avec **%d points**.
            Il va falloir redoubler d'efforts ! 💪
            """, factionList, score);

        String json = safeFormat("""
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
                "text": "Dukumon Academy"
              },
              "timestamp": "%s"
            }
          ]
        }
        """,
                escape(description),
                0xF1C40F,
                java.time.Instant.now().toString()
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

    private String getFactionTitle(String faction) {
        return switch (faction) {
            case "Salador" -> "Salador prend les devants !";
            case "Caradaigle" -> "Caradaigle s'envole vers la victoire !";
            case "Bulbitard" -> "Bulbitard frappe dans l'ombre !";
            default -> "🏆 Tournoi des 3 maisons";
        };
    }

    private String getCustomLeaderMessage(String faction) {
        return switch (faction) {
            case "Salador" -> """
                🔥 **Les flammes de Salador illuminent le tournoi !**
                
                Les courageux de Salador ont pris la tête du classement !
                Leur bravoure et leur détermination portent leurs fruits. 🔥
                
                *"Par le feu et l'honneur, Salador triomphera !"*
                """;
            case "Caradaigle" -> """
                💧 **L'intelligence de Caradaigle domine le tournoi !**
                
                Les sages de Caradaigle prennent les devants !
                Leur stratégie et leur sagesse les mènent vers la victoire. 💧
                
                *"La connaissance est le plus grand pouvoir !"*
                """;
            case "Bulbitard" -> """
                🍃 **La ruse de Bulbitard frappe encore !**
                
                Les astucieux de Bulbitard ont pris la tête !
                Leur ambition et leur ingéniosité les propulsent au sommet. 🍃
                
                *"Par tous les moyens nécessaires !"*
                """;
            default -> faction + " passe en tête du tournoi des 3 maisons !";
        };
    }

    private String getFactionDailyTitle(String faction) {
        return switch (faction) {
            case "Salador" -> "Le règne de feu continue";
            case "Caradaigle" -> "L'excellence intellectuelle perdure";
            case "Bulbitard" -> "La domination par la ruse";
            default -> "Rapport quotidien - Tournoi des 3 maisons";
        };
    }

    private String getCustomDailyLeaderMessage(String faction) {
        return switch (faction) {
            case "Salador" -> """
                **Salador maintient sa domination !**
                
                Un jour de plus au sommet pour les vaillants guerriers !
                Leur flamme brûle toujours aussi fort. 🔥
                
                *Les autres maisons feraient bien de se réveiller !*
                """;
            case "Caradaigle" -> """
                **La sagesse de Caradaigle règne toujours !**
                
                Un jour de plus à la tête grâce à leur intelligence !
                Leur stratégie continue de porter ses fruits. 💧
                
                *La connaissance mène à la victoire !*
                """;
            case "Bulbitard" -> """
                **Bulbitard conserve son avantage !**
                
                Toujours en tête grâce à leur astuce légendaire !
                Leur ambition ne connaît pas de limites. 🍃
                
                *L'ingéniosité triomphe encore !*
                """;
            default -> faction + " est toujours en tête du tournoi des 3 maisons. Il est temps de se mettre au travail les loosers !";
        };
    }

    private String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    /**
     * Formate une chaîne de manière sécurisée, en gérant les erreurs de format
     * @param template Le template de format
     * @param args Les arguments de format
     * @return La chaîne formatée ou le template original en cas d'erreur
     */
    private String safeFormat(String template, Object... args) {
        try {
            return template.formatted(args);
        } catch (Exception e) {
            System.err.println("Erreur de formatage Discord: " + e.getMessage());
            System.err.println("Template: " + template);
            System.err.println("Arguments: " + java.util.Arrays.toString(args));
            // Retourner le template original sans formatage plutôt que de crasher
            return template;
        }
    }

}
