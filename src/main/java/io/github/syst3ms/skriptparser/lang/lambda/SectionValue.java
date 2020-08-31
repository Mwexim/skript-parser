package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class SectionValue<S extends ArgumentSection, T> implements Expression<T> {
    private S section;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (!initialize(expressions, matchedPattern, parseContext)) {
            return false;
        }
        var logger = parseContext.getLogger();
        section = Expression.getLinkedSection(parseContext.getParserState(), getSectionClass(), getSelectorFunction())
                .orElse(null);
        if (section == null) {
            logger.error(
                    "Couldn't find a section linked to the expression '" +
                            toString(null, logger.isDebug()) +
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

    public abstract boolean initialize(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext);

    public boolean postInitialize(S section, ParseContext parseContext) {
        return true;
    }

    public Function<? super List<? extends S>, Optional<? extends S>> getSelectorFunction() {
        return l -> l.stream().findFirst();
    }

    public abstract T[] getSectionValues(S section, TriggerContext ctx);

    public abstract Class<? extends S> getSectionClass();

}
