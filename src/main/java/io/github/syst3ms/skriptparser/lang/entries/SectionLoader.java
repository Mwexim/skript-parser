package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.SimpleCodeSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;

public class SectionLoader extends EntryLoader {
	public SectionLoader(String key, boolean optional) {
		super(key, optional);
	}

	@Override
	public boolean loadEntry(SectionConfiguration config, FileElement element, ParserState parserState, SkriptLogger logger) {
		if (!element.getLineContent().equalsIgnoreCase(this.key))
			return false;
		if (!(element instanceof FileSection)) {
			logger.error("The entry '" + key + "' has been configured incorrectly.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		var entry = new SimpleCodeSection((FileSection) element, parserState, logger, element.getLineContent());
		if (config.getParent() != null)
			entry.setParent(config.getParent());
		config.getData().put(key, entry);
		return true;
	}
}
