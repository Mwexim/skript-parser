package io.github.syst3ms.skriptparser.event;

/**
 * The event representing the main entry point in scripts,
 * equivalent to {@code public static void main(String[] args)} in Java
 */
public class MainEvent implements Event {
    private String[] args;

    public MainEvent(String[] args) {
        this.args = args;
    }

    @Override
    public String getEventName() {
        return "main";
    }

    public String[] getArguments() {
        return args;
    }
}
