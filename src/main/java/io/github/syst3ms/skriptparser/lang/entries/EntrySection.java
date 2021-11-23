package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

public class EntrySection extends CodeSection {
	private final String name;

	public EntrySection(String name) {
		this.name = name;
	}

	@Override
	@Contract("_, _, _ -> fail")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<? extends Statement> walk(TriggerContext ctx) {
		return getFirst();
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return name;
	}
}
