package io.github.syst3ms.skriptparser.event;

/**
 * A context under which a trigger may be run.
 * A simpler version of Bukkit's Event class.
 */
public interface TriggerContext {
    /**
     * A dummy TriggerContext that may be used when the context is known not to matter.
     */
    TriggerContext DUMMY = () -> "dummy";

    String getName();
}
