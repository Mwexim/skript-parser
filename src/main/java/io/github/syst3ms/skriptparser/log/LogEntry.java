package io.github.syst3ms.skriptparser.log;

import org.jetbrains.annotations.Nullable;

public class LogEntry {
    private final LogType type;
    private final String message;
    private final int recursion;
    private final ErrorType errorType;

    public LogEntry(String message, LogType verbosity, int recursion, @Nullable ErrorType errorType) {
        this.type = verbosity;
        this.message = message;
        this.recursion = recursion;
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public LogType getType() {
        return type;
    }

    int getRecursion() {
        return recursion;
    }

    ErrorType getErrorType() {
        return errorType;
    }
}
