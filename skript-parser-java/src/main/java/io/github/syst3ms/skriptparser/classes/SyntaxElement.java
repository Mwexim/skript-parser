package io.github.syst3ms.skriptparser.classes;

public interface SyntaxElement {
    boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult);

    String toString(boolean debug);
}
