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

public class SecMap extends ReturnSection<Object> implements SelfReferencing {
    static {
        Parser.getMainRegistration().addSection(
                SecMap.class,
                "map %~objects%"
        );
    }

	private Expression<?> mapped;

	@Nullable
	private Statement actualNext;
	@Nullable
	private Iterator<?> iterator;
	private final List<Object> result = new ArrayList<>();

	@Override
	public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
		var currentLine = logger.getLine();
		super.setNext(this);
		return super.loadSection(section, parserState, logger) && checkReturns(logger, currentLine, true);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		mapped = expressions[0];
		var logger = parseContext.getLogger();
		if (mapped.acceptsChange(ChangeMode.SET).isEmpty()
				|| mapped.acceptsChange(ChangeMode.DELETE).isEmpty()) {
			logger.error(
					"The expression '" +
							mapped.toString(TriggerContext.DUMMY, logger.isDebug()) +
							"' cannot be changed",
					ErrorType.SEMANTIC_ERROR
			);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<? extends Statement> walk(TriggerContext ctx) {
		boolean isVariable = mapped instanceof Variable<?>;
		if (iterator == null)
			iterator = isVariable
					? ((Variable<?>) mapped).variablesIterator(ctx)
					: mapped.iterator(ctx);

		if (iterator.hasNext()) {
			setArguments(isVariable
					? ((Pair<String, Object>) iterator.next()).getSecond()
					: iterator.next()
			);
			return start();
		} else {
			if (result.size() == 0) {
				mapped.change(ctx, new Object[0], ChangeMode.DELETE);
			} else {
				mapped.change(ctx, result.toArray(), ChangeMode.SET);
			}
			finish();
			return Optional.ofNullable(actualNext);
		}
	}

	@Override
	public void step(Statement item) {
		var returned = getReturned().orElseThrow(AssertionError::new);
		assert getArguments().length == 1;
		assert returned.length == 1;
		result.add(returned[0]); // We add the filtered argument to the result
	}

	@Override
	public void finish() {
		// Cache clearing
		iterator = null;
		result.clear();
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
        return "map " + mapped.toString(ctx, debug);
    }
}
