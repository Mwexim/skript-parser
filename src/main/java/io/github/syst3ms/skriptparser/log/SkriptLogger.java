package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class managing Skript's I/O messages.
 */
public class SkriptLogger {
    public static final String LOG_FORMAT = "%s (line %d: \"%s\", %s)";

    private final boolean debug;

    private String fileName;
    private List<FileElement> fileElements;
    private int line = -1;
    private List<LogEntry> logEntries = new ArrayList<>();
    private List<LogEntry> logged = new ArrayList<>();
    private boolean open = true;
    private boolean hasError = false;
    public SkriptLogger(boolean debug) {
        this.debug = debug;
    }

    public SkriptLogger() {
        this(false);
    }

    public void setFileInfo(String fileName, List<FileElement> fileElements) {
        this.fileName = fileName;
        this.fileElements = flatten(fileElements);
    }

    private List<FileElement> flatten(List<FileElement> fileElements) {
        return fileElements.stream()
                .flatMap(e -> {
                    if (e instanceof FileSection) {
                        FileSection sec = (FileSection) e;
                        return Stream.concat(
                                Stream.of(e),
                                flatten(sec.getElements()).stream()
                        );
                    } else {
                        return Stream.of(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public void nextLine() {
        line++;
    }

    private void log(String message, LogType type) {
        if (open) {
            if (line == -1) {
                logEntries.add(new LogEntry(message, type));
            } else {
                logEntries.add(new LogEntry(String.format(LOG_FORMAT, message, line + 1, fileElements.get(line).getLineContent(), fileName), type));
            }
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
        if (debug)
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

    public boolean isDebug() {
        return debug;
    }
}
