package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

public interface SyntaxElement {

    boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult);

    String toString(@Nullable Event e, boolean debug);
}
