package io.github.syst3ms.skriptparser.log;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class managing Skript's I/O messages.
 */
public class SkriptLogger {
    private List<LogEntry> logEntries = new ArrayList<>();
    private List<LogEntry> logged = new ArrayList<>();
    private boolean open = true;
    private boolean hasError = false;

    private void log(String message, LogType type) {
        if (open) {
            logEntries.add(new LogEntry(message, type));
        }
    }

    public void error(String message) {
        if (!hasError) {
            clearNotError();
            log(message, LogType.ERROR);
            hasError = true;
        }
    }

    public void warn(String message) {
        log(message, LogType.WARNING);
    }

    public void info(String message) {
        log(message, LogType.INFO);
    }

    public void debug(String message) {
        log(message, LogType.DEBUG);
    }

    public void clearNotError() {
        this.logEntries = logEntries.stream().filter(e -> e.getType() == LogType.ERROR || e.getType() == LogType.DEBUG).collect(Collectors.toList());
    }

    public void clearError() {
        this.logEntries = logEntries.stream().filter(e -> e.getType() != LogType.ERROR).collect(Collectors.toList());
        hasError = false;
    }

    public void clearLogs() {
        logEntries.clear();
        hasError = false;
    }

    public void logOutput() {
        logged.addAll(logEntries);
        clearLogs();
    }

    public List<LogEntry> close() {
        open = false;
        return logged;
    }
}
