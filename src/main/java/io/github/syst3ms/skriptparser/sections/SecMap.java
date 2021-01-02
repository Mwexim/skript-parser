package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class SecMap extends ReturnSection<Object> {
    private Expression<?> mapped;
    private SkriptFunction<?, ?> mapper;

    static {
        Parser.getMainRegistration().addSection(
                SecMap.class,
                "map %~objects%"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        mapped = expressions[0];
        var logger = parseContext.getLogger();
        if (mapped.acceptsChange(ChangeMode.SET).isEmpty()) {
            logger.error(
                    "The expression '" +
                            mapped.toString(null, logger.isDebug()) +
                            "' cannot be changed",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        mapper = SkriptFunction.create(this);
        return true;
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        var currentLine = logger.getLine();
        return super.loadSection(section, parserState, logger) && checkReturns(logger, currentLine, true);
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Object[] mappedValues = Arrays.stream(mapped.getValues(ctx))
                .map(e -> mapper.apply(ctx, e)
                        .filter(a -> a.length == 1)
                        .map(a -> a[0])
                        .orElse(null)
                )
                .toArray();
        mapped.change(ctx, mappedValues, ChangeMode.SET);
        return getNext();
    }

    @Override
    public Class<?> getReturnType() {
        return mapped.getReturnType();
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "map " + mapped.toString(ctx, debug);
    }
}
