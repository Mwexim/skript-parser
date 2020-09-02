package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * All uppercase, lowercase, or digit characters in a string.
 *
 * @name Characters
 * @pattern upper[ ]case char(acters|s) in %strings%
 * @pattern lower[ ]case char(acters|s) in %strings%
 * @pattern digit char(acters|s) in %strings%
 * @pattern special char(acters|s) in %strings%
 * @pattern white[]space char(acters|s) in %strings%
 * @since ALPHA
 * @author Olyno
 */
public class ExprChars implements Expression<String> {

    private enum CharType {
        UPPER_CASE, LOWER_CASE, DIGIT, SPECIAL, WHITE_SPACE
    }

    static {
        Parser.getMainRegistration().addExpression(ExprChars.class,
            String.class,
            true,
            "[all] upper[ ]case char(acters|s) in %strings%",
            "[all] lower[ ]case char(acters|s) in %strings%",
            "[all] digit char(acters|s) in %strings%",
            "[all] special char(acters|s) in %strings%",
            "[all] [white[]]space char(acters|s) in %strings%"
        );
    }

    private Expression<String> values;
    private CharType charType;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        values = (Expression<String>) expressions[0];
        charType = CharType.values()[matchedPattern];
        return true;
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        StringBuilder allChars = new StringBuilder();
        List<String> allValues = Arrays.asList(values.getValues(ctx));
        switch (charType) {
            case UPPER_CASE:
                allValues.forEach(value -> {
                    for (char character : value.toCharArray()) {
                        if (Character.isUpperCase(character)) {
                            allChars.append(character);
                        }
                    }
                });
                break;
            case LOWER_CASE:
                allValues.forEach(value -> {
                    for (char character : value.toCharArray()) {
                        if (Character.isLowerCase(character)) {
                            allChars.append(character);
                        }
                    }
                });
                break;
            case DIGIT:
                allValues.forEach(value -> {
                    for (char character : value.toCharArray()) {
                        if (Character.isDigit(character)) {
                            allChars.append(character);
                        }
                    }
                });
            case SPECIAL:
                allValues.forEach(value -> {
                    for (char character : value.toCharArray()) {
                        if (!Character.isLetterOrDigit(character) && !Character.isWhitespace(character)) {
                            allChars.append(character);
                        }
                    }
                });
                break;
            case WHITE_SPACE:
                allValues.forEach(value -> {
                    for (char character : value.toCharArray()) {
                        if (Character.isWhitespace(character)) {
                            allChars.append(character);
                        }
                    }
                });
                break;
        }
        return allChars.toString().split("");
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "all " + charType.name().replaceAll("_", "").toLowerCase() + " characters in " + values.toString(ctx, debug);
    }
}
