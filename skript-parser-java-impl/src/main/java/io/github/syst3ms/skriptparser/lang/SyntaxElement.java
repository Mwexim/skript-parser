package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseResult;

public interface SyntaxElement {
    boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult);

    String toString(boolean debug);
}
