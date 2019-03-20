package io.github.syst3ms.skriptparser.log;

public class LogEntry {
    private final LogType type;
    private final String message;

    public LogEntry(String message, LogType verbosity) {
        this.type = verbosity;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public LogType getType() {
        return type;
    }

}
