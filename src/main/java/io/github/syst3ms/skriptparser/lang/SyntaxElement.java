package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * The base class for all elements that are described by a syntax
 * @see Statement
 * @see Expression
 * @see CodeSection
 */
public interface SyntaxElement {
    /**
     * Initialises this SyntaxElement before being used. This method is always called before all the others in
     * an extending class, the only exception being {@link CodeSection#loadSection(FileSection, ParserState, SkriptLogger)}.
     * @param expressions an array of expressions representing all the expressions that are being passed
     *                    to this syntax element. As opposed to Skript, elements of this array can't be {@code null}.
     * @param matchedPattern the index of the pattern that was successfully matched. It corresponds to the order of
     *                       the syntaxes in registration
     * @param parseContext an object containing additional information about the parsing of this syntax element, like
     *                    regex matches and parse marks
     * @return {@code true} if the syntax element was initialized successfully, {@code false} otherwise.
     * @see SkriptRegistration
     * @see ParseContext
     */
    boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext);

    /**
     * @param ctx the event
     * @param debug whether to show additional information or not
     * @return a {@link String} that should aim to resemble what is written in the script as closely as possible
     */
    String toString(@Nullable TriggerContext ctx, boolean debug);

    /**
     * Checks whether this syntax element is inside of specific given {@link CodeSection}s.
     *
     * This method shouldn't be used for {@linkplain SyntaxElement}s that should only work with specific {@link TriggerContext}s.
     * For this purpose, prefer {@link ParseContext#getParserState()} used in conjunction with {@link ParserState#getCurrentContexts()}.
     * @param parseContext the parser context
     * @param isStrict true if the required section has to be the one directly enclosing this SyntaxElement
     * @param requiredSections a list of the classes of all the {@link CodeSection}s this SyntaxElement should be restricted to
     * @return whether this Syntax element is in a given {@link CodeSection} or not
     * @see ParserState#getCurrentContexts()
     */
    @SafeVarargs
    static boolean checkIsInSection(ParseContext parseContext, boolean isStrict, Class<? extends CodeSection>... requiredSections) {
        var currentSections = parseContext.getParserState().getCurrentSections();
        var sections = Arrays.asList(requiredSections);
        var limit = isStrict ? 1 : currentSections.size();
        for (var i = 0; i < limit; i++) {
            var sec = currentSections.get(i);
            if (sections.contains(sec.getClass())) {
                return true;
            }
        }
        return false;
    }
}
