package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.lang.base.TaggedExpression;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.registration.tags.Tag;
import io.github.syst3ms.skriptparser.registration.tags.TagManager;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A string that possibly contains expressions inside it, meaning that its value may be unknown at parse time
 */
@SuppressWarnings("ConfusingArgumentToVarargsMethod")
public class VariableString extends TaggedExpression {
    /**
     * An array containing raw data for this {@link VariableString}.
     * Contains {@link String} and {@link Expression} elements
     */
    private final Object[] data;
    private final boolean simple;

    private VariableString(Object[] data) {
        this.data = data;
        this.simple = data.length == 1 && data[0] instanceof String;
    }

    /**
     * Creates a new instance of a VariableString.
     * @param s the text to create a new instance from, with its surrounding quotes
     * @param logger the logger
     * @return {@code null} if either:
     * <ul>
     *     <li>The argument isn't quoted correctly</li>
     *     <li>{@link #newInstance(String, ParserState, SkriptLogger)} returned null, which can happen when the string literal is of the form
     *     {@code "..."}</li>
     *     <li>Something went very wrong when parsing a raw literal {@code R"possible delimiter(...)possible delimiter'}
     *     </li>
     * </ul>. Returns a new instance of a VariableString otherwise.
     */
    public static Optional<VariableString> newInstanceWithQuotes(String s, ParserState parserState, SkriptLogger logger) {
        if (s.startsWith("\"") && s.endsWith("\"") && StringUtils.nextSimpleCharacterIndex(s, 0) == s.length()) {
            return newInstance(s.substring(1, s.length() - 1), parserState, logger);
        } else if (s.startsWith("'") && s.endsWith("'") && StringUtils.nextSimpleCharacterIndex(s, 0) == s.length()) {
            return Optional.of(new VariableString(new String[] {
                    s.substring(1, s.length() - 1).replace("\\\"", "\"")
            }));
        }
        return Optional.empty();
    }

    /**
     * Creates a new instance of a VariableString from the text inside a string literal.
     * @param s the content of the string literal, without quotes
     * @param parserState the current parser state
     * @param logger the logger
     * @return a new instance of a VariableString, or {@code null} if there are unbalanced {@literal %} symbols
     */
    public static Optional<VariableString> newInstance(String s, ParserState parserState, SkriptLogger logger) {
        List<Object> data = new ArrayList<>(StringUtils.count(s, "%"));
        var sb = new StringBuilder();
        var charArray = s.toCharArray();
        for (var i = 0; i < charArray.length; i++) {
            var c = charArray[i];
            if (c == '%') {
                if (i == charArray.length - 1) {
                    return Optional.empty();
                } else if (charArray[i + 1] == '%') {
                    sb.append(charArray[++i]);
                    continue;
                }
                var content = StringUtils.getPercentContent(s, i + 1);
                var toParse = content.map(co -> co.replaceAll("\\\\(.)", "$1"));
                if (toParse.isEmpty())
                    return Optional.empty();
                logger.recurse();
                var expression = SyntaxParser.parseExpression(toParse.get(), SyntaxParser.OBJECTS_PATTERN_TYPE, parserState, logger);
                logger.callback();
                if (expression.isEmpty())
                    return Optional.empty();
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(expression.get());
                i += content.get().length() + 1;
            } else if (c == '<') {
                if (i == charArray.length - 1
                        || Character.isWhitespace(charArray[i + 1])) {
                    sb.append(c);
                    continue;
                }
                var content = StringUtils.getBracketContent(s, i + 1, '>');
                if (content.isEmpty()) {
                    sb.append(c);
                    continue;
                }
                logger.recurse();
                var tag = TagManager.parseTag(content.get(), logger);
                logger.callback();
                if (tag.isEmpty()) {
                    return Optional.empty();
                }
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(tag.get());
                i += content.get().length() + ">".length();
            } else if (c == '\\') {
                if (i == charArray.length - 1) {
                    return Optional.empty();
                }
                char next = charArray[++i];
                switch (next) {
                    case 'n':
                        sb.append(System.lineSeparator());
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    default:
                        sb.append(next);
                }
            } else if (c == '&') {
                if (i == charArray.length - 1) {
                    sb.append(c);
                    break;
                }
                char next = charArray[++i];
                if (Character.isWhitespace(next)) {
                    sb.append(c).append(next);
                    continue;
                }
                logger.recurse();
                var tag = TagManager.parseTag(String.valueOf(next), logger);
                logger.callback();
                if (tag.isEmpty()) {
                    logger.clearErrors();
                    sb.append(c).append(next);
                    continue;
                }
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(tag.get());
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            data.add(sb.toString());
        }
        return Optional.of(new VariableString(data.toArray()));
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    public String toString(TriggerContext ctx) {
        return toString(ctx, "default");
    }

    public String toString(TriggerContext ctx, String tagCtx) {
        if (simple)
            return (String) data[0];

        // Filters all non-usable tags away right from the start.
        var actualData = Arrays.stream(data)
                .filter(o -> !(o instanceof Tag) || ((Tag) o).isUsable(tagCtx))
                .toArray();
        var sb = new StringBuilder();
        int currentTags = 1;
        List<Tag> ongoingTags = new ArrayList<>();

        for (int i = 0; i < actualData.length; i++) {
            var o = actualData[i];
            if (o instanceof Expression) {
                sb.append(TypeManager.toString(((Expression<?>) o).getValues(ctx)));
            } else if (o instanceof Tag) {
                ongoingTags.add((Tag) o);
                int indexOfNext = CollectionUtils.ordinalConditionalIndexOf(actualData, ++currentTags, t -> t instanceof Tag);
                if (indexOfNext == -1)
                    indexOfNext = actualData.length;
                var affected = new StringBuilder();

                for (int j = i + 1; j < indexOfNext; j++) {
                    var o2 = actualData[j];
                    if (o2 instanceof Expression) {
                        affected.append(TypeManager.toString(((Expression<?>) o2).getValues(ctx)));
                    } else if (o2 instanceof Tag) {
                        throw new IllegalStateException();
                    } else {
                        affected.append(o2);
                    }
                }
                ongoingTags.removeIf(t -> !o.equals(t) && !((Tag) o).combinesWith(t.getClass()));
                var fin = affected.toString();
                for (Tag tag : ongoingTags) {
                    fin = tag.getValue(fin);
                }
                sb.append(fin);
                i = indexOfNext - 1;
            } else {
                sb.append(o);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        if (simple)
            return "\"" + data[0] + "\"";
        var sb = new StringBuilder("\"");
        for (var o : data) {
            if (o instanceof Expression) {
                sb.append('%').append(((Expression<?>) o).toString(ctx, debug)).append('%');
            } else if (o instanceof Tag) {
                sb.append(((Tag) o).toString(debug));
            } else {
                sb.append(o);
            }
        }
        return sb.append("\"").toString();
    }

    /**
     * @return whether this VariableString is actually constant and whose value can be known at parse time
     */
    public boolean isSimple() {
        return simple;
    }

    public String defaultVariableName() {
        if (simple)
            return (String) data[0];
        var sb = new StringBuilder();
        for (var o : data) {
            if (o instanceof String) {
                sb.append(o);
            } else if (o instanceof Tag) {
                sb.append(((Tag) o).toString(false));
            } else {
                assert o instanceof Expression;
                Optional<? extends ExpressionInfo<? extends Expression<?>, ?>> exprInfo = SyntaxManager.getExpressionExact((Expression<?>) o);
                assert exprInfo.isPresent();
                sb.append("<").append(exprInfo.get().getReturnType().toString()).append(">");
            }
        }
        return sb.toString();
    }
}
