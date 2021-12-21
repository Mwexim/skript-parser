package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

public class SimpleCodeSection extends CodeSection {
	private final String name;

	public SimpleCodeSection(FileSection section, ParserState parserState, SkriptLogger logger, String name) {
		this.name = name;
		loadSection(section, parserState, logger);
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
