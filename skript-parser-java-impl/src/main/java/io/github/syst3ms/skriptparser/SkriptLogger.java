package io.github.syst3ms.skriptparser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkriptLogger {
    @Nullable
    private static String error;
    private static List<String> warnings = new ArrayList<>();

    public static void error(String s) {
        if (error == null)
            error = s;
    }

    public static void warn(String s) {
        warnings.add(s);
    }

    public static void clear() {
        error = null;
        warnings.clear();
    }

    public static void printError() {
        if (error != null) {
            System.err.println("[Error] " + error);
        }
        clear();
    }

    public static void printLog() {
        if (error != null)
            throw new IllegalStateException("Can't print a warnings when there's already an error");
        for (String warning : warnings) {
            System.err.println("[Warning] " + warning);
        }
        clear();
    }
}
