package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SectionConfiguration {
	public static final String OPTION_SPLIT_PATTERN = ": ";

	private Map<String, Boolean> optionEntries = new HashMap<>();
	private Map<String, Boolean> sectionEntries = new HashMap<>();

	private Map<String, String> configuredOptions = new HashMap<>();
	private Map<String, CodeSection> configuredSections = new HashMap<>();

	public SectionConfiguration addOption(String key) {
		return addOption(key, false);
	}

	public SectionConfiguration addOption(String key, boolean optional) {
		optionEntries.put(key, optional);
		return this;
	}

	public SectionConfiguration addSection(String key) {
		return addSection(key, false);
	}

	public SectionConfiguration addSection(String key, boolean optional) {
		sectionEntries.put(key, optional);
		return this;
	}

	public SectionConfiguration build() {
		optionEntries = Collections.unmodifiableMap(optionEntries);
		sectionEntries = Collections.unmodifiableMap(sectionEntries);
		return this;
	}

	public boolean loadConfiguration(@Nullable CodeSection parent, FileSection section, ParserState parserState, SkriptLogger logger) {
		boolean successful = true;

		// Checking if all option entries are configured
		outer:
		for (var option : optionEntries.entrySet()) {
			for (var el : section.getElements()) {
				if (el instanceof VoidElement || el instanceof FileSection)
					continue;
				var content = el.getLineContent().split(OPTION_SPLIT_PATTERN);
				if (content.length != 2)
					continue;
				var key = content[0];
				var entry = content[1];

				if (key.equalsIgnoreCase(option.getKey())) {
					configuredOptions.put(option.getKey(), entry);
					continue outer;
				}
			}
			if (option.getValue())
				continue;
			// If we're here, it means no value matched and the entry hasn't been configured.
			// Only the section line is relevant.
			logger.setLine(section.getLine() - 1);
			logger.error("The option entry named '" + option.getKey() + "' has not been configured", ErrorType.SEMANTIC_ERROR);
			logger.finalizeLogs();
			successful = false;
		}

		// Checking if all section entries are configured.
		outer:
		for (var option : sectionEntries.entrySet()) {
			for (var el : section.getElements()) {
				if (el.getLineContent().equalsIgnoreCase(option.getKey())) {
					var entry = new EntrySection((FileSection) el, parserState, logger, section.getLineContent());
					if (parent != null)
						entry.setParent(parent);
					configuredSections.put(option.getKey(), entry);
					continue outer;
				}
			}
			if (option.getValue())
				continue;
			// If we're here, it means no value matched and the entry hasn't been configured.
			// Only the section line is relevant.
			logger.setLine(section.getLine() - 1);
			logger.error("The section entry named '" + option.getKey() + "' has not been configured", ErrorType.SEMANTIC_ERROR);
			logger.finalizeLogs();
			successful = false;
		}

		if (!successful) {
			// Add a final error message stating the section has not been configured correctly.
			// Only the section line is relevant.
			logger.setLine(section.getLine() - 1);
			logger.error("The section '" + section.getLineContent() + "' has not been configured correctly", ErrorType.SEMANTIC_ERROR);
		}

		configuredOptions = Collections.unmodifiableMap(configuredOptions);
		configuredSections = Collections.unmodifiableMap(configuredSections);
		return successful;
	}

	public String getOption(String key) {
		return configuredOptions.get(key);
	}

	public CodeSection getSection(String key) {
		return configuredSections.get(key);
	}
}