package io.github.syst3ms.skriptparser.registration.tags;

import io.github.syst3ms.skriptparser.log.ErrorContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.RecentElementList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TagManager {
    /**
     * The ordering describing the order in which tags should be tested during parsing
     */
    private static final Comparator<TagInfo<?>> INFO_COMPARATOR = (t, t2) -> t2.getPriority() - t.getPriority();

    /**
     * All {@link Tag tags} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<TagInfo<?>> recentTags = new RecentElementList<>();
    private static final List<TagInfo<?>> tags = new ArrayList<>();


    public static void register(SkriptRegistration reg) {
        tags.addAll(reg.getTags());
        tags.sort(INFO_COMPARATOR);
    }

    /**
     * @return a list of all currently registered tags
     */
    public static List<TagInfo<?>> getTags() {
        return tags;
    }

    /**
     * Parse a string as a {@link Tag}.
     * Note that this does not support angle brackets ('<>') or ampersands ('&').
     * @param toParse the string to parse
     * @param logger the logger
     * @return the parsed tag, empty if no tag was found
     */
    public static Optional<Tag> parseTag(String toParse, SkriptLogger logger) {
        if (toParse.isEmpty()
                || Character.isWhitespace(toParse.charAt(0))
                || Character.isWhitespace(toParse.charAt(toParse.length() - 1)))
            return Optional.empty();

        for (var recentTag : recentTags) {
            var tag = matchTagInfo(toParse, recentTag, logger);
            if (tag.isPresent()) {
                recentTags.acknowledge(recentTag);
                logger.clearErrors();
                return tag;
            }
            logger.forgetError();
        }
        // Let's not loop over the same elements again
        var remainingTags = tags;
        recentTags.removeFrom(remainingTags);
        for (var remainingTag : remainingTags) {
            var tag = matchTagInfo(toParse, remainingTag, logger);
            if (tag.isPresent()) {
                recentTags.acknowledge(remainingTag);
                logger.clearErrors();
                return tag;
            }
            logger.forgetError();
        }
        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No tag matching '" + toParse + "' was found.", ErrorType.NO_MATCH);
        return Optional.empty();
    }

    private static Optional<Tag> matchTagInfo(String toCheck, TagInfo<?> info, SkriptLogger logger) {
        String[] values = toCheck.split("=", 2);
        assert values.length == 1 || values.length == 2;
        String key = values[0];
        String[] parameters = values.length == 2
                // Basically splits at ',' but takes backslashes into account.
                ? values[1].split("(?<!\\\\)(?:\\\\\\\\)*,")
                : new String[0];
        for (String s : parameters) {
            if (s.isEmpty())
                return Optional.empty();
        }
        try {
            var tag = info.getSyntaxClass()
                    .getDeclaredConstructor()
                    .newInstance();
            logger.setContext(ErrorContext.INITIALIZATION);
            if (tag.init(key, parameters))
                return Optional.of(tag);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
        }
        return Optional.empty();
    }
}
