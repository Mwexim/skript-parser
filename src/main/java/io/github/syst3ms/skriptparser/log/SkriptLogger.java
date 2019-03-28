package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class managing Skript's I/O messages.
 */
public class SkriptLogger {
    public static final String LOG_FORMAT = "%s (line %d: \"%s\", %s)";
    private static final Comparator<LogEntry> ERROR_COMPARATOR = (e1, e2) -> {
        if (e1.getErrorType().ordinal() != e2.getErrorType().ordinal()) {
            return e2.getErrorType().ordinal() - e1.getErrorType().ordinal();
        } else {
            return e2.getRecursion() - e1.getRecursion();
        }
    };
    // State
    private final boolean debug;
    private boolean open = true;
    private boolean hasError = false;
    private int recursion = 1;
    // File
    private String fileName;
    private List<FileElement> fileElements;
    private int line = -1;
    // Logs
    private List<LogEntry> logEntries = new ArrayList<>();
    private List<LogEntry> logged = new ArrayList<>();

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
        List<FileElement> list = new ArrayList<>();
        for (FileElement element : fileElements) {
            list.add(element);
            if (element instanceof FileSection) {
                list.addAll(flatten(((FileSection) element).getElements()));
            }
        }
        return list;
    }

    public void nextLine() {
        line++;
    }

    public void startLogHandle() {
        recursion++;
    }

    public void closeLogHandle() {
        recursion--;
    }

    private void log(String message, LogType type, ErrorType error) {
        if (open) {
            if (line == -1) {
                logEntries.add(new LogEntry(message, type, recursion, error));
            } else {
                logEntries.add(new LogEntry(String.format(LOG_FORMAT, message, line + 1, fileElements.get(line).getLineContent(), fileName), type, recursion, error));
            }
        }
    }

    public void error(String message, ErrorType errorType) {
        if (!hasError) {
            clearNotError();
            log(message, LogType.ERROR, errorType);
            hasError = true;
        }
    }

    public void warn(String message) {
        log(message, LogType.WARNING, null);
    }

    public void info(String message) {
        log(message, LogType.INFO, null);
    }

    public void debug(String message) {
        if (debug)
            log(message, LogType.DEBUG, null);
    }

    public void forgetError() {
        hasError = false;
    }

    public void clearNotError() {
        logEntries.removeIf(entry -> entry.getRecursion() >= recursion && entry.getType() != LogType.ERROR && entry.getType() != LogType.DEBUG);
    }

    public void clearLogs() {
        logEntries.removeIf(entry -> entry.getRecursion() >= recursion && entry.getType() != LogType.DEBUG);
        hasError = false;
    }

    public void logOutput() {
        logEntries.stream()
                .filter(e -> e.getType() == LogType.ERROR)
                .min(ERROR_COMPARATOR)
                .ifPresent(logged::add);
        for (LogEntry entry : logEntries) {
            if (entry.getType() != LogType.ERROR) {
                logged.add(entry);
            }
        }
        clearLogs();
    }

    public void logAndClose() {
        logOutput();
        closeLogHandle();
    }

    public List<LogEntry> close() {
        open = false;
        return logged;
    }

    public boolean isDebug() {
        return debug;
    }
}
