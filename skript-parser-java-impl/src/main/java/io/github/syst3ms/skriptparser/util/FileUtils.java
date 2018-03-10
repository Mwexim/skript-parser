package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
    private static File jarFileLocation;
    public static final Pattern LEADING_WHITESPACE_PATTERN = Pattern.compile("(\\s+)\\S.+");
    public static final String MULTILINE_SYNTAX_TOKEN = "\\";

    public static List<String> readAllLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        FileReader in = new FileReader(file);
        BufferedReader reader = new BufferedReader(in);
        String line;
        StringBuilder multilineBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.replace("\\" + MULTILINE_SYNTAX_TOKEN, "\0")
                    .endsWith(MULTILINE_SYNTAX_TOKEN)) {
                multilineBuilder.append(line.substring(0, line.length() - 1)).append("\0");
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

    public static int getIndentationLevel(String line) {
        Matcher m = LEADING_WHITESPACE_PATTERN.matcher(line);
        if (m.matches()) {
            return StringUtils.count(m.group(1), "\t", "    ");
        } else {
            return 0;
        }
    }

    private static String trimMultilineIndent(String multilineText) {
        String[] lines = multilineText.split("\0");
        // Inspired from Kotlin's trimIndent() function
        int baseIndent = Arrays.stream(lines)
                               .skip(1) // First line's indent should be ignored
                               .mapToInt(FileUtils::getIndentationLevel)
                               .min()
                               .orElse(0);
        if (baseIndent == 0)
            return multilineText.replace("\0", "");
        Pattern pat = Pattern.compile("\\t| {4}");
        StringBuilder sb = new StringBuilder(lines[0]);
        for (String line : Arrays.copyOfRange(lines, 1, lines.length)) {
            Matcher m = pat.matcher(line);
            for (int i = 0; i < baseIndent && m.find(); i++) {
                line = line.replaceFirst(m.group(), "");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    public static void loadClasses(String basePackage, String... subPackages) throws IOException, URISyntaxException {
        for (int i = 0; i < subPackages.length; i++)
            subPackages[i] = subPackages[i].replace('.', '/') + "/";
        basePackage = basePackage.replace('.', '/') + "/";
        try (JarFile jar = new JarFile(getJarFileLocation())) {
            List<JarEntry> entries = Collections.list(jar.entries());
            for (JarEntry e : entries) {
                if (e.getName().startsWith(basePackage) && e.getName().endsWith(".class")) {
                    boolean load = subPackages.length == 0;
                    for (String sub : subPackages) {
                        if (e.getName().startsWith(sub, basePackage.length())) {
                            load = true;
                            break;
                        }
                    }
                    if (load) {
                        String c = e.getName()
                                    .replace('/', '.')
                                    .substring(0, e.getName().length() - ".class".length());
                        try {
                            Class.forName(c, true, FileUtils.class.getClassLoader());
                        } catch (ClassNotFoundException | ExceptionInInitializerError ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static File getJarFileLocation() throws URISyntaxException {
        if (jarFileLocation != null) {
            return jarFileLocation;
        } else {
            return (jarFileLocation = new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
        }
    }
}
