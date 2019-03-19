package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Loop;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.util.MultiMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains the logic for loading, parsing and interpreting entire script files
 */
public class ScriptLoader {
    private static final LinkedList<Loop> currentLoops = new LinkedList<>();
    private static MultiMap<String, Trigger> triggerMap = new MultiMap<>();

    public static MultiMap<String, Trigger> getTriggerMap() {
        return triggerMap;
    }

    /**
     * Parses and loads the provided script in memory
     * @param script the script file to load
     */
    public static void loadScript(File script) {
        FileParser parser = new FileParser();
        List<FileElement> elements;
        String scriptName;
        try {
            List<String> lines = FileUtils.readAllLines(script);
            scriptName = script.getName().replaceAll("(.+)\\..+", "$1");
            elements = parser.parseFileLines(scriptName,
                    lines,
                    0,
                    1
            );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (FileElement element : elements) {
            if (element instanceof FileSection) {
                Trigger trig = SyntaxParser.parseTrigger((FileSection) element);
                if (trig == null) {
                    // REMIND error
                    continue;
                }
                triggerMap.putOne(scriptName, trig);
            } else {
                // REMIND error
            }
        }
        SkriptAddon.getAddons().forEach(SkriptAddon::finishedLoading);
    }

    /**
     * Parses all items inside of a given section.
     * @param section the section
     * @return a list of {@linkplain Statement effects} inside of the section
     */
    public static List<Statement> loadItems(FileSection section) {
        List<Statement> items = new ArrayList<>();
        List<FileElement> elements = section.getElements();
        for (FileElement element : elements) {
            if (element instanceof FileSection) {
                FileSection sec = (FileSection) element;
                String content = sec.getLineContent();
                if (content.regionMatches(true, 0, "if ", 0, "if ".length())) {
                    String toParse = content.substring("if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse,
                            SyntaxParser.MAYBE_CONDITIONAL
                    );
                    if (booleanExpression == null) {
                        continue;
                    }
                    items.add(new Conditional(sec, booleanExpression, Conditional.ConditionalMode.IF));
                } else if (content.regionMatches(true, 0, "else if ", 0, "else if ".length())) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        continue;
                    }

                    String toParse = content.substring("else if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse,
                            SyntaxParser.MAYBE_CONDITIONAL
                    );
                    if (booleanExpression == null) {
                        continue;
                    }
                    Conditional c = new Conditional(sec, booleanExpression, Conditional.ConditionalMode.ELSE_IF);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                } else if (content.equalsIgnoreCase("else")) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        continue;
                    }

                    Conditional c = new Conditional(sec, null, Conditional.ConditionalMode.ELSE);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                } else {
                    CodeSection codeSection = SyntaxParser.parseSection(sec);
                    if (codeSection == null) {
                        continue;
                    }
                    items.add(codeSection);
                }
            } else {
                String content = element.getLineContent();
                Statement eff = SyntaxParser.parseStatement(content);
                if (eff == null) {
                    continue;
                }
                items.add(eff);
            }
        }
        for (int i = 0; i + 1 < items.size(); i++) {
            items.get(i).setNext(items.get(i + 1));
        }
        return items;
    }

    public static void addCurrentLoop(Loop loop) {
        currentLoops.addLast(loop);
    }

    public static Loop getCurrentLoop() {
        return currentLoops.getLast();
    }

    public static void removeCurrentLoop() {
        currentLoops.removeLast();
    }

    public static Iterable<Loop> getCurrentLoops() {
        return currentLoops;
    }
}
