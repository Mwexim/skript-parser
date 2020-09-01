package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.lambda.ReturnSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

public class EffReturn extends Effect {
    private ReturnSection<?> section;
    private Expression<?> returned;

    static {
        Main.getMainRegistration().addEffect(
                EffReturn.class,
                "return %objects%"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        returned = expressions[0];
        var logger = parseContext.getLogger();
        var sec = Expression.getLinkedSection(parseContext.getParserState(), CodeSection.class).orElse(null);
        if (sec == null) {
            logger.error("Couldn't find a section matching this return statement", ErrorType.SEMANTIC_ERROR);
            return false;
        } else if (!(sec instanceof ReturnSection)) {
            logger.error(
                    "The closest section matching this return statement doesn't accept return values",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        section = (ReturnSection<?>) sec;
        if (section.isSingle() && !returned.isSingle()) {
            logger.error("Only a single return value was expected, but multiple were given", ErrorType.SEMANTIC_ERROR);
            return false;
        } else if (!Converters.converterExists(returned.getReturnType(), section.getReturnType())) {
            var secType = TypeManager.getByClass(section.getReturnType())
                    .map(t -> StringUtils.withIndefiniteArticle(t.toString(), false))
                    .orElse(section.getReturnType().getName());
            var exprType = TypeManager.getByClass(returned.getReturnType())
                    .map(t -> StringUtils.withIndefiniteArticle(t.toString(), false))
                    .orElseThrow(AssertionError::new);
            logger.error(
                    "Expected " +
                            secType +
                            " return value, but found " +
                            exprType,
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        if (!section.getReturnType().isAssignableFrom(returned.getReturnType())) {
            // The value is convertible but not in the trivial way
            returned = returned.convertExpression(section.getReturnType())
                    .orElseThrow(() -> new SkriptParserException("Return value should be convertible at this stage"));
        }
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        section.setReturned(returned.getValues(ctx));
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "return " + returned.toString(ctx, debug);
    }
}
