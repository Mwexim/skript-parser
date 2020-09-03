package io.github.syst3ms.skriptparser.expressions;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;

/**
 * All uppercase, lowercase, or digit characters in a string.
 *
 * @name Characters
 * @pattern [all] upper[ ]case char[acter]s in %strings%
 * @pattern [all] lower[ ]case char[acter]s in %strings%
 * @pattern [all] digit char[acter]s in %strings%
 * @pattern [all] special char[acter]s in %strings%
 * @pattern [all] [white[]]space char[acter]s in %strings%
 * @since ALPHA
 * @author Olyno
 */
public class ExprStringChars implements Expression<String> {

    private enum CharType {
        UPPER_CASE, LOWER_CASE, DIGIT, SPECIAL, WHITE_SPACE
    }

    private final static PatternInfos<Function<Character, Boolean>> PATTERNS = new PatternInfos<>(
        new Object[][]{
            {"[all] upper[ ]case char[acter]s in %strings%", (Function<Character, Boolean>) Character::isUpperCase},
            {"[all] lower[ ]case char[acter]s in %strings%", (Function<Character, Boolean>) Character::isLowerCase},
            {"[all] digit char[acter]s in %strings%", (Function<Character, Boolean>) Character::isDigit},
            {"[all] special char[acter]s in %strings%", (Function<Character, Boolean>) (c) -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)},
            {"[all] [white[]]space char[acter]s in %strings%", (Function<Character, Boolean>) Character::isWhitespace}
        }
	);

    static {
        Parser.getMainRegistration().addExpression(ExprStringChars.class,
            String.class,
            false,
            PATTERNS.getPatterns()
        );
    }

    private Expression<String> values;
    CharType charType;

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
        for (char character : content.toCharArray()) {
            if (PATTERNS.getInfo(charType.ordinal()).apply(character))
                allChars.append(character);
        }
        return allChars.toString().split("");
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "all " + charType.name().replaceAll("_", "").toLowerCase() + " characters in " + values.toString(ctx, debug);
    }
}
