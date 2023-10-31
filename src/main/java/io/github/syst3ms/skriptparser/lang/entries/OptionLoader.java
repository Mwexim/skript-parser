package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;

public class OptionLoader extends EntryLoader {
	public static final String OPTION_SPLIT_PATTERN = ": ";

	private final boolean multiple;

	public OptionLoader(String key, boolean multiple, boolean optional) {
		super(key, optional);
		this.multiple = multiple;
	}

	@Override
	public boolean loadEntry(SectionConfiguration config, FileElement element, ParserState parserState, SkriptLogger logger) {
		var content = element.getLineContent().split(OPTION_SPLIT_PATTERN);
		if (content.length == 0)
			return false;
		var key = content[0];
		var entry = content.length > 1 ? content[1] : null;

		if (!key.equalsIgnoreCase(this.key))
			return false;
		if (element instanceof FileSection) {
			if (!multiple) {
				logger.error("The entry '" + key + "' does not support multiple values.", ErrorType.SEMANTIC_ERROR);
				return false;
			} else if (entry != null) {
				logger.error("The entry '" + key + "' has been configured incorrectly.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			config.getData().put(this.key, ((FileSection) element).getElements().stream()
					.filter(el -> !(el instanceof VoidElement))
					.map(FileElement::getLineContent)
					.toArray(String[]::new)
			);
		} else {
			if (entry == null) {
				logger.error("The entry '" + key + "' has been configured incorrectly.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			config.getData().put(this.key, multiple ? new String[] {entry} : entry);
		}
		return true;
	}

	public boolean isMultiple() {
		return multiple;
	}
}
