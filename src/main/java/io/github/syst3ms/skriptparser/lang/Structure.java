package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.util.List;

public abstract class Structure extends SkriptEvent {


	protected SectionConfiguration getConfiguration() {
		return null;
	}

	@Override
	public List<Statement> loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
		SectionConfiguration configuration = getConfiguration();
		if (configuration != null) {
			configuration.loadConfiguration(null, section, parserState, logger);
			List<FileElement> elements = section.getElements();
			elements.subList(0, configuration.getEntries().size()).clear();
		}
		return ScriptLoader.loadItems(section, parserState, logger);
	}

	/**
	 * @return the default loading priority for structures
	 */
	@Override
	public int getLoadingPriority() {
		return 400;
	}

}
