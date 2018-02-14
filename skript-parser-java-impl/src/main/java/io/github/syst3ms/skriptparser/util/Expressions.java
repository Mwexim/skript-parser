package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.lang.interfaces.ConvertibleExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.LoopableExpression;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.types.ConvertedExpression;

import java.util.Collection;
import java.util.Iterator;

public class Expressions {

	public static <T> Class<?> getReturnType(Expression<? extends T> expr) {
		if (expr instanceof Literal) {
			return ((Literal<?>) expr).getReturnType();
		} else if (expr instanceof Variable) {
			return ((Variable<?>) expr).getReturnType();
		} else if (expr instanceof VariableString) {
			return String.class;
		} else if (expr instanceof ConvertedExpression) {
			return ((ConvertedExpression) expr).getReturnType();
		} else {
			ExpressionInfo info = getExpressionExact(expr);
			if (info == null) {
				assert false;
				return Object.class;
			}
			return info.getReturnType().getType().getTypeClass();
		}
	}

	public static ExpressionInfo<?, ?> getExpressionExact(Expression<?> expr) {
		Class<?> c;
		if (expr instanceof ConvertedExpression) {
			c = ((ConvertedExpression) expr).getSource().getClass();
		} else {
			c = expr.getClass();
		}
		for (ExpressionInfo<?, ?> info : SyntaxManager.getAllExpressions()) {
			if (info.getSyntaxClass() == c) {
				return info;
			}
		}
		return null;
	}

	public static boolean isSingle(Expression<?> expression) {
		if (expression instanceof DynamicNumberExpression) {
			return ((DynamicNumberExpression) expression).isSingle();
		} else {
			for (ExpressionInfo<?, ?> info : SyntaxManager.getAllExpressions()) {
				if (info.getSyntaxClass() == expression.getClass()) {
					return info.getReturnType().isSingle();
				}
			}
			throw new SkriptParserException("Unregistered expression class : " + expression.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Iterator<? extends T> iterator(Expression<T> expr, Event e) {
		if (expr instanceof LoopableExpression) {
			return ((LoopableExpression<T>) expr).iterator(e);
		} else {
			return CollectionUtils.iterator(expr.getValues(e));
		}
	}

	public static <F, T> Expression<?> convertExpression(Expression<F> expression, Class<T>... to) {
		if (expression instanceof ConvertibleExpression) {
			return ((ConvertibleExpression) expression).getConvertedExpression(to);
		} else {
			return ConvertedExpression.newInstance(expression, to);
		}
	}
}
