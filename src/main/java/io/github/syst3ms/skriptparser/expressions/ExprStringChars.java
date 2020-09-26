package io.github.syst3ms.skriptparser.expressions;

import java.util.function.Predicate;

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
 * @pattern [all [of the]] upper[ ]case char[acter]s in %strings%
 * @pattern [all [of the]] lower[ ]case char[acter]s in %strings%
 * @pattern [all [of the]] digit char[acter]s in %strings%
 * @pattern [all [of the]] special char[acter]s in %strings%
 * @pattern [all [of the]] [white[]]space char[acter]s in %strings%
 * @since ALPHA
 * @author Olyno
 */
public class ExprStringChars implements Expression<String> {

    private final static PatternInfos<Predicate<Character>> PATTERNS = new PatternInfos<>(
        new Object[][]{
            {"[all [of the]] upper[ ]case char[acter]s in %strings%", (Predicate<Character>) Character::isUpperCase},
            {"[all [of the]] lower[ ]case char[acter]s in %strings%", (Predicate<Character>) Character::isLowerCase},
            {"[all [of the]] digit char[acter]s in %strings%", (Predicate<Character>) Character::isDigit},
            {"[all [of the]] special char[acter]s in %strings%", (Predicate<Character>) (c) -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)},
            {"[all [of the]] [white[]]space char[acter]s in %strings%", (Predicate<Character>) Character::isWhitespace}
        }
    );
    
    private final String[] CHAR_TYPES = {
        "upper case", "lower case", "digit", "special", "white space"
    };

    static {
        Parser.getMainRegistration().addExpression(ExprStringChars.class,
            String.class,
            false,
            PATTERNS.getPatterns()
        );
    }

    private Expression<String> values;
    private int charType;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        values = (Expression<String>) expressions[0];
        charType = matchedPattern;
        return true;
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        StringBuilder allChars = new StringBuilder();
        String content = String.join("", values.getValues(ctx));
        for (char character : content.toCharArray()) {
            if (PATTERNS.getInfo(charType).test(character)) {
                allChars.append(character);
            }
        }
        return allChars.toString().split("");
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "all " + CHAR_TYPES[charType] + " characters in " + values.toString(ctx, debug);
    }
}
