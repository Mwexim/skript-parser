package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;

import java.util.List;

/**
 * An expression that can only be used in select CodeSections. It is possible to make a RestrictedExpression "strict" ;
 * that is, it would be invalid to use it when not directly enclosed in one of its required sections.
 * It is possible to specify the error message that should be shown if the restrictions aren't followed.
 *
 * This class shouldn't be used for expressions that should only work with specific {@link TriggerContext}s.
 * For this purpose, use {@link ParseContext#getParserState()} in conjuction with {@link ParserState#getCurrentContexts()}.
 * @param <T> the return type
 * @see ParserState#getCurrentContexts()
 */
public abstract class RestrictedExpression<T> implements Expression<T> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        var requiredSections = getRequiredSections();
        var currentSections = parseContext.getParserState().getCurrentSections();
        var limit = isStrict() ? 1 : currentSections.size();
        CodeSection required = null;
        for (var i = 0; i < limit; i++) {
            var sec = currentSections.get(i);
            if (requiredSections.contains(sec.getClass())) {
                required = sec;
                break;
            }
        }
        if (required == null) {
            var logger = parseContext.getLogger();
            logger.error(getSpecificErrorMessage() + " : '" + this.toString(null, logger.isDebug()) + "'", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return initialize(expressions, required, matchedPattern, parseContext);
    }

    protected abstract boolean initialize(Expression<?>[] expressions, CodeSection requiredSection, int matchedPattern, ParseContext parseContext);

    /**
     * The error message that should be displayed if this expression is used outside of one of its required sections
     * @return the error message that should be displayed if this expression is used outside of one of its required sections
     */
    protected abstract String getSpecificErrorMessage();

    /**
     * Returns a list of the classes of all the sections inside of which this expression can be used
     * @return a list of the classes of all the sections inside of which this expression can be used
     */
    protected abstract List<Class<? extends CodeSection>> getRequiredSections();

    /**
     * True if the directly enclosing section must be a required one (a member of {@link #getRequiredSections()}), false otherwise.
     * @return whether the directly enclosing section must be a required one or not
     */
    protected abstract boolean isStrict();
}
