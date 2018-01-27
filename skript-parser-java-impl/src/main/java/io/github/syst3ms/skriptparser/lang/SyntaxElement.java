package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseResult;

import java.util.LinkedList;

public interface SyntaxElement {
    LinkedList<Effect> recentElements = new LinkedList<>();

    boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult);

    String toString(boolean debug);
}
