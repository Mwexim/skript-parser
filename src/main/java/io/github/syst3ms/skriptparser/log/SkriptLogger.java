package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.util.MultiMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class managing Skript's I/O messages.
 */
public class SkriptLogger {
    public static final String LOG_FORMAT = "%s (line %d: \"%s\", %s)";
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
    private MultiMap<Integer, LogEntry> logEntries = new MultiMap<>();
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

    private void log(String message, LogType type) {
        if (open) {
            if (line == -1) {
                logEntries.putOne(recursion, new LogEntry(message, type));
            } else {
                logEntries.putOne(recursion, new LogEntry(String.format(LOG_FORMAT, message, line + 1, fileElements.get(line).getLineContent(), fileName), type));
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
        int i = recursion;
        while (logEntries.containsKey(i)) {
            List<LogEntry> previous = logEntries.remove(i);
            logEntries.put(
                    i,
                    previous.stream()
                            .filter(e -> e.getType() == LogType.ERROR || e.getType() == LogType.DEBUG)
                            .collect(Collectors.toList())
            );
            i++;
        }
    }

    public void forgetError() {
        hasError = false;
    }

    public void clearLogs() {
        int i = recursion;
        while (logEntries.containsKey(i)) {
            List<LogEntry> previous = logEntries.remove(i);
            logEntries.put(
                    i,
                    previous.stream()
                            .filter(e -> e.getType() == LogType.DEBUG)
                            .collect(Collectors.toList())
            );
            i++;
        }
        hasError = false;
    }

    public void logOutput() {
        List<LogEntry> flatView = logEntries.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().sorted((e1, e2) -> e2.getType().ordinal() - e1.getType().ordinal()))
                .collect(Collectors.toList());
        boolean hasError = false;
        for (LogEntry entry : flatView) {
            if (entry.getType() != LogType.ERROR || !hasError) {
                logged.add(entry);
                hasError = entry.getType() == LogType.ERROR;
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
