package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A class for parsing syntaxes in string form into parser-usable objects
 */
public class PatternParser {
    private static final Pattern PARSE_MARK_PATTERN = Pattern.compile("(0[bx])?(\\d+?):(.*)");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(-)?([*~])?(=)?(?<types>[\\w/]+)?");

    /**
     * Parses a pattern and returns a {@link PatternElement}. This method can be called by itself, for example when parsing group constructs.
     * @param pattern the pattern to be parsed
     * @return the parsed PatternElement, or {@literal null} if something went wrong.
     */
    @Nullable
    public PatternElement parsePattern(String pattern, SkriptLogger logger) {
        if (pattern.isEmpty())
            return new TextElement("");
        List<PatternElement> elements = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();
        String[] parts = StringUtils.splitVerticalBars(pattern, logger);
        if (parts == null) {
            return null;
        } else if (parts.length > 1) {
            pattern = "(" + pattern + ")";
        }
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            int initialPos = i;
            if (c == '[') {
                String s = StringUtils.getEnclosedText(pattern, '[', ']', i);
                if (s == null) {
                    logger.error("Unmatched square bracket (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return null;
                } else if (s.isEmpty()) {
                    logger.warn("There is an empty optional group. Place a backslash before a square bracket for it to be interpreted literally : [" + s + "]");
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.length() + 1; // sets i to the closing bracket, for loop does the rest
                Matcher matcher = PARSE_MARK_PATTERN.matcher(s);
                PatternElement content;
                String[] vertParts = StringUtils.splitVerticalBars(s, logger);
                if (vertParts == null) {
                    return null; // The content is malformed anyway
                }
                if (matcher.matches() && vertParts.length == 1) {
                    String base = matcher.group(1);
                    String mark = matcher.group(2);
                    int markNumber;
                    try {
                        if (base == null) {
                            markNumber = Integer.parseInt(mark);
                        } else if (base.equals("0b")) {
                            markNumber = Integer.parseInt(mark, 2);
                        } else if (base.equals("0x")) {
                            markNumber = Integer.parseInt(mark, 16);
                        } else {
                            logger.error("Invalid parse mark (index " + initialPos + ") : '" + base + mark + "'", ErrorType.MALFORMED_INPUT);
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    String rest = matcher.group(3);
                    PatternElement e = parsePattern(rest, logger);
                    if (e == null) {
                        return null;
                    }
                    content = new ChoiceGroup(Collections.singletonList(new ChoiceElement(e, markNumber))); // I said I would keep the other constructor for unit tests
                } else {
                    content = parsePattern(s, logger);
                    if (content == null) {
                        return null;
                    }
                }
                elements.add(new OptionalGroup(content));
            } else if (c == '(') {
                String s = StringUtils.getEnclosedText(pattern, '(', ')', i);
                if (s == null) {
                    logger.error("Unmatched parenthesis (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.length() + 1;
                String[] choices = StringUtils.splitVerticalBars(s, logger);
                if (choices == null) {
                    return null;
                }
                List<ChoiceElement> choiceElements = new ArrayList<>();
                for (String choice : choices) {
                    if (choice.isEmpty()) {
                        logger.warn("There is an empty choice in the choice group. Place a backslash before the vertical bar for it to be interpreted literally : (" + s + ")");
                    }
                    Matcher matcher = PARSE_MARK_PATTERN.matcher(choice);
                    if (matcher.matches()) {
                        String base = matcher.group(1);
                        String mark = matcher.group(2);
                        int markNumber;
                        try {
                            if (base == null) {
                                markNumber = Integer.parseInt(mark);
                            } else if (base.equals("0b")) {
                                markNumber = Integer.parseInt(mark, 2);
                            } else if (base.equals("0x")) {
                                markNumber = Integer.parseInt(mark, 16);
                            } else {
                                logger.error("Invalid parse mark (index " + initialPos + ") : '" + base + mark + "'", ErrorType.MALFORMED_INPUT);
                                return null;
                            }
                        } catch (NumberFormatException e) {
                            return null;
                        }
                        String rest = matcher.group(3);
                        PatternElement choiceContent = parsePattern(rest, logger);
                        if (choiceContent == null) {
                            return null;
                        }
                        choiceElements.add(new ChoiceElement(choiceContent, markNumber));
                    } else {
                        PatternElement choiceContent = parsePattern(choice, logger);
                        if (choiceContent == null) {
                            return null;
                        }
                        choiceElements.add(new ChoiceElement(choiceContent, 0));
                    }
                }
                elements.add(new ChoiceGroup(choiceElements));
            } else if (c == '<') {
                String s = StringUtils.getEnclosedText(pattern, '<', '>', i);
                if (s == null) {
                    logger.error("Unmatched angle bracket (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return null;
                } else if (s.isEmpty()) {
                    logger.warn("There is an empty regex group. Place a backslash before an angle bracket for it to be interpreted literally : [" + s + "]");
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.length() + 1;
                Pattern pat;
                try {
                    pat = Pattern.compile(s);
                } catch (PatternSyntaxException e) {
                    logger.error("Invalid regex pattern (index " + initialPos + ") : '" + s + "'", ErrorType.MALFORMED_INPUT);
                    return null;
                }
                elements.add(new RegexGroup(pat));
            } else if (c == '%') {
                /*
                 * Can't use getEnclosedText as % acts for both opening and closing,
                 * and there's no need of checking for nested stuff
                 */
                int nextIndex = pattern.indexOf('%', i + 1);
                if (nextIndex == -1) {
                    logger.error("Unmatched percent (index " + initialPos + ") : '" + pattern.substring(initialPos) + "'", ErrorType.MALFORMED_INPUT);
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder.setLength(0);
                }
                String s = pattern.substring(i + 1, nextIndex);
                i = nextIndex;
                Matcher m = VARIABLE_PATTERN.matcher(s);
                if (!m.matches()) {
                    logger.error("Invalid expression element (index " + initialPos + ") : '" + s + "'", ErrorType.MALFORMED_INPUT);
                    return null;
                } else {
                    boolean nullable = m.group(1) != null;
                    ExpressionElement.Acceptance acceptance = ExpressionElement.Acceptance.ALL;
                    if (m.group(2) != null) {
                        String acc = m.group(2);
                        if (acc.equals("~")) {
                            acceptance = ExpressionElement.Acceptance.EXPRESSIONS_ONLY;
                        } else if (acc.equals("^")) {
                            acceptance = ExpressionElement.Acceptance.VARIABLES_ONLY;
                        } else {
                            acceptance = ExpressionElement.Acceptance.LITERALS_ONLY;
                        }
                    }
                    String typeString = m.group("types");
                    String[] types = typeString.split("/");
                    List<PatternType<?>> patternTypes = new ArrayList<>();
                    for (String type : types) {
                        PatternType<?> t = TypeManager.getPatternType(type);
                        if (t == null) {
                            logger.error("Unknown type (index " + initialPos + ") : '" + type + "'", ErrorType.NO_MATCH);
                            return null;
                        }
                        patternTypes.add(t);
                    }
                    boolean acceptConditional = m.group(3) != null;
                    if (acceptConditional && patternTypes.stream().noneMatch(t -> t.getType().getTypeClass() == Boolean.class)) {
                        logger.error("Can't use the '=' flag on non-boolean types (index " + initialPos + ")", ErrorType.SEMANTIC_ERROR);
                        return null;
                    }
                    elements.add(new ExpressionElement(patternTypes, acceptance, nullable, acceptConditional));
                }
            } else if (c == '\\') {
                if (i == pattern.length() - 1) {
                    logger.error("Invalid backslash at the end of the string", ErrorType.MALFORMED_INPUT);
                    return null;
                } else {
                    textBuilder.append(chars[++i]);
                }
            } else if (c == ']' || c == ')' || c == '>') { // Closing brackets are skipped over, so this marks an error
                logger.error("Unmatched closing bracket (index " + initialPos + ") : '" + pattern.substring(0, initialPos + 1) + "'", ErrorType.MALFORMED_INPUT);
                return null;
            } else {
                textBuilder.append(c);
            }
        }
        if (textBuilder.length() != 0) {
            elements.add(new TextElement(textBuilder.toString()));
            textBuilder.setLength(0);
        }
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            return new CompoundElement(elements);
        }
    }

}
