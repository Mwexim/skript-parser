package io.github.syst3ms.skriptparser.registration.tags;

import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.util.ConsoleColors;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SkriptTags {
    public static final SimpleTag RESET_TAG = new SimpleTag("reset", 'r', s -> ConsoleColors.RESET + s);
    private static final Comparator<Tag> INFO_COMPARATOR = (t, t2) -> {
        if (t.getPriority() != t2.getPriority())
            return t2.getPriority() - t.getPriority();
        return priorityValue(t2) - priorityValue(t);
    };
    private static final List<Tag> tags = new ArrayList<>();
    private static boolean occasionalEnabled;

    /**
     * Registers a new {@link Tag}.
     * Note that instances of {@link Tag} itself will not function.
     * @param tag the tag to be registered
     */
    public static void registerTag(Tag tag) {
        registerTag(tag, false);
    }

    /**
     * Registers a new {@link Tag}.
     * Note that instances of {@link Tag} itself will not function.
     * @param tag the tag to be registered
     * @param occasional whether or not the tag is occasional
     */
    public static void registerTag(Tag tag, boolean occasional) {
        registerTag(tag, occasional, 5);
    }

    /**
     * Registers a new {@link Tag}.
     * Note that instances of {@link Tag} itself will not function.
     * @param tag the tag to be registered
     * @param occasional whether or not the tag is occasional
     * @param priority the priority
     */
    public static void registerTag(Tag tag, boolean occasional, int priority) {
        tag.setOccasional(occasional);
        tag.setPriority(priority);
        tags.add(tag);
    }

    /**
     * @return a list of all currently registered tags
     */
    public static List<Tag> getTags() {
        tags.sort(INFO_COMPARATOR);
        return tags;
    }

    /**
     * Parses a string as a {@link Tag}.
     * @param str the string to parse
     * @param logger the logger
     * @return the parsed tag
     */
    public static Optional<Tag> parseTag(String str, SkriptLogger logger) {
        if (str.isEmpty())
            return Optional.empty();
        // Quickly see if a tag is the reset tag. This tag is commonly used,
        // and since it's built-in in Skript and has special use-cases, this doesn't hurt.
        if (str.equalsIgnoreCase("&r") || str.equals("<reset>"))
            return Optional.of(RESET_TAG);

        if (str.startsWith("&")) {
            // The tag can only be a simple tag. The matching is very straightforward.
            assert str.length() == 2;
            final String matchString = str.substring(1);

            Optional<Tag> opt = getTags().stream()
                    .filter(t -> t instanceof SimpleTag)
                    .filter(t -> ((SimpleTag) t).matches(matchString, true))
                    .findFirst();
            if (opt.isPresent()) {
                return opt;
            }
        } else if (str.startsWith("<") && str.endsWith(">") && StringUtils.findClosingIndex(str, '<', '>', 0) == str.length() - 1) {
            // The tag can be all 3 types. We'll need to do some research before we can know which one is applicable.
            String[] matchParts = str.substring(1, str.length() - 1).split("=", 2);

            if (matchParts.length > 1) {
                // There are more parts, which means it's a normal tag.
                assert matchParts.length == 2;
                final String matchString = matchParts[0];

                Optional<Tag> opt = getTags().stream()
                        .filter(t -> t instanceof NormalTag)
                        .filter(t -> ((NormalTag) t).matches(matchString))
                        .findFirst();
                if (opt.isPresent()) {
                    NormalTag tag = (NormalTag) opt.get();
                    tag.setParameter(matchParts[1]);
                    return Optional.of(tag);
                }
            } else {
                // The tag can be either a simple tag or a dynamic opt. We check the simple tag first.
                assert matchParts.length == 1;
                final String matchString = matchParts[0];

                Optional<Tag> opt = getTags().stream()
                        .filter(t -> t instanceof SimpleTag)
                        .filter(t -> ((SimpleTag) t).matches(matchString, false))
                        .findFirst();
                if (opt.isPresent()) {
                    return opt;
                }

                // It has to be a dynamic tag at this moment.
                opt = getTags().stream()
                        .filter(t -> t instanceof DynamicTag)
                        .filter(t -> ((DynamicTag) t).matches(matchString))
                        .findFirst();
                if (opt.isPresent()) {
                    DynamicTag tag = (DynamicTag) opt.get();
                    tag.setKey(matchString);
                    return Optional.of(tag);
                }
            }
        }
        logger.error("No tag matching '" + str + "' was found.", ErrorType.SEMANTIC_ERROR);
        return Optional.empty();
    }

    public static boolean isOccasionalEnabled() {
        return occasionalEnabled;
    }

    public static <T> T occasionally(Supplier<T> action) {
        occasionalEnabled = true;
        T obj = action.get();
        occasionalEnabled = false;
        return obj;
    }

    private static int priorityValue(Tag tag) {
       if (tag instanceof NormalTag) {
           return 3;
       } else if (tag instanceof SimpleTag) {
           return 2;
       } else if (tag instanceof DynamicTag) {
           return 1;
       } else {
           return 0;
       }
    }
}
