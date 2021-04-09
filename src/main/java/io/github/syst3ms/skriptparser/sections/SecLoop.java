package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.control.Continuable;
import io.github.syst3ms.skriptparser.lang.control.SelfReferencing;
import io.github.syst3ms.skriptparser.lang.lambda.ArgumentSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Optional;

/**
 * This section iterates over all the values of the given expression, one by one.
 * One can also loop a certain amount of times instead. The looped expression will still be
 * valid in that case.
 *
 * @name Loop
 * @type SECTION
 * @pattern loop %integer% times
 * @pattern loop %objects%
 * @since ALPHA
 * @author Mwexim
 */
public class SecLoop extends ArgumentSection implements Continuable, SelfReferencing {
	static {
		Parser.getMainRegistration().addSection(
				SecLoop.class,
				"loop %integer% times",
				"loop %objects%"
		);
	}

	@Nullable
	private Expression<?> expression;
	private Expression<BigInteger> times;
	private boolean isNumericLoop;

	@Nullable
	private Statement actualNext;
	@Nullable
	private Iterator<?> iterator;

	@Override
	public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
		super.setNext(this);
		return super.loadSection(section, parserState, logger);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		isNumericLoop = matchedPattern == 0;
		if (isNumericLoop) {
			times = (Expression<BigInteger>) expressions[0];
			// We can do some certainty checks with Literals.
			if (times instanceof Literal<?>) {
				var t = ((Optional<BigInteger>) ((Literal<BigInteger>) times).getSingle()).orElse(BigInteger.ONE);
				if (t.intValue() <= 0) {
					parseContext.getLogger().error("Cannot loop a negative or zero amount of times", ErrorType.SEMANTIC_ERROR);
					return false;
				} else if (t.intValue() == 1) {
					parseContext.getLogger().error(
							"Cannot loop a single time",
							ErrorType.SEMANTIC_ERROR,
							"Remove this loop, because looping something once can be achieved without a loop-statement"
					);
					return false;
				}
			}
		} else {
			expression = expressions[0];
			if (expression.isSingle()) {
				parseContext.getLogger().error(
						"Cannot loop a single value",
						ErrorType.SEMANTIC_ERROR,
						"Remove this loop, because you clearly don't need to loop a single value"
				);
				return false;
			}
		}
		return true;
	}

	@Override
	public Optional<? extends Statement> walk(TriggerContext ctx) {
		if (isNumericLoop && expression == null) {
			// We just set the looped expression to a range from 1 to the amount of times.
			// This allows the usage of 'loop-number' to get the current iteration
			expression = rangeOf(ctx, times);
		}

		if (iterator == null) {
			assert expression != null;
			iterator = expression instanceof Variable ? ((Variable<?>) expression).variablesIterator(ctx) : expression.iterator(ctx);
		}

		if (iterator.hasNext()) {
			setArguments(iterator.next());
			return start();
		} else {
			finish();
			return Optional.ofNullable(actualNext);
		}
	}

	@Override
	public Statement setNext(@Nullable Statement next) {
		this.actualNext = next;
		return this;
	}

	@Override
	public void finish() {
		// Cache clearing
		iterator = null;
	}

	@Override
	public Optional<? extends Statement> getContinued(TriggerContext ctx) {
		return Optional.of(this);
	}

	@Override
	public Optional<Statement> getActualNext() {
		return Optional.ofNullable(actualNext);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		assert expression != null;
		return "loop " + (isNumericLoop ? times.toString(ctx, debug) + " times" : expression.toString(ctx, debug));
	}

	/**
	 * @return the expression whose values this loop is iterating over
	 */
	public Expression<?> getLoopedExpression() {
		if (isNumericLoop && expression == null) {
			expression = rangeOf(TriggerContext.DUMMY, times);
		}
		return expression;
	}

	/**
	 * Returns a SimpleLiteral containing the numbers 1 up until a certain amount, specified
	 * by the given expression.
	 * @param ctx the context
	 * @param size the expression
	 * @return the SimpleLiteral
	 */
	private static Expression<BigInteger> rangeOf(TriggerContext ctx, Expression<BigInteger> size) {
		BigInteger[] range = (BigInteger[]) size.getSingle(ctx)
				.filter(t -> t.compareTo(BigInteger.ZERO) > 0)
				.map(t -> Ranges.getRange(BigInteger.class).orElseThrow()
						.getFunction()
						.apply(BigInteger.ONE, t)) // Upper bound is inclusive
				.orElse(new BigInteger[0]);
		return new SimpleLiteral<>(BigInteger.class, range);
	}
}
