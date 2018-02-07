package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.SimpleFileLine;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;

import java.util.ArrayList;
import java.util.List;

public class ScriptLoader {

    public static List<Effect> loadItems(FileSection section) {
        List<Effect> effects = new ArrayList<>();
        List<FileElement> elements = section.getElements();
        for (FileElement element : elements) {
            if (element instanceof SimpleFileLine) {

            } else {
                String content = element.getLineContent().toLowerCase();
                if (content.startsWith("if ")) {
                    String toParse = content.substring(3, content.length());
                    Expression<? extends Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, false);
                    if (booleanExpression == null) {
                        error("Can't understand this condition : " + toParse);
                        continue;
                    }

                }
            }
        }
        return effects;
    }

    private static void error(String s) {
        // TODO get started with this thing
    }
}
