package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;

import java.util.*;

/**
 * A class managing Skript's I/O messages.
 */
public class SkriptLogger {
    public static final String LOG_FORMAT = "%s (line %d: \"%s\", %s)";
    private static final Comparator<LogEntry> ERROR_COMPARATOR = (e1, e2) -> {
        if (e1.getErrorType().ordinal() != e2.getErrorType().ordinal()) {
            return e1.getErrorType().ordinal() - e2.getErrorType().ordinal();
        } else {
            return e1.getRecursion() - e2.getRecursion();
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

    /**
     * Advances in the currently analysed file. Used to properly display errors.
     */
    public void nextLine() {
        line++;
    }

    /**
     * Increments the recursion of the logger ; should be called before calling methods that may use SkriptLogger later
     * in execution.
     */
    public void startLogHandle() {
        recursion++;
    }

    /**
     * Decrements the recursion of the logger ; should be called after calling methods that may use SkriptLogger later
     * in execution.
     */
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

    /**
     * Logs an error message
     * @param message the error message
     * @param errorType the error type
     */
    public void error(String message, ErrorType errorType) {
        if (!hasError) {
            clearNotError();
            log(message, LogType.ERROR, errorType);
            hasError = true;
        }
    }

    /**
     * Logs a warning message
     * @param message the warning message
     */
    public void warn(String message) {
        log(message, LogType.WARNING, null);
    }

    /**
     * Logs an info message
     * @param message the info message
     */
    public void info(String message) {
        log(message, LogType.INFO, null);
    }

    /**
     * Logs a debug message. Will only work if debug mode is enabled.
     * @param message the debug message
     */
    public void debug(String message) {
        if (debug)
            log(message, LogType.DEBUG, null);
    }

    /**
     * Used to "forget" about a previous error, in case it is desirable to take into account multiple errors.
     * Should only be called by the parser.
     */
    public void forgetError() {
        hasError = false;
    }

    /**
     * Clears every log that is not an error or a debug message.
     */
    public void clearNotError() {
        logEntries.removeIf(entry -> entry.getRecursion() >= recursion && entry.getType() != LogType.ERROR && entry.getType() != LogType.DEBUG);
    }

    /**
     * Clears every log that is not a debug message.
     */
    public void clearLogs() {
        logEntries.removeIf(entry -> entry.getRecursion() >= recursion && entry.getType() != LogType.DEBUG);
        hasError = false;
    }

    /**
     * Finishes a logging process by making some logged entries definitive. All non-error logs are made definitive,
     * and only the error that has the most priority is made definitive.
     */
    public void logOutput() {
        logEntries.stream()
                .filter(e -> e.getType() == LogType.ERROR)
                .max(ERROR_COMPARATOR)
                .ifPresent(logged::add);
        for (LogEntry entry : logEntries) {
            if (entry.getType() != LogType.ERROR) {
                logged.add(entry);
            }
        }
        clearLogs();
    }

    /**
     * Finishes this Logger object, making it impossible to edit.
     * @return the final logged entries
     */
    public List<LogEntry> close() {
        open = false;
        return logged;
    }

    /**
     * @return whether this Logger is in debug mode
     */
    public boolean isDebug() {
        return debug;
    }
}
