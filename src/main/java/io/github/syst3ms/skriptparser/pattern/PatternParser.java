package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A class for parsing syntaxes in string form into parser-usable objects
 */
public class PatternParser {
    private static final Pattern PARSE_MARK_PATTERN = Pattern.compile("(([A-Za-z0-9]+)?):(.*)");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(-)?([*^~])?(=)?(?<types>[\\w/]+)?");

    /**
     * Parses a pattern and returns a {@link PatternElement}. This method can be called by itself, for example when parsing group constructs.
     * @param pattern the pattern to be parsed
     * @return the parsed PatternElement, or {@literal null} if something went wrong.
     */
    public static Optional<? extends PatternElement> parsePattern(String pattern, SkriptLogger logger) {
        if (pattern.isEmpty())
            return Optional.of(new TextElement(""));

        List<PatternElement> elements = new ArrayList<>();
        var textBuilder = new StringBuilder();
        var parts = StringUtils.splitVerticalBars(pattern, logger);

        if (parts.isEmpty()) {
            return Optional.empty();
        } else if (parts.get().length > 1) {
            pattern = "(" + pattern + ")";
        }

        var chars = pattern.toCharArray();
        boolean marked = false;
        for (var i = 0; i < chars.length; i++) {
            char c = chars[i];
            int initialPos = i;
            if (c == ':') {
                if (chars[i + 1] != '(') {
                    logger.warn("No group found after leading mark sign (index " + initialPos + "): '" + pattern.substring(initialPos) + "'. Escape the character to prevent ambiguity.");
                    textBuilder.append(c);
                    continue;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                marked = true;
            } else if (c == '[') {
                var s = StringUtils.getEnclosedText(pattern, '[', ']', i);
                if (s.isEmpty()) {
                    logger.error("Unmatched square bracket (index " + initialPos + "): '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                } else if (s.get().isEmpty()) {
                    logger.warn("There is an empty optional group. Place a backslash before a square bracket for it to be interpreted literally : [" + s.get() + "]");
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.get().length() + 1; // sets i to the closing bracket, for loop does the rest
                Optional<OptionalGroup> optionalGroup;
                var matcher = PARSE_MARK_PATTERN.matcher(s.get());
                var vertParts = StringUtils.splitVerticalBars(pattern, logger);
                if (vertParts.isEmpty()) {
                    return Optional.empty(); // The content is malformed anyway
                }
                if (matcher.matches() && vertParts.get().length == 1) {
                    final var mark = matcher.group(1);
                    optionalGroup = parsePattern(matcher.group(3), logger)
                            .map(el -> {
                                if (mark.isEmpty()) {
                                    var simplified = el.simplify();
                                    return new ChoiceElement(
                                            el,
                                            simplified.size() > 0 ? simplified.get(0) : el.toString()
                                    );
                                } else {
                                    return new ChoiceElement(el, mark);
                                }
                            })
                            .map(el -> new ChoiceGroup(Collections.singletonList(el)))
                            .map(OptionalGroup::new);
                } else {
                    optionalGroup = s.flatMap(st -> parsePattern(st, logger))
                            .map(OptionalGroup::new);
                }
                if (optionalGroup.isEmpty()) {
                    return Optional.empty();
                } else {
                    elements.add(optionalGroup.get());
                }
            } else if (c == '(') {
                var s = StringUtils.getEnclosedText(pattern, '(', ')', i);
                if (s.isEmpty()) {
                    logger.error("Unmatched parenthesis (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.get().length() + 1;
                var choices = StringUtils.splitVerticalBars(s.get(), logger);
                if (choices.isEmpty()) {
                    return Optional.empty();
                }
                List<ChoiceElement> choiceElements = new ArrayList<>();
                for (var choice : choices.get()) {
                    if (choice.isEmpty()) {
                        logger.warn("There is an empty choice in the choice group. Place a backslash before the vertical bar for it to be interpreted literally : (" + s.get() + ")");
                    }
                    Optional<ChoiceElement> choiceElement;
                    var matcher = PARSE_MARK_PATTERN.matcher(choice);
                    if (matcher.matches() || marked) {
                        var mark = matcher.matches() ? matcher.group(1) : "";
                        var rest = matcher.matches() ? matcher.group(3) : choice;
                        choiceElement = parsePattern(rest, logger)
                                .map(el -> {
                                    if (mark.isEmpty()) {
                                        var simplified = el.simplify();
                                        return new ChoiceElement(el, simplified.size() > 0 ? simplified.get(0) : el.toString());
                                    } else {
                                        return new ChoiceElement(el, mark);
                                    }
                                });
                    } else {
                        choiceElement = parsePattern(choice, logger)
                                .map(p -> new ChoiceElement(p, null));
                    }
                    if (choiceElement.isEmpty()) {
                        return Optional.empty();
                    } else {
                        choiceElements.add(choiceElement.get());
                    }
                }
                elements.add(new ChoiceGroup(choiceElements));
                marked = false;
            } else if (c == '<') {
                var s = StringUtils.getEnclosedText(pattern, '<', '>', i);
                if (s.isEmpty()) {
                    logger.error("Unmatched angle bracket (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                } else if (s.get().isEmpty()) {
                    logger.warn("There is an empty regex group. Place a backslash before an angle bracket for it to be interpreted literally : [" + s.get() + "]");
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.get().length() + 1;
                Pattern pat;
                try {
                    pat = Pattern.compile(s.get());
                } catch (PatternSyntaxException e) {
                    logger.error("Invalid regex pattern (index " + initialPos + ") : '" + s.get() + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                }
                elements.add(new RegexGroup(pat));
            } else if (c == '%') {
                /*
                 * Can't use getEnclosedText as % acts for both opening and closing,
                 * and there's no need of checking for nested stuff
                 */
                var nextIndex = pattern.indexOf('%', i + 1);
                if (nextIndex == -1) {
                    logger.error("Unmatched percent (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder.setLength(0);
                }
                var s = pattern.substring(i + 1, nextIndex);
                i = nextIndex;
                var m = VARIABLE_PATTERN.matcher(s);
                if (!m.matches()) {
                    logger.error("Invalid expression element (index " + initialPos + ") : '" + s + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                } else {
                    var nullable = m.group(1) != null;
                    var acceptance = ExpressionElement.Acceptance.ALL;
                    if (m.group(2) != null) {
                        var acc = m.group(2);
                        if (acc.equals("~")) {
                            acceptance = ExpressionElement.Acceptance.EXPRESSIONS_ONLY;
                        } else if (acc.equals("^")) {
                            acceptance = ExpressionElement.Acceptance.VARIABLES_ONLY;
                        } else {
                            acceptance = ExpressionElement.Acceptance.LITERALS_ONLY;
                        }
                    }
                    var typeString = m.group("types");
                    var types = typeString.split("/");
                    List<PatternType<?>> patternTypes = new ArrayList<>();
                    for (var type : types) {
                        var t = TypeManager.getPatternType(type);
                        if (t.isEmpty()) {
                            logger.error("Unknown type (index " + initialPos + ") : '" + type + "'", ErrorType.NO_MATCH);
                            return Optional.empty();
                        }
                        patternTypes.add(t.get());
                    }
                    var acceptConditional = m.group(3) != null;
                    if (acceptConditional && patternTypes.stream().noneMatch(t -> t.getType().getTypeClass() == Boolean.class)) {
                        logger.error("Can't use the '=' flag on non-boolean types (index " + initialPos + ")", ErrorType.SEMANTIC_ERROR);
                        return Optional.empty();
                    }
                    elements.add(new ExpressionElement(patternTypes, acceptance, nullable, acceptConditional));
                }
            } else if (c == '\\') {
                if (i == pattern.length() - 1) {
                    logger.error("Invalid backslash at the end of the string", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                } else {
                    textBuilder.append(chars[++i]);
                }
            } else if (c == '}' || c == ']' || c == ')' || c == '>') { // Closing brackets are skipped over, so this marks an error
                logger.error("Unmatched closing bracket (index " + initialPos + ") : '" + pattern.substring(0, initialPos + 1) + "'", ErrorType.MALFORMED_INPUT);
                return Optional.empty();
            } else {
                textBuilder.append(c);
            }
        }
        if (textBuilder.length() != 0) {
            elements.add(new TextElement(textBuilder.toString()));
            textBuilder.setLength(0);
        }
        if (elements.size() == 1) {
            return Optional.of(elements.get(0));
        } else {
            return Optional.of(new CompoundElement(elements));
        }
    }
}
