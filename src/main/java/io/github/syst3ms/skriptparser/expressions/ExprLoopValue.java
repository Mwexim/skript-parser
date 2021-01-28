package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.lang.lambda.SectionValue;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The currently looped value.
 *
 * @name Loop Value
 * @pattern [the] loop-<.+>
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprLoopValue extends SectionValue<SecLoop, Object> {
	private SecLoop loop;
	private boolean isVariableLoop;
	private boolean isIndex;
	@Nullable
	private Class<?> loopedClass;
	private String loopedString;
	private int discriminant;

	static {
		Parser.getMainRegistration().addExpression(
			ExprLoopValue.class,
			Object.class,
			true,
			"[the] loop-<.+>"
		);
	}

	@Override
	public boolean preInitialize(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		loopedString = parseContext.getMatches().get(0).group();
		discriminant = -1;
		final Matcher m = Pattern.compile("^(.+)-(\\d+)$").matcher(loopedString);
		if (m.matches()) {
			loopedString = m.group(1);
			discriminant = Integer.parseInt(m.group(2));
		}
		loopedClass = TypeManager.getPatternType(loopedString)
				.map(patternType -> patternType.getType().getTypeClass())
				.orElse(null);
		return true;
	}

	@Override
	public Function<? super List<? extends SecLoop>, Optional<? extends SecLoop>> getSelectorFunction() {
		return sections -> {
			int j = 1;
			SecLoop loop = null;
			for (SecLoop l : sections) {
				Class<?> loopedType = l.getLoopedExpression().getReturnType();
				if (loopedClass != null && (loopedClass.isAssignableFrom(loopedType) || loopedType == Object.class) ||
						"value".equals(loopedString) ||
						l.getLoopedExpression().isLoopOf(loopedString)) {
					if (j < discriminant) {
						j++;
						continue;
					}
					if (loop != null) {
						return Optional.empty();
					}
					loop = l;
					if (j == discriminant)
						break;
				}
			}
			if (loop == null) {
				return Optional.empty();
			}
			if (loop.getLoopedExpression() instanceof Variable) {
				isVariableLoop = true;
				if (((Variable<?>) loop.getLoopedExpression()).isIndexLoop(loopedString))
					isIndex = true;
			}
			this.loop = loop;
			return Optional.of(loop);
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> Optional<? extends Expression<R>> convertExpression(Class<R> to) {
		if (isVariableLoop && !isIndex) {
			return Optional.of(new ConvertedExpression<>(this, (Class<R>) ClassUtils.getCommonSuperclass(to), o -> Converters.convert(o, to)));
		} else {
			return super.convertExpression(to);
		}
	}

	@Override
	public Class<?> getReturnType() {
		if (isIndex)
			return String.class;
		return loop.getLoopedExpression().getReturnType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getSectionValues(SecLoop loop, TriggerContext ctx) {
		Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
		if (isVariableLoop) {
			// loop.getArguments() == null -> is this check dangerous?
			if (loop.getArguments() == null || loop.getArguments()[0] == null) {
				return new Object[0];
			}
			var current = (Pair<String, Object>) loop.getArguments()[0];
			if (isIndex) {
				return new String[] {current.getFirst()};
			}
			one[0] = current.getSecond();
			return one;
		}
		one[0] = loop.getArguments()[0];
		return one;
	}

	@Override
	public Class<? extends SecLoop> getSectionClass() {
		return SecLoop.class;
	}

	@Override
	public String toString(TriggerContext ctx, final boolean debug) {
		if (isVariableLoop) {
			@SuppressWarnings("unchecked")
			final Map.Entry<String, Object> current = (Map.Entry<String, Object>) loop.getCurrent(ctx);
			if (current == null)
				return TypeManager.NULL_REPRESENTATION;
			return isIndex ? "\"" + current.getKey() + "\"" : TypeManager.toString(current.getValue());
		}
		return TypeManager.toString(loop.getCurrent(ctx));
	}

}
