package io.github.syst3ms.skriptparser.lang.entries;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;

import java.util.HashMap;
import java.util.Map;

public abstract class CategorySection extends CodeSection {
    protected final Map<String, String> options = new HashMap<>();
    protected final Map<String, CodeSection> sections = new HashMap<>();

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        boolean successful = true;

        outer:
        for (var option : getConfiguration()) {
            for (var el : section.getElements()) {
                logger.setLine(el.getLine() - 1);

                if (el instanceof VoidElement)
                    continue;
                if (el instanceof FileSection) {
                    var name = el.getLineContent(); // The trialing ':' is already removed here.
                    if (name.equalsIgnoreCase(option.name) && !option.single) {
                        var entry = new EntrySection(name);

                        entry.loadSection((FileSection) el, parserState, logger);
                        entry.setParent(this);

                        sections.put(name, entry);
                        continue outer;
                    } else if (name.equalsIgnoreCase(option.name)) {
                        logger.error("Expected an entry option for the entry, but found a section instead", ErrorType.SEMANTIC_ERROR);
                        break;
                    }
                } else {
                    var content = el.getLineContent().split(": ");
                    if (content.length != 2)
                        continue;

                    var name = el.getLineContent().split(": ")[0];
                    var entry = el.getLineContent().split(": ")[1];

                    if (name.equalsIgnoreCase(option.name) && option.single) {
                        options.put(name, entry);
                        continue outer;
                    } else if (name.equalsIgnoreCase(option.name)) {
                        logger.error("Expected an entry section for the entry, but found an option instead", ErrorType.SEMANTIC_ERROR);
                        break;
                    }
                }
            }

            if (option.optional)
                continue;
            if (!logger.hasError()) {
                // If we're here, it means no value matched.
                // Only the section line is relevant.
                logger.setLine(section.getLine() - 1);
                logger.error("The entry named '" + option.name + "' has not been configured", ErrorType.SEMANTIC_ERROR);
            }
            logger.finalizeLogs();
            successful = false;
        }

        if (!successful) {
            logger.setLine(section.getLine() - 1);
            logger.error("The category section '" + section.getLineContent() + "' has not been configured correctly", ErrorType.SEMANTIC_ERROR);
        }
        return successful;
    }

    protected abstract EntryOption[] getConfiguration();

    public static class EntryOption {
        private final String name;
        private final boolean single;
        private final boolean optional;

        public EntryOption(String name, boolean single) {
            this(name, single, false);
        }

        public EntryOption(String name, boolean single, boolean optional) {
            this.name = name;
            this.single = single;
            this.optional = optional;
        }

        /**
         * Names must be exact matches, ignoring cases. The entry will always be split
         * using ':' as delimiter. The first half will be used to match against this name,
         * the latter half will be used as entry, if this entry {@linkplain #isSingle() is single}.
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * If this entry needs a single String as configuration, this will return true.
         * Otherwise, this entry acts like a section and will use its items as configuration.
         * @return whether this entry needs a single value
         */
        public boolean isSingle() {
            return single;
        }

        /**
         * @return whether this entry is optional
         */
        public boolean isOptional() {
            return optional;
        }
    }
}
