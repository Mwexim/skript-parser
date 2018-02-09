package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.SimpleFileLine;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ScriptLoader {

    public static List<Effect> loadItems(FileSection section) {
        List<Effect> items = new ArrayList<>();
        List<FileElement> elements = section.getElements();
        for (int i = 0; i < elements.size(); i++) {
            FileElement element = elements.get(i);
            if (element instanceof FileSection) {
                FileSection sec = (FileSection) element;
                String content = sec.getLineContent();
                if (StringUtils.startsWithIgnoreCase(content, "if ")) {
                    String toParse = content.substring("if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, false);
                    if (booleanExpression == null) {
                        error("Can't understand this condition : " + toParse);
                        continue;
                    }
                    items.add(new Conditional(sec, booleanExpression, Conditional.ConditionalMode.IF));
                } else if (StringUtils.startsWithIgnoreCase(content, "else if ")) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        error("An 'else if' must be placed right after an 'if' or another 'else if'");
                    }
                    String toParse = content.substring("else if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, false);
                    if (booleanExpression == null) {
                        error("Can't understand this condition : " + toParse);
                        continue;
                    }
                    Conditional c = new Conditional(sec, booleanExpression, Conditional.ConditionalMode.ELSE_IF);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                    items.add(c);
                } else if (content.equalsIgnoreCase("else")) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        error("An 'else' must be placed right after an 'if' or an 'else if'");
                    }
                    Conditional c = new Conditional(sec, null, Conditional.ConditionalMode.ELSE);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                    items.add(c);
                } else {
                    // I am not doing this right now. Please.
                    error("Can't understand this section : " + sec.getLineContent());
                }
            } else {
                assert element instanceof SimpleFileLine;
                SimpleFileLine line = (SimpleFileLine) element;
                String content = line.getLineContent();
                Effect eff = SyntaxParser.parseEffect(content);
                if (eff == null)
                    continue;
                items.add(eff);
            }
        }
        for (int i = 0; i + 1 < items.size(); i++) {
            items.get(i).setNext(items.get(i + 1));
        }
        return items;
    }

    private static void error(String s) {
        // TODO get started with this thing
    }
}
