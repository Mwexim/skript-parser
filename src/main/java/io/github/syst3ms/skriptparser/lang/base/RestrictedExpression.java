package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Collections;
import java.util.List;

public abstract class RestrictedExpression<T> implements Expression<T> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        List<Class<? extends CodeSection>> requiredSections = getRequiredSections();
        List<CodeSection> currentSections = parseContext.getParserState().getCurrentSections();
        int limit = isStrict() ? 1 : currentSections.size();
        CodeSection required = null;
        for (int i = 0; i < limit; i++) {
            CodeSection sec = currentSections.get(i);
            if (requiredSections.contains(sec.getClass())) {
                required = sec;
                break;
            }
        }
        if (required == null) {
            SkriptLogger logger = parseContext.getLogger();
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
