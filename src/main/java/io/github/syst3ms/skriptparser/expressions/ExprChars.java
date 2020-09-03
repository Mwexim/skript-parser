package io.github.syst3ms.skriptparser.expressions;

import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * All uppercase, lowercase, or digit characters in a string.
 *
 * @name Characters
 * @pattern upper[ ]case char[acter]s in %strings%
 * @pattern lower[ ]case char[acter]s in %strings%
 * @pattern digit char[acter]s in %strings%
 * @pattern special char[acter]s in %strings%
 * @pattern [white[]]space char[acter]s in %strings%
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
            false,
            "[all] upper[ ]case char[acter]s in %strings%",
            "[all] lower[ ]case char[acter]s in %strings%",
            "[all] digit char[acter]s in %strings%",
            "[all] special char[acter]s in %strings%",
            "[all] [white[]]space char[acter]s in %strings%"
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
        String content = String.join("", values.getValues(ctx));
        switch (charType) {
            case UPPER_CASE:
                for (char character : content.toCharArray()) {
                    if (Character.isUpperCase(character)) {
                        allChars.append(character);
                    }
                }
                break;
            case LOWER_CASE:
                for (char character : content.toCharArray()) {
                    if (Character.isLowerCase(character)) {
                        allChars.append(character);
                    }
                }
                break;
            case DIGIT:
                for (char character : content.toCharArray()) {
                    if (Character.isDigit(character)) {
                        allChars.append(character);
                    }
                }
            case SPECIAL:
                for (char character : content.toCharArray()) {
                    if (!Character.isLetterOrDigit(character) && !Character.isWhitespace(character)) {
                        allChars.append(character);
                    }
                }
                break;
            case WHITE_SPACE:
                for (char character : content.toCharArray()) {
                    if (Character.isWhitespace(character)) {
                        allChars.append(character);
                    }
                }
                break;
        }
        return allChars.toString().split("");
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "all " + charType.name().replaceAll("_", "").toLowerCase() + " characters in " + values.toString(ctx, debug);
    }
}
