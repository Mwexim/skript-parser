package io.github.syst3ms.skriptparser.log;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * An entry in Skript's log.
 */
public class LogEntry {
    private final LogType type;
    private final String message;
    private final int line;
    private final List<ErrorContext> errorContext;
    private final ErrorType errorType;
    private final String tip;

    public LogEntry(String message, LogType verbosity, int line, List<ErrorContext> errorContext, @Nullable ErrorType errorType) {
        this(message, verbosity, line, errorContext, errorType, null);
    }

    public LogEntry(String message, LogType verbosity, int line, List<ErrorContext> errorContext, @Nullable ErrorType errorType, @Nullable String tip) {
        this.type = verbosity;
        this.message = message;
        this.line = line;
        this.errorContext = errorContext;
        this.errorType = errorType;
        this.tip = tip;
    }

    public String getMessage() {
        return message;
    }

    public LogType getType() {
        return type;
    }

    List<ErrorContext> getErrorContext() {
        return errorContext;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public Optional<String> getTip() {
        return Optional.ofNullable(tip);
    }

    public int getLine() {
        return line;
    }
}
