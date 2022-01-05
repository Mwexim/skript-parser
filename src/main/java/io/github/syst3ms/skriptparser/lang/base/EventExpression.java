package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.registration.context.ContextValues;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One can use this as a utility class for a {@linkplain Type#getDefaultExpression() default expression}.
 * This expression class holds a reference to a specific {@link ContextValue} based on the required
 * return type and whether or not the result must be a single value. A runtime exception will be thrown
 * if such context value does not exist.
 * <br>
 * Note that the {@linkplain ContextValue.State state} of the referenced context value must be 'present'.
 * @param <T> the Expression's type
 * @author Mwexim
 */
public class EventExpression<T> implements Expression<T> {
	private final PatternType<? extends T> returnType;
	private final List<ContextValue<? extends TriggerContext, T>> contextValues = new ArrayList<>();

	public EventExpression(Class<? extends T> returnType, boolean isSingle) {
		this.returnType = new PatternType<>(TypeManager.getByClassExact(returnType).orElseThrow(IllegalArgumentException::new), isSingle);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseContext.getParserState().getCurrentContexts().stream()
				.map(ctx -> ContextValues.getContextValues(ctx).stream()
						.filter(info -> returnType.getType().getTypeClass().isAssignableFrom(info.getReturnType().getType().getTypeClass()))
						.filter(info -> !returnType.isSingle() || info.getReturnType().isSingle())
						.filter(info -> info.getState() == ContextValue.State.PRESENT)
						.collect(Collectors.toList()))
				.map(List.class::cast)
				.forEach(contextValues::addAll);
		if (contextValues.size() == 0) {
			parseContext.getLogger().error("Couldn't find "
							+ returnType.getType().withIndefiniteArticle(returnType.isSingle())
							+ " as default expression in this event",
					ErrorType.NO_MATCH);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] getValues(TriggerContext ctx) {
		var info = (ContextValue<TriggerContext, T>) contextValues.stream()
				.filter(val -> val.getContext().isAssignableFrom(ctx.getClass()))
				.findFirst()
				.orElseThrow(() -> new SkriptRuntimeException("Couldn't find any context value corresponding to the class '" + ctx.getClass().getName() + "'"));
		return info.getFunction().apply(ctx);
	}

	@Override
	public boolean isSingle() {
		return returnType.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType.getType().getTypeClass();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		var info = (ContextValue<TriggerContext, T>) contextValues.stream()
				.filter(val -> val.getContext().isAssignableFrom(ctx.getClass()))
				.findFirst().orElse(null);
		if (info != null) {
			return new String[] {"past ", "", "future "}[info.getState().ordinal()]
					+ (info.getUsage().isCorrect(true) ? "" : "context-")
					+ info.getReturnType().getType().withIndefiniteArticle(returnType.isSingle());
		} else {
			return TypeManager.NULL_REPRESENTATION;
		}
	}
}