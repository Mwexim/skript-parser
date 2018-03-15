package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Loop;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExprLoopValue implements Expression<Object> {
	@SuppressWarnings("null")
	private String name;
	@SuppressWarnings("null")
	private Loop loop;
	private boolean isVariableLoop;
	private boolean isIndex;

	static {
		Main.getMainRegistration().addExpression(
			ExprLoopValue.class,
			Object.class,
			true,
			6,
			"[the] loop-<.+>"
		);
	}

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, ParseResult parser) {
		name = parser.getExpressionString();
		String s = "" + parser.getMatches().get(0).group();
		int i = -1;
		final Matcher m = Pattern.compile("^(.+)-(\\d+)$").matcher(s);
		if (m.matches()) {
			s = m.group(1);
			i = Integer.parseInt(m.group(2));
		}
		Class<?> c;
		PatternType<?> type = TypeManager.getPatternType(s);
		if (type != null) { // And that, people, is why I like Kotlin
			c = type.getType().getTypeClass();
		} else {
			c = null;
		}
		int j = 1;
		Loop loop = null;

		for (final Loop l : ScriptLoader.getCurrentLoops()) {
			if (c != null && c.isAssignableFrom(l.getLoopedExpression().getReturnType()) ||
				"value".equals(s) ||
				l.getLoopedExpression().isLoopOf(s)) {
				if (j < i) {
					j++;
					continue;
				}
				if (loop != null) {
					return false;
				}
				loop = l;
				if (j == i)
					break;
			}
		}
		if (loop == null) {
			return false;
		}
		if (loop.getLoopedExpression() instanceof Variable) {
			isVariableLoop = true;
			if (((Variable<?>) loop.getLoopedExpression()).isIndexLoop(s))
				isIndex = true;
		}
		this.loop = loop;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> Expression<R> convertExpression(Class<R> to) {
		if (isVariableLoop && !isIndex) {
			return new ConvertedExpression<>(this, (Class<R>) ClassUtils.getCommonSuperclass(to), o -> Converters.convert(o, to));
		} else {
			return Expression.super.convertExpression(to);
		}
	}

	@Override
	public Class<?> getReturnType() {
		if (isIndex)
			return String.class;
		return loop.getLoopedExpression().getReturnType();
	}

	@Override
	public Object[] getValues(Event e) {
		Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
		if (isVariableLoop) {
			@SuppressWarnings("unchecked")
			final Map.Entry<String, Object> current = (Map.Entry<String, Object>) loop.getCurrent(e);
			if (current == null) {
				return new Object[0];
			}
			if (isIndex) {
				return new String[] {current.getKey()};
			}
			one[0] = current.getValue();
			return one;
		}
		one[0] = loop.getCurrent(e);
		return one;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return name;
		if (isVariableLoop) {
			@SuppressWarnings("unchecked")
			final Map.Entry<String, Object> current = (Map.Entry<String, Object>) loop.getCurrent(e);
			if (current == null)
				return TypeManager.NULL_REPRESENTATION;
			return isIndex ? "\"" + current.getKey() + "\"" : TypeManager.toString(current.getValue());
		}
		return TypeManager.toString(loop.getCurrent(e));
	}

}
