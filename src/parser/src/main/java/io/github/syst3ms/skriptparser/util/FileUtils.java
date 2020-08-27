package io.github.syst3ms.skriptparser.util;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Utility functions for file parsing
 */
public class FileUtils {
    public static final Pattern LEADING_WHITESPACE_PATTERN = Pattern.compile("(\\s+)\\S.*");
    public static final String MULTILINE_SYNTAX_TOKEN = "\\";
    private static File jarFile;

    /**
     * Parses a file and returns a list containing all of its lines.
     *
     * This parser offers the possiblity to stretch out code across multiple lines by simply adding a single backslash
     * before a line break to indicate to the parser that it should be considered as a single line. For example :
     * <pre>
     *     set {large_list::*} to "one long string", \
     *                            "a second long string", \
     *                            "yet another long string" \
     *                            "an even longer string which would make reading very awkward otherwise", \
     *                            "nearing the end of the list" and \
     *                            "the end of the list"
     * </pre>
     * This text will be interpreted as a single long line with all the strings back to back.
     * The actual indentation before each additional line doesn't matter, all that matters is that it stays consistent.
     * @param filePath the file to parse
     * @return the lines of the file
     * @throws IOException if the file can't be read
     */
    public static List<String> readAllLines(Path filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        var multilineBuilder = new StringBuilder();
        for (var line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            line = line.replaceAll("\\s*$", "");
            if (line.replace("\\" + MULTILINE_SYNTAX_TOKEN, "\0")
                    .endsWith(MULTILINE_SYNTAX_TOKEN)) {
                multilineBuilder.append(line, 0, line.length() - 1).append("\0");
            } else if (multilineBuilder.length() > 0) {
                multilineBuilder.append(line);
                lines.add(trimMultilineIndent(multilineBuilder.toString()));
                multilineBuilder.setLength(0);
            } else {
                lines.add(line);
            }
        }
        if (multilineBuilder.length() > 0) {
            multilineBuilder.deleteCharAt(multilineBuilder.length() - 1);
            lines.add(trimMultilineIndent(multilineBuilder.toString()));
        }
        return lines;
    }

    /**
     * Counts the number of indents (a single tab or 4 spaces) at the beginning of a line, or alternatively just counts
     * the amount of spaces at the beginning of a line (with a tab counting as 4 regular spaces).
     * @param line the line
     * @param countAllSpaces if true, the method will count each space separately rather than in groups of 4
     * @return the indentation level
     */
    public static int getIndentationLevel(String line, boolean countAllSpaces) {
        var m = LEADING_WHITESPACE_PATTERN.matcher(line);
        if (m.matches()) {
            var space = m.group(1);
            if (countAllSpaces) {
                return 4 * StringUtils.count(space, "\t") + StringUtils.count(space, " ");
            } else {
                return StringUtils.count(space, "\t", "    ");
            }
        } else {
            return 0;
        }
    }

    private static String trimMultilineIndent(String multilineText) {
        var lines = multilineText.split("\0");
        // Inspired from Kotlin's trimIndent() function
        var baseIndent = Arrays.stream(lines)
                               .skip(1) // First line's indent should be ignored
                               .mapToInt(l -> getIndentationLevel(l, true))
                               .min()
                               .orElse(0);
        if (baseIndent == 0)
            return multilineText.replace("\0", "");
        var pat = Pattern.compile("\\s");
        var sb = new StringBuilder(lines[0]);
        for (var line : Arrays.copyOfRange(lines, 1, lines.length)) {
            var m = pat.matcher(line);
            for (var i = 0; i < baseIndent && m.find(); i++) {
                line = line.replaceFirst(m.group(), "");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Loads all classes of selected packages of the skript-parser JAR.
     * @param basePackage a root package
     * @param subPackages a list of all subpackages of the root package, in which classes will be leadied
     * @throws IOException
     */
    public static void loadClasses(File jarFile, String basePackage, String... subPackages) throws IOException {
        for (var i = 0; i < subPackages.length; i++)
            subPackages[i] = subPackages[i].replace('.', '/') + "/";
        basePackage = basePackage.replace('.', '/') + "/";
        try (var jar = new JarFile(jarFile)) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var e = entries.nextElement();
                if (e.getName().startsWith(basePackage) && e.getName().endsWith(".class")) {
                    var load = subPackages.length == 0;
                    for (final var sub : subPackages) {
                        if (e.getName().startsWith(sub, basePackage.length())) {
                            load = true;
                            break;
                        }
                    }
                    if (load && !e.getName().matches(".+\\$\\d+\\.class")) { // It's of very little use to load anonymous classes
                        final var c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
                        try {
                            Class.forName(c, true, FileUtils.class.getClassLoader());
                        } catch (final ClassNotFoundException | ExceptionInInitializerError ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the JAR file containing the given Class. Passing down the current class is recommended.
     * @param cla the class
     * @return the JAR file containing the class
     */
    public static File getCurrentJarFile(Class<?> cla) throws URISyntaxException {
        if (jarFile == null) {
            jarFile = new File(cla.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        return jarFile;
    }
}
