package fr.nihilis.scoreboardnotifier.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameEventBus {

    private static final List<Consumer<GameEvent>> listeners = new ArrayList<>();

    public static void register(Consumer<GameEvent> listener) {
        listeners.add(listener);
    }

    public static void post(GameEvent event) {
        listeners.forEach(l -> l.accept(event));
    }
}