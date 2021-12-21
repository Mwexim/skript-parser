package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SectionConfiguration {
	@Nullable
	private CodeSection parent;
	private final List<EntryLoader> entries = new ArrayList<>();
	private final Map<String, Object> data = new HashMap<>();

	public SectionConfiguration addOption(String key) {
		return addOption(false, key);
	}

	public SectionConfiguration addOption(boolean multiple, String key) {
		return addOption(multiple, key, false);
	}

	public SectionConfiguration addOption(boolean multiple, String key, boolean optional) {
		entries.add(new OptionLoader(multiple, key, optional));
		return this;
	}

	public SectionConfiguration addSection(String key) {
		return addSection(key, false);
	}

	public SectionConfiguration addSection(String key, boolean optional) {
		entries.add(new SectionLoader(key, optional));
		return this;
	}

	public SectionConfiguration addLoader(EntryLoader loader) {
		entries.add(loader);
		return this;
	}

	public boolean loadConfiguration(@Nullable CodeSection parent, FileSection section, ParserState parserState, SkriptLogger logger) {
		boolean successful = true;
		this.parent = parent;

		outer:
		for (var entry : entries) {
			for (var el : section.getElements()) {
				logger.setLine(el.getLine() - 1);

				if (logger.hasError()) {
					/*
					 * If the execution of 'loadEntry' caused errors, it means that we
					 * should not continue parsing the other sections, as specified in
					 * the Javadoc.
					 * We finalize the logs and move on to the next entry.
					 */
					logger.finalizeLogs();
					successful = false;
					continue outer;
				}
				if (el instanceof VoidElement)
					continue;
				if (entry.loadEntry(this, el, parserState, logger))
					continue outer;
			}
			if (entry.isOptional())
				continue;
			// If we're here, it means no value matched and the entry hasn't been configured.
			// Only the section line is relevant.
			logger.setLine(section.getLine() - 1);
			logger.error("The entry named '" + entry.key + "' has not been configured", ErrorType.SEMANTIC_ERROR);
			logger.finalizeLogs();
			successful = false;
		}

		if (!successful) {
			// Add a final error message stating the section has not been configured correctly.
			// Only the section line is relevant.
			logger.setLine(section.getLine() - 1);
			logger.error("The section '" + section.getLineContent() + "' has not been configured correctly", ErrorType.SEMANTIC_ERROR);
		}
		return successful;
	}

	@Nullable
	public CodeSection getParent() {
		return parent;
	}

	public Map<String, Object> getData() {
		return data;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(String key, Class<T> cls) {
		return (T) data.get(key);
	}

	public String getString(String key) {
		return getValue(key, String.class);
	}

	public String[] getStringList(String key) {
		return getValue(key, String[].class);
	}

	public CodeSection getSection(String key) {
		return getValue(key, CodeSection.class);
	}
}