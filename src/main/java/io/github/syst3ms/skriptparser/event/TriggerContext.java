package io.github.syst3ms.skriptparser.event;

public interface TriggerContext {
    TriggerContext DUMMY = () -> "dummy";

    String getName();
}
