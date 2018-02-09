package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.SkriptParser;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.ExpressionElement;
import io.github.syst3ms.skriptparser.pattern.TextElement;

import java.util.Collections;

public class ExprWhether implements Expression<Boolean> {
	private Expression<Boolean> condition;

	static {
		SkriptParser.setWhetherPattern(
			new CompoundElement(
				new TextElement("whether "),
				new ExpressionElement(Collections.singletonList(SyntaxParser.BOOLEAN_PATTERN_TYPE), ExpressionElement.Acceptance.EXPRESSIONS_ONLY, false)
			)
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		condition = (Expression<Boolean>) expressions[0];
		return true;
	}

	@Override
	public Boolean[] getValues(Event e) {
		return condition.getValues(e);
	}

	@Override
	public String toString(Event e, boolean debug) {
		return "whether " + condition.toString(e, debug);
	}
}
