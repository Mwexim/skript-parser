package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

/**
 * The base class for all elements that are described by a syntax
 * @see Effect
 * @see Expression
 * @see CodeSection
 */
public interface SyntaxElement {
    /**
     * Initialises this SyntaxElement before being used. This method is always called before all the others in
     * an extending class, the only exception being {@link CodeSection#loadSection(FileSection)}.
     * @param expressions an array of expressions representing all the expressions that are being passed
     *                    to this syntax element. As opposed to Skript, elements of this array can't be {@code null}.
     * @param matchedPattern the index of the pattern that was successfully matched. It corresponds to the order of
     *                       the syntaxes in registration
     * @param parseResult an object containing additional information about the parsing of this syntax element, like
     *                    regex matches and parse marks
     * @return {@code true} if the syntax element was initialized successfully, {@code false} otherwise.
     * @see io.github.syst3ms.skriptparser.registration.SkriptRegistration
     * @see ParseResult
     */
    boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult);

    /**
     * @param e the event
     * @param debug whether to show additional information or not
     * @return a {@link String} that should aim to resemble what is written in the script as closely as possible
     */
    String toString(@Nullable TriggerContext e, boolean debug);
}
