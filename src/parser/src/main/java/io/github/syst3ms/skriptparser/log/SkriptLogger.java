package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An object through which Skript can keep track of errors, warnings and other useful information to the one that writes
 * Skript code.
 */
public class SkriptLogger {
    public static final String LOG_FORMAT = "%s (line %d: \"%s\", %s)";
    /*
     * In decreasing order of priority :
     * ErrorContext.RESTRICTED_SYNTAXES
     * ErrorContext.CONSTRAINT_CHECKING
     * ErrorContext.INITIALIZATION
     * ErrorContext.MATCHING
     * ErrorContext.MATCHING + ErrorContext.RESTRICTED_SYNTAXES
     * ErrorContext.MATCHING + ErrorContext.CONSTRAINT_CHECKING
     * ErrorContext.MATCHING + ErrorContext.INITIALIZATION
     * ErrorContext.MATCHING + ErrorContext.MATCHING
     * ErrorContext.MATCHING + ErrorContext.MATCHING + ErrorContext.RESTRICTED_SYNTAXES
     * ErrorContext.MATCHING + ErrorContext.MATCHING + ErrorContext.CONSTRAINT_CHECKING
     * ErrorContext.MATCHING + ErrorContext.MATCHING + ErrorContext.INITIALIZATION
     * ErrorContext.MATCHING + ErrorContext.MATCHING + ErrorContext.MATCHING
     * ...
     * ErrorContext.MATCHING + ErrorContext.MATCHING + ErrorContext.NO_MATCH
     * ErrorContext.MATCHING + ErrorContext.NO_MATCH
     * ErrorContext.NO_MATCH
     */
    private static final Comparator<LogEntry> ERROR_COMPARATOR = (e1, e2) -> {
        List<ErrorContext> c1 = e1.getErrorContext(),
                c2 = e2.getErrorContext();
        if (!c1.equals(c2)) {
            var s1 = c1.stream()
                    .map(c -> Integer.toString(c.ordinal()))
                    .collect(Collectors.joining());
            var s2 = c2.stream()
                    .map(c -> Integer.toString(c.ordinal()))
                    .collect(Collectors.joining());
            return s1.compareTo(s2);
        } else {
            return e1.getErrorType().ordinal() - e2.getErrorType().ordinal();
        }
    };
    // State
    private final boolean debug;
    private boolean open = true;
    private boolean hasError = false;
    private final LinkedList<ErrorContext> errorContext = new LinkedList<>();
    // File
    private String fileName;
    private List<FileElement> fileElements;
    private int line = -1;
    // Logs
    private final List<LogEntry> logEntries = new ArrayList<>();
    private final List<LogEntry> logged = new ArrayList<>();

    public SkriptLogger(boolean debug) {
        this.debug = debug;
        errorContext.addLast(ErrorContext.MATCHING);
    }

    public SkriptLogger() {
        this(false);
    }

    /**
     * Provides the logger information about the file it's currently parsing
     * @param fileName the file name
     * @param fileElements the {@link FileElement}s of the current file
     */
    public void setFileInfo(String fileName, List<FileElement> fileElements) {
        this.fileName = fileName;
        this.fileElements = flatten(fileElements);
    }

    private List<FileElement> flatten(List<FileElement> fileElements) {
        List<FileElement> list = new ArrayList<>();
        for (var element : fileElements) {
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
     * Like {@link #setLine(int)}, is only used for the purposes of the trigger loading priority system.
     * @return the current line
     */
    public int getLine() {
        return line;
    }

    /**
     * Like {@link #getLine()}, is only used for the purposes of the trigger loading priority system.
     * @param line the new line number
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Increments the recursion of the logger ; should be called before calling methods that may use SkriptLogger later
     * in execution.
     */
    public void recurse() {
        errorContext.addLast(ErrorContext.MATCHING);
    }

    /**
     * Decrements the recursion of the logger ; should be called after calling methods that may use SkriptLogger later
     * in execution.
     */
    public void callback() {
        errorContext.removeLast();
    }

    /**
     * Updates the error context, which matters for establishing which errors are the most important
     * @param context the new error context
     */
    public void setContext(ErrorContext context) {
        errorContext.removeLast();
        errorContext.addLast(context);
    }

    private void log(String message, LogType type, ErrorType error) {
        if (open) {
            List<ErrorContext> ctx = new ArrayList<>(errorContext);
            if (line == -1) {
                logEntries.add(new LogEntry(message, type, line, ctx, error));
            } else {
                logEntries.add(new LogEntry(String.format(LOG_FORMAT, message, line + 1, fileElements.get(line).getLineContent(), fileName), type, line, ctx, error));
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
            clearNotError(); // Errors take priority over everything (except DEBUG), so we just delete all other logs
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
        logEntries.removeIf(entry -> entry.getErrorContext().size() >= errorContext.size() && entry.getType() != LogType.ERROR && entry.getType() != LogType.DEBUG);
    }

    /**
     * Clears every log that is not a debug message.
     */
    public void clearLogs() {
        logEntries.removeIf(entry -> entry.getErrorContext().size() >= errorContext.size() && entry.getType() != LogType.DEBUG);
        errorContext.removeLast();
        errorContext.add(ErrorContext.MATCHING);
        hasError = false;
    }

    /**
     * Finishes a logging process by making some logged entries definitive. All non-error logs are made definitive
     * and only the error that has the most priority is made definitive.
     */
    public void logOutput() {
        logEntries.stream()
                .filter(e -> e.getType() == LogType.ERROR)
                .min(ERROR_COMPARATOR)
                .ifPresent(logged::add);
        for (var entry : logEntries) { // If no errors have been logged, then all other LogTypes get logged here, DEBUG being the special case
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
        logged.sort((e1, e2) -> { // Due to the load priority system, entries might not be ordered like their corresponding lines
            if (e1.getLine() == -1 || e2.getLine() == -1)
                return 0;
            return e1.getLine() - e2.getLine();
        });
        return logged;
    }

    /**
     * @return whether this Logger is in debug mode
     */
    public boolean isDebug() {
        return debug;
    }
}
