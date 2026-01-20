package fr.nihilis.scoreboardnotifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DiscordNotifier {

    private final String webhookUrl;
    private final HttpClient client;

    public DiscordNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.client = HttpClient.newHttpClient();
    }

    public void send(String message) {
        try {
            String json = "{\"content\": \"" + message + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> System.out.println("Webhook sent: " + response.statusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
