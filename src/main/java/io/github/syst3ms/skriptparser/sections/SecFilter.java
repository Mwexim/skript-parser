package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.effects.EffReturn;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.lambda.ReturnSection;
import io.github.syst3ms.skriptparser.lang.lambda.SkriptFunction;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class SecFilter extends ReturnSection<Boolean> {
    private Expression<?> filtered;
    private SkriptFunction<SecFilter, Boolean> lambda;

    static {
        Parser.getMainRegistration().addSection(
                SecFilter.class,
                "filter %~objects%"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        filtered = expressions[0];
        var logger = parseContext.getLogger();
        if (filtered.acceptsChange(ChangeMode.SET).isEmpty()) {
            logger.error(
                    "The expression '" +
                            filtered.toString(null, logger.isDebug()) +
                            "' cannot be changed",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        } else if (filtered.acceptsChange(ChangeMode.SET)
                .filter(cl -> CollectionUtils.contains(cl, filtered.getReturnType()))
                .isEmpty()) {
            // Why this would happen is beyond me, but it's worth checking regardless
            logger.error(
                    "The expression '" +
                            filtered.toString(null, logger.isDebug()) +
                            "' cannot be changed with values of its own type",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        lambda = SkriptFunction.create(this);
        return true;
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        var currentLine = logger.getLine();
        return super.loadSection(section, parserState, logger) && checkReturns(logger, currentLine, true);
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        var filteredValues = Arrays.stream(filtered.getValues(ctx))
                .filter(v -> lambda.apply(ctx, v)
                        .filter(a -> a.length == 1)
                        .map(a -> a[0])
                        .orElse(false)
                )
                .toArray();
        filtered.change(ctx, filteredValues, ChangeMode.SET);
        return getNext();
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "filter " + filtered.toString(ctx, debug);
    }
}
