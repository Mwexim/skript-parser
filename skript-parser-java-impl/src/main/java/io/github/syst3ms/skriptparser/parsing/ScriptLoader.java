package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.SkriptLogger;
import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.SimpleFileLine;
import io.github.syst3ms.skriptparser.lang.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ScriptLoader {
    private static final LinkedList<Loop> currentLoops = new LinkedList<>();

    public static List<Effect> loadItems(FileSection section) {
        List<Effect> items = new ArrayList<>();
        List<FileElement> elements = section.getElements();
        for (FileElement element : elements) {
            if (element instanceof FileSection) {
                FileSection sec = (FileSection) element;
                String content = sec.getLineContent();
                if (content.regionMatches(true, 0, "if ", 0, "if ".length())) {
                    String toParse = content.substring("if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, true);
                    if (booleanExpression == null) {
                        SkriptLogger.printError();
                        continue;
                    }
                    SkriptLogger.printLog();
                    items.add(new Conditional(sec, booleanExpression, Conditional.ConditionalMode.IF));
                } else if (content.regionMatches(true, 0, "else if ", 0, "else if ".length())) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        SkriptLogger.error("An 'else if' must be placed right after an 'if' or another 'else if'");
                        continue;
                    }

                    String toParse = content.substring("else if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, true);
                    if (booleanExpression == null) {
                        SkriptLogger.printError();
                        continue;
                    }
                    SkriptLogger.printLog();
                    Conditional c = new Conditional(sec, booleanExpression, Conditional.ConditionalMode.ELSE_IF);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                    items.add(c);
                } else if (content.equalsIgnoreCase("else")) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        SkriptLogger.error("An 'else' must be placed right after an 'if' or an 'else if'");
                        continue;
                    }

                    SkriptLogger.printLog();
                    Conditional c = new Conditional(sec, null, Conditional.ConditionalMode.ELSE);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                    items.add(c);
                } else {
                    CodeSection codeSection = SyntaxParser.parseSection(sec);
                    if (codeSection == null) {
                        SkriptLogger.printError();
                        continue;
                    }
                    SkriptLogger.printLog();
                    items.add(codeSection);
                }
            } else {
                assert element instanceof SimpleFileLine;
                SimpleFileLine line = (SimpleFileLine) element;
                String content = line.getLineContent();
                Effect eff = SyntaxParser.parseEffect(content);
                if (eff == null) {
                    SkriptLogger.printError();
                    continue;
                }
                SkriptLogger.printLog();
                items.add(eff);
            }
        }
        if (items.size() < elements.size())
            SkriptLogger.printError();
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
