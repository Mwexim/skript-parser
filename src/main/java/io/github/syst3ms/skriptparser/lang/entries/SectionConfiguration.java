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
import java.util.Optional;

/**
 * The SectionConfiguration class can be used to create simple and efficient data sections
 * that only allow specific keys and values. Skript's command structure is the perfect example:
 * <pre>
 * {@code
 * command /hello:
 *     permission: plugin.hello
 *     permission message: "You don't have the permission to do that!"
 *     cooldown: 1 minute
 *     trigger:
 *         broadcast "Hello!"
 * }
 * </pre>
 * This example portrays the many features this class has.
 * <ul>
 *     <li>Keys can be omitted if they are optional.</li>
 *     <li>Whole sections can be added as value.</li>
 *     <li>Some keys only allow specific types.</li>
 * </ul>
 */
public class SectionConfiguration {

	private final Map<String, Object> data = new HashMap<>();
	private final List<EntryLoader> entries;

	@Nullable
	private CodeSection parent;
	private boolean loaded;

	private SectionConfiguration(List<EntryLoader> entries) {
		this.entries = entries;
	}

	/**
	 * Load the data of this {@link SectionConfiguration} into to the {@link #getData() data map} using
	 * a FileSection instance. This method should be called only once and will throw an error if attempting
	 * to load the configuration multiple times. The default use-case would be to load the configuration inside
	 * CodeSection's {@link CodeSection#loadSection(FileSection, ParserState, SkriptLogger) load} method.
	 * 
	 * @param parent the parent section
	 * @param section the file section
	 * @param parserState the parse state
	 * @param logger the logger
	 * @return whether the section was loaded successfully or errors occurred
	 */
	public boolean loadConfiguration(@Nullable CodeSection parent, FileSection section, ParserState parserState, SkriptLogger logger) {
		if (loaded)
			throw new IllegalStateException("This section configuration has already been loaded once");

		boolean successful = true;
		this.parent = parent;

		outer:
		for (var entry : entries) {
			for (var element : section.getElements()) {
				logger.setLine(element.getLine() - 1);
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
				if (element instanceof VoidElement)
					continue;
				if (entry.loadEntry(this, element, parserState, logger))
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
		loaded = true;
		return successful;
	}

	@Nullable
	public CodeSection getParent() {
		return parent;
	}

	/**
	 * @return a modifiable map containing the loaded data
	 */
	public Map<String, Object> getData() {
		return data;
	}

	@Nullable
	public Object getValue(String key) {
		return data.get(key);
	}

	/**
	 * Tries to retrieve a value from its key and cast it to the correct class.
	 * This can only be used when you register your option as a {@link Builder#addLiteral(String, Class) literal},
	 * otherwise, the option will likely be parsed as a String and throw an exception.
	 * Options that allow literal lists are saved as an array.
	 * 
	 * @param key the key of the node.
	 * @param cls the class to cast to.
	 * @return the value cast to the given class.
	 * @param <T> the type of the return value.
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getValue(String key, Class<T> cls) {
		var result = data.get(key);
		if (result == null)
			return Optional.empty();
		if (result.getClass() == String.class && result.getClass() != cls)
			throw new UnsupportedOperationException("The key '" + key + "' was not registered as a literal, was parsed as a String and can, therefore, not be cast to " + cls.getName());
		return Optional.of((T) result);
	}

	@Nullable
	public Optional<String> getString(String key) {
		return getValue(key, String.class);
	}

	public Optional<String[]> getStringList(String key) {
		return getValue(key, String[].class);
	}

	public Optional<CodeSection> getSection(String key) {
		return getValue(key, CodeSection.class);
	}

	public static class Builder {

		private final List<EntryLoader> entries = new ArrayList<>();

		public Builder addKey(String key) {
			entries.add(new OptionLoader(key, false, false));
			return this;
		}

		public Builder addOptionalKey(String key) {
			entries.add(new OptionLoader(key, false, true));
			return this;
		}

		public Builder addList(String key) {
			entries.add(new OptionLoader(key, true, false));
			return this;
		}

		public Builder addOptionalList(String key) {
			entries.add(new OptionLoader(key, true, true));
			return this;
		}

		public Builder addLiteral(String key, Class<?> typeClass) {
			entries.add(new LiteralLoader<>(key, typeClass, false, false));
			return this;
		}

		public Builder addLiteralList(String key, Class<?> typeClass) {
			entries.add(new LiteralLoader<>(key, typeClass, true, false));
			return this;
		}

		public Builder addSection(String key) {
			entries.add(new SectionLoader(key, false));
			return this;
		}

		public Builder addLoader(EntryLoader loader) {
			entries.add(loader);
			return this;
		}

		public SectionConfiguration build() {
			return new SectionConfiguration(entries);
		}

	}

}
