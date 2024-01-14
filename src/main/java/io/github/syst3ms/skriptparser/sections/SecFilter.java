package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.control.SelfReferencing;
import io.github.syst3ms.skriptparser.lang.lambda.ReturnSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * This section filters the given input values one by one, deciding whether to keep each argument
 * or not by the returned boolean.
 * Note that the filtered expression will be changed, hence why it can't be a literal list.
 *
 * @name Filter
 * @type SECTION
 * @pattern filter %~objects%
 * @since ALPHA
 * @author Mwexim
 */
public class SecFilter extends ReturnSection<Boolean> implements SelfReferencing {
    static {
        Parser.getMainRegistration().addSection(
                SecFilter.class,
                "filter %~objects%"
        );
        /**
         * filter {_test::*}:
         *     return type of input is pig
         */
    }

    private Expression<?> filtered;

    @Nullable
    private Statement actualNext;
    @Nullable
    private Iterator<?> iterator;
    private final List<Object> result = new ArrayList<>();

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        filtered = expressions[0];
        var logger = parseContext.getLogger();
        if (!filtered.acceptsChange(ChangeMode.SET, filtered.getReturnType(), false)
                || filtered.acceptsChange(ChangeMode.DELETE).isEmpty()) {
            logger.error(
                    "The expression '" +
                            filtered.toString(TriggerContext.DUMMY, logger.isDebug()) +
                            "' cannot be changed",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        return true;
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        var currentLine = logger.getLine();
        super.setNext(this);
        return super.loadSection(section, parserState, logger) && checkReturns(logger, currentLine, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        boolean isVariable = filtered instanceof Variable<?>;
        if (iterator == null)
            iterator = isVariable
                    ? ((Variable<?>) filtered).variablesIterator(ctx)
                    : filtered.iterator(ctx);

        if (iterator.hasNext()) {
            setArguments(isVariable
                    ? ((Pair<String, Object>) iterator.next()).getSecond()
                    : iterator.next()
            );
            return start();
        } else {
            if (result.size() == 0) {
                filtered.change(ctx, ChangeMode.DELETE, new Object[0]);
            } else {
                filtered.change(ctx, ChangeMode.SET, result.toArray());
            }
            finish();
            return Optional.ofNullable(actualNext);
        }
    }

    @Override
    public void step(Statement item) {
        if (getReturned().map(val -> val[0]).orElse(false)) {
            assert getArguments().length == 1;
            result.add(getArguments()[0]); // We add the filtered argument to the result
        }
    }

    @Override
    public void finish() {
        // Cache clearing
        iterator = null;
        result.clear();
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
    public Statement setNext(@Nullable Statement next) {
        actualNext = next;
        return this;
    }

    @Override
    public Optional<Statement> getActualNext() {
        return Optional.ofNullable(actualNext);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "filter " + filtered.toString(ctx, debug);
    }
}
