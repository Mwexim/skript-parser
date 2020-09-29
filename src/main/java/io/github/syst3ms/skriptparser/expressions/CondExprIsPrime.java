package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.util.Arrays;

/**
 * Check if a given number is a prime number.
 * This means that for a number {@code n},
 * there does not exist a number {@code a} such that
 * {@code a < n} and {@code n % a = 0}, except for {@code a = 1}.
 *
 * @name Is Prime
 * @type CONDITION
 * @pattern %numbers% (is|are)[ not|n't] [a] prime [number]
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprIsPrime extends PropertyConditional<Number> {

    static {
        Parser.getMainRegistration().addSelfRegisteringElement(
                CondExprIsPrime.class,
                "numbers",
                ConditionalType.BE,
                "[a] prime [number]"
        );
    }

    @Override
    public boolean check(TriggerContext ctx, Number[] performers) {
        return isNegated() != Arrays.stream(performers)
                .allMatch(n ->
                    BigDecimalMath.isIntValue(BigDecimalMath.getBigDecimal(n))
                            && isPrime(n.intValue())
                );
    }

    /*
     * This method is indeed pretty slow and will often create issues
     * where the calculations will exponentially take up more time.
     * I believe this is not an issue, even when we use really high numbers
     * because of BigInteger.
     */
    // TODO implement a better system, possibly making use of the math utils for numbers higher than Integer.MAX_VALUE
    public static boolean isPrime(int number) {
        if (number == 1)
            return false;
        if (number % 2 == 0)
            return false;
        for (int i = 3; i * i <= number; i += 2) {
            if (number % i == 0)
                return false;
        }
        return true;
    }
}
