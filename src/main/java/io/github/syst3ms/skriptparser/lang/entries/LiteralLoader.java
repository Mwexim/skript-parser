package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.types.TypeManager;

import java.util.ArrayList;
import java.util.List;

public class LiteralLoader<T> extends OptionLoader {

	private final Class<T> typeClass;

	public LiteralLoader(String key, Class<T> typeClass, boolean multiple, boolean optional) {
		super(key, multiple, optional);
		this.typeClass = typeClass;
	}

	@Override
	public boolean loadEntry(SectionConfiguration config, FileElement element, ParserState parserState, SkriptLogger logger) {
		// We will use the loaded values later
		if (!super.loadEntry(config, element, parserState, logger))
			return false;

		var type = TypeManager.getByClassExact(typeClass);
		if (type.isEmpty()) {
			logger.error("Couldn't find a type corresponding to the class '" + typeClass.getName() + "'", ErrorType.NO_MATCH);
			return false;
		} else if (type.get().getLiteralParser().isEmpty()) {
			logger.error("The type '" + type.get().getBaseName() + "' doesn't have a literal parser.", ErrorType.NO_MATCH);
			return false;
		}
		var parser = type.get().getLiteralParser().get();

		logger.setLine(element.getLine() - 1);

		if (isMultiple()) {
			List<T> data = new ArrayList<>();
			boolean successful = true;

			var list = config.getStringList(key);
			if (!list.isPresent()) {
				logger.error("List at key '" + key + "' does not exist.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			for (var value : list.get()) {
				var result = parser.apply(value);
				if (result == null) {
					// With the logic that errors get skipped, we will allow the other values to be parsed.
					logger.error("Couldn't parse '" + value + "' as " + type.get().withIndefiniteArticle(false), ErrorType.SEMANTIC_ERROR);
					logger.finalizeLogs();
					successful = false;
					continue;
				}
				data.add(result);
			}
			config.getData().put(key, data.toArray());
			return successful;
		} else {
			var value = config.getString(key);
			if (!value.isPresent()) {
				logger.error("Key '" + key + "' does not exist.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			var result = parser.apply(value.get());
			// We don't want this data to linger if an error occurs
			config.getData().remove(key);
			if (result == null) {
				logger.error("Couldn't parse '" + config.getString(key) + "' as " + type.get().withIndefiniteArticle(false), ErrorType.SEMANTIC_ERROR);
				return false;
			}
			config.getData().put(key, result);
			return true;
		}

	}

}
