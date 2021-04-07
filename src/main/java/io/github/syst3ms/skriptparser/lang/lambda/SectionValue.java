package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * An expression that is tied to a given {@link ArgumentSection}, and acts as a way to retrieve
 * the arguments passed through an {@link Expression}.
 *
 * @param <S> the type of the section from which to retrieve arguments
 * @param <T> the return value of the expression
 */
public abstract class SectionValue<S extends ArgumentSection, T> implements Expression<T> {
    private S section;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (!preInitialize(expressions, matchedPattern, parseContext)) {
            return false;
        }
        section = Expression.getLinkedSection(parseContext.getParserState(), getSectionClass(), getSelectorFunction())
                .orElse(null);
        if (section == null) {
            var logger = parseContext.getLogger();
            logger.error(
                    "Couldn't find a section linked to the expression '" +
                            toString(TriggerContext.DUMMY, logger.isDebug()) +
                            "'",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        return postInitialize(section, parseContext);
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        return getSectionValues(section, ctx);
    }

    /**
     * This method is run before the section linked to this {@code SectionValue} is identified, and should be used
     * to initialize fields and other class data using the usual parameters of the {@link SyntaxElement#init} function.
     *
     * @param expressions an array of expressions representing all the expressions that are being passed
     *                    to this {@code SectionValue}. As opposed to Skript, elements of this array can't be {@code null}.
     * @param matchedPattern the index of the pattern that was successfully matched. It corresponds to the order of
     *                       the syntaxes in registration
     * @param parseContext an object containing additional information about the parsing of this {@code SectionValue},
     *                     like regex matches and parse marks
     * @return {@code true} if the {@code SectionValue} was pre-initialized successfully, {@code false} otherwise.
     * @see SyntaxElement#init
     */
    public abstract boolean preInitialize(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext);

    /**
     * This method is run after the section linked to this {@code SectionValue} is identified, and should be used to
     * make some additional verifications/setup operations on the linked {@link ArgumentSection} that was identified.
     *
     * By default, this function always returns {@code true} with no added operations.
     *
     * @param section the section that was identified as corresponding to this {@code SectionValue}
     * @param parseContext the parse context used previously
     * @return {@code true} if the {@code SectionValue} was post-initialized successfully, {@code false} otherwise.
     */
    public boolean postInitialize(S section, ParseContext parseContext) {
        return true;
    }

    /**
     * Returns the selector function for this {@code SectionValue}.
     *
     * This function is supplied with a list of all the sections of the type described by {@link #getSectionClass()},
     * and returns an Optional describing the section that this {@code SectionValue} should be linked to, or an empty
     * Optional if no matching section was found. This is useful for targeting a specific section out of multiple
     * surrounding ones based on criteria specific to the implementation.
     *
     * By default, this always picks the first matching function (i.e the innermost one), if there is one.
     *
     * @return the selector function for this {@code SectionValue}.
     */
    public Function<? super List<? extends S>, Optional<? extends S>> getSelectorFunction() {
        return l -> l.stream().findFirst();
    }

    /**
     * Returns the values of this {@code SectionValue}, akin to the output of {@link Expression#getValues}, given
     * the linked section and the {@link TriggerContext}.
     *
     * @param section the linked section
     * @param ctx the {@link TriggerContext}
     * @return the values of this {@code SectionValue}
     */
    public abstract T[] getSectionValues(S section, TriggerContext ctx);

    /**
     * @return the class of the {@link ArgumentSection} this {@code SectionValue} is linked to.
     */
    public abstract Class<? extends S> getSectionClass();
}
