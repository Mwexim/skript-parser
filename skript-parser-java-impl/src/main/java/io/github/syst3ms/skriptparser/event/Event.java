package io.github.syst3ms.skriptparser.event;

/**
 * Event interface made to somewhat replicate Bukkit's. Subject to changes or even a complete rework
 */
public interface Event {
    Event DUMMY = () -> "Dummy event";

    String getEventName();
}
