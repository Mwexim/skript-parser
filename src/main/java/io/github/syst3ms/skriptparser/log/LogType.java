package io.github.syst3ms.skriptparser.log;

/**
 * An enum describing what a log message can be : an info, a warning, an error or a debug message.
 */
public enum LogType {
    /**
     * The log is a debug message, that should be shown if and only if debug mode is activated, always.
     */
    DEBUG,
    /**
     * The log is an info message, that merely provides additional information/statistics and doesn't call for changes/fixes.
     */
    INFO,
    /**
     * The log is a warning, that indicates things that should be avoided or might be undesirable, even if they aren't
     * critical to the point of making the code impossible to run.
     */
    WARNING,
    /**
     * The log is an error that needs fixing before being able to be run.
     */
    ERROR
}
