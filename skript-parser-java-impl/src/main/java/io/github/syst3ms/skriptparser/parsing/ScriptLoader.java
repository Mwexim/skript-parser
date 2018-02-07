package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.SimpleFileLine;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;

import java.util.ArrayList;
import java.util.List;

public class ScriptLoader {

    public static List<Effect> loadItems(FileSection section) {
        List<Effect> items = new ArrayList<>();
        List<FileElement> elements = section.getElements();
        for (int i = 0; i < elements.size(); i++) {
            FileElement element = elements.get(i);
            // FileSection extends from SimpleFileLine
            if (!(element instanceof FileSection)) {
            } else {
                FileSection sec = (FileSection) element;
                String content = sec.getLineContent().toLowerCase();
                if (content.startsWith("if ")) {
                    String toParse = content.substring(3, content.length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, false);
                    if (booleanExpression == null) {
                        error("Can't understand this condition : " + toParse);
                        continue;
                    }
                    items.add(new Conditional(sec, booleanExpression, Conditional.ConditionalMode.IF));
                } else if (content.startsWith("else if ")) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        error("An 'else if' must be placed right after an 'if' or another 'else if'");
                    }
                    String toParse = content.substring(8, content.length());
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

                }
            }
        }
        return items;
    }

    private static void error(String s) {
        // TODO get started with this thing
    }
}
