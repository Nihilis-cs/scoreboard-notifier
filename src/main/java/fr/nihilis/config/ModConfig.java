package fr.nihilis.config;


import java.nio.file.Files;
import java.nio.file.Path;


public class ModConfig {


public static String webhookUrl = "";


public static void load() {
try {
Path path = Path.of("config/fr.nihilis.scoreboard-notifier.txt");
if (!Files.exists(path)) {
Files.createDirectories(path.getParent());
Files.writeString(path, "WEBHOOK_URL=PUT_YOUR_WEBHOOK_HERE");
}
webhookUrl = Files.readString(path).replace("WEBHOOK_URL=", "").trim();
} catch (Exception e) {
e.printStackTrace();
}
}
}