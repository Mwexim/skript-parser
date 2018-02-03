package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.PatternType;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.registration.Type;
import io.github.syst3ms.skriptparser.registration.TypeManager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SyntaxParser {
	private static final LinkedList<SyntaxInfo<? extends Effect>> recentEffects = new LinkedList<>();
	private static final LinkedList<SyntaxInfo<? extends CodeSection>> recentSections = new LinkedList<>();
	private static final LinkedList<ExpressionInfo<?, ?>> recentExpressions = new LinkedList<>();

	public static <T> Expression<? extends T> parseExpression(String s, PatternType<T> expectedType) { // empty implementation
		Map<Class<?>, Type<?>> classToTypeMap = TypeManager.getInstance().getClassToTypeMap();
		for (Class<?> c : classToTypeMap.keySet()) {
            Class<T> typeClass = expectedType.getType().getTypeClass();
            if (typeClass.isAssignableFrom(c)) {
				Function<String, ?> literalParser = classToTypeMap.get(c).getLiteralParser();
				if (literalParser != null) {
					T literal = (T) literalParser.apply(s);
					if (literal != null) {
						return new Literal<>(typeClass, literal);
					}
				}
			}
		}
		for (ExpressionInfo<?, ?> info : recentExpressions) {
			Expression<? extends T> expression = matchExpressionInfo(s, info, expectedType);
			if (expression != null) {
				recentExpressions.removeFirstOccurrence(info);
				recentExpressions.addFirst(info);
				return expression;
			}
		}
		// Let's not loop over the same elements again
		Collection<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getInstance().getAllExpressions();
		remainingExpressions.removeAll(recentExpressions);
		for (ExpressionInfo<?, ?> info : remainingExpressions) {
			Expression<? extends T> expression = matchExpressionInfo(s, info, expectedType);
			if (expression != null) {
				recentExpressions.removeFirstOccurrence(info);
				recentExpressions.addFirst(info);
				return expression;
			}
		}
		return null;
	}

	/**
	 * Tries to match an {@link ExpressionInfo} against the given {@link String} expression.
	 * Made for DRY purposes inside of {@link #parseExpression(String, PatternType)}
	 * @param <T> The return type of the {@link Expression}
	 * @return the Expression instance of matching, or {@literal null} otherwise
	 */
	private static <T> Expression<? extends T> matchExpressionInfo(String s, ExpressionInfo<?, ?> info, PatternType<T> expectedType) {
		List<PatternElement> patterns = info.getPatterns();
		for (int i = 0; i < patterns.size(); i++) {
            Type<?> returnType = info.getReturnType();
            if (!returnType.getTypeClass().isAssignableFrom(expectedType.getType().getTypeClass())) {
                continue;
            }
			PatternElement element = patterns.get(i);
			SkriptParser parser = new SkriptParser(element);
			if (element.match(s, 0, parser) != -1) {
				try {
					Expression<? extends T> expression = (Expression<? extends T>) info.getSyntaxClass().newInstance();
					expression.init(
						parser.getParsedExpressions().toArray(new Expression[0]),
						i,
						parser.toParseResult()
					);
					return expression;
				} catch (InstantiationException | IllegalAccessException e) {
					error("Parsing of " + info.getSyntaxClass()
											  .getSimpleName() + "succeeded, but it couldn't be instantiated");
					continue;
				}
			}
		}
		error("Can't understand the expression : '" + s + "'");
		return null;
	}

	private static void error(String s) {
		// TODO
	}
}
