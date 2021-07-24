package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Map;

/**
 * All indices of a given list variable.
 *
 * @name Variable Indices
 * @type EXPRESSION
 * @pattern [all [of] [the]] ind(exes|ices) of %^objects%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprVariableIndices implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprVariableIndices.class,
				String.class,
				false,
				"[all [[of] the]] ind(exes|ices) of %^objects%"
		);
	}

	private Variable<Object> value;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		value = (Variable<Object>) expressions[0];
		if (value.isSingle()) {
			var logger = parseContext.getLogger();
			logger.error(
					"Single variables are not allowed, found '" + value.toString(TriggerContext.DUMMY, logger.isDebug()) + "'",
					ErrorType.SEMANTIC_ERROR
			);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String[] getValues(TriggerContext ctx) {
		return value.getRaw(ctx)
				.map(val -> ((Map<String, Object>) val).keySet().toArray(new String[0]))
				.orElse(new String[0]);

	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "indices of " + value.toString(ctx, debug);
	}
}
