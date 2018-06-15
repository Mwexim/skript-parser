package io.github.syst3ms.skriptparser.event;

/**
 * The event representing the main entry point in scripts,
 * equivalent to {@code public static void main(String[] args)} in Java
 */
public class MainEvent implements TriggerContext {
    private String[] args;

    public MainEvent(String[] args) {
        this.args = args;
    }

    @Override
    public String getName() {
        return "main";
    }

    public String[] getArguments() {
        return args;
    }
}
