package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;

public abstract class EntryLoader {
	protected final String key;
	private final boolean optional;

	public EntryLoader(String key, boolean optional) {
		this.key = key;
		this.optional = optional;
	}

	/**
	 * This {@link EntryLoader} will attempt to load the entry
	 * using its {@linkplain FileElement}. One can use this method
	 * to create specific error messages or to load the value correctly.
	 * @param config the configuration
	 * @param element the element
	 * @param parserState the parser state
	 * @param logger the logger
	 * @return {@code true} if loaded successfully, {@code false} if an error occurred
	 */
	public abstract boolean loadEntry(SectionConfiguration config, FileElement element, ParserState parserState, SkriptLogger logger);

	public boolean isOptional() {
		return optional;
	}
}
