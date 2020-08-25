package io.github.syst3ms.skriptparser.log;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An entry in Skript's log.
 */
public class LogEntry {
    private final LogType type;
    private final String message;
    private final int line;
    private final List<ErrorContext> errorContext;
    private final ErrorType errorType;

    public LogEntry(String message, LogType verbosity, int line, List<ErrorContext> errorContext, @Nullable ErrorType errorType) {
        this.type = verbosity;
        this.message = message;
        this.line = line;
        this.errorContext = errorContext;
        this.errorType = errorType;
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

    ErrorType getErrorType() {
        return errorType;
    }

    public int getLine() {
        return line;
    }
}
