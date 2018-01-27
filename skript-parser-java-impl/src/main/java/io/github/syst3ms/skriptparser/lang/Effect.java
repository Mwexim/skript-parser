package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.SimpleFileLine;

@FunctionalInterface
public interface Effect {
    static Effect parse(SimpleFileLine s) {
        return fromLambda(() -> {});
    }

    static Effect fromLambda(Runnable runnable) {
        return runnable::run;
    }

    void execute();
}
