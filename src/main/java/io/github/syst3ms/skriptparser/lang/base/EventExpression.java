package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.registration.context.ContextValues;
import io.github.syst3ms.skriptparser.types.Type;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
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
	private final Class<? extends T> returnType;
	private final boolean isSingle;
	private ContextValue<TriggerContext, T> info;

	public EventExpression(Class<? extends T> returnType, boolean isSingle) {
		this.returnType = returnType;
		this.isSingle = isSingle;
	}

	@Override
	@Contract("_, _, _ -> fail")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T[] getValues(TriggerContext ctx) {
		if (info == null)
			info = getContextValue(ctx, returnType, isSingle);
		return info.getFunction().apply(ctx);
	}

	@Override
	public boolean isSingle() {
		return isSingle;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		if (info == null)
			info = getContextValue(ctx, returnType, isSingle);
		return new String[] {"past ", "", "future "}[info.getState().ordinal()]
				+ (info.getUsage().isCorrect(true) ? "" : "context-")
				+ info.getReturnType().getType().withIndefiniteArticle(isSingle);
	}

	@SuppressWarnings("unchecked")
	private static <T> ContextValue<TriggerContext, T> getContextValue(TriggerContext ctx, Class<? extends T> returnType, boolean isSingle) {
		var possibilities = ContextValues.getContextValues(ctx.getClass()).stream()
				.filter(info -> returnType.isAssignableFrom(info.getReturnType().getType().getTypeClass()))
				.filter(info -> !isSingle || info.getReturnType().isSingle())
				.filter(info -> info.getState() == ContextValue.State.PRESENT)
				.collect(Collectors.toList());
		if (possibilities.size() > 1)
			throw new SkriptRuntimeException("Expected exactly one match, but found multiple " + (isSingle ? "" : "non-") + "single context values corresponding to the class '" + returnType.getName() + "'");
		return Optional.ofNullable(possibilities.get(0))
				.map(info -> (ContextValue<TriggerContext, T>) info)
				.orElseThrow(() -> new SkriptRuntimeException("Couldn't find a" + (isSingle ? "" : "non-") + "single context value corresponding to the class '" + returnType.getName() + "'"));
	}
}