package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * Various arithmetic expressions, including addition, subtraction, multiplication, division and exponentiation.
 * Notes :
 * <ul>
 *      <li>All of the operations will accommodate for the type of the two operands.
 *          <ul>
 *              <li>Two operands of the same type will yield a result of that type, except in the following special cases
 *                  <ul>
 *                      <li>Trying to divide in general will always return a {@link BigDecimal}</li>
 *                      <li>Trying to divide anything by 0 will return {@literal 0} regardless of the original types.</li>
 *                  </ul>
 *              </li>
 *              <li>Adding a decimal type to an integer type will yield a decimal result.</li>
 *              <li>If any of the operands is an arbitrary precision number, the result will be of arbitrary precision</li>
 *          </ul>
 *      </li>
 *      <li>0<sup>0</sup> is defined to be 1</li>
 * </ul>
 * 
 * @name Arithmetic Operators
 * @pattern %number%[ ]+[ ]%number%
 * @pattern %number%[ ]-[ ]%number%
 * @pattern %number%[ ]*[ ]%number%
 * @pattern %number%[ ]/[ ]%number%
 * @pattern %number%[ ]^[ ]%number%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprArithmeticOperators implements Expression<Number> {
    public static final PatternInfos<Operator> PATTERNS = new PatternInfos<>(new Object[][]{
            {"%number%[ ]+[ ]%number%", Operator.PLUS},
            {"%number%[ ]-[ ]%number%", Operator.MINUS},
            {"%number%[ ]*[ ]%number%", Operator.MULT},
            {"%number%[ ]/[ ]%number%", Operator.DIV},
            {"%number%[ ]^[ ]%number%", Operator.EXP},
        }
    );

    static {
        Parser.getMainRegistration().addExpression(
            ExprArithmeticOperators.class,
            Number.class,
            true,
            PATTERNS.getPatterns()
        );
    }

    private Expression<? extends Number> first, second;
    private Operator op;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, ParseContext parseContext) {
        first = (Expression<? extends Number>) exprs[0];
        second = (Expression<? extends Number>) exprs[1];
        op = PATTERNS.getInfo(matchedPattern);
        if (second instanceof Literal) {
            Optional<? extends Number> value = ((Literal<? extends Number>) second).getSingle();
            if (value.filter(Operator::isZero).isPresent()) {
                parseContext.getLogger().error(
                        "Cannot divide by 0!",
                        ErrorType.SEMANTIC_ERROR,
                        "Make sure the expression/variable you want to divide with does not represent 0, as dividing by 0 results in mathematical issues"
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                .map(f -> (Number) f, s -> (Number) s)
                .mapToOptional((f, s) -> new Number[]{op.calculate(f, s)})
                .orElse(new Number[0]);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return first.toString(ctx, debug) + " " + op + " " + second.toString(ctx, debug);
    }

    @Override
    public Expression<? extends Number> simplify() {
        if (first instanceof Literal && second instanceof Literal)
            return new SimpleLiteral<>(Number.class, getValues(TriggerContext.DUMMY));
        return this;
    }

    private enum Operator {
        PLUS('+') {
            @Override
            public Number calculate(Number left, Number right) {
                if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    var l = BigDecimalMath.getBigDecimal(left);
                    var r = BigDecimalMath.getBigDecimal(right);
                    return l.add(r);
                } else {
                    assert left instanceof BigInteger && right instanceof BigInteger;
                    return ((BigInteger) left).add(((BigInteger) right));
                }
            }
        },
        MINUS('-') {
            @Override
            public Number calculate(Number left, Number right) {
                if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    var l = BigDecimalMath.getBigDecimal(left);
                    var r = BigDecimalMath.getBigDecimal(right);
                    return l.subtract(r);
                } else {
                    assert left instanceof BigInteger && right instanceof BigInteger;
                    return ((BigInteger) left).subtract(((BigInteger) right));
                }
            }
        },
        MULT('*') {
            @Override
            public Number calculate(Number left, Number right) {
                if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    var l = BigDecimalMath.getBigDecimal(left);
                    var r = BigDecimalMath.getBigDecimal(right);
                    return l.multiply(r);
                } else {
                    assert left instanceof BigInteger && right instanceof BigInteger;
                    return ((BigInteger) left).multiply(((BigInteger) right));
                }
            }
        },
        DIV('/') {
            @Override
            public Number calculate(Number left, Number right) {
                if (isZero(right)) {
                    return BigInteger.ZERO;
                } else {
                    return BigDecimalMath.getBigDecimal(left).divide(BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
                }
            }
        },
        EXP('^') {
            @Override
            public Number calculate(Number left, Number right) {
                if (isZero(right)) {
                    return left instanceof BigDecimal ? BigDecimal.ONE : BigInteger.ONE;
                }
                if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    return BigDecimalMath.pow(BigDecimalMath.getBigDecimal(left), BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
                } else {
                    assert left instanceof BigInteger && right instanceof BigInteger;
                    return pow((BigInteger) left, (BigInteger) right);
                }
            }
        };

        public final char sign;

        Operator(char sign) {
            this.sign = sign;
        }

        public abstract Number calculate(Number left, Number right);

        @Override
        public String toString() {
            return String.valueOf(sign);
        }

        private static boolean isZero(Number n) {
            return BigDecimalMath.getBigDecimal(n).compareTo(BigDecimal.ZERO) == 0;
        }

        private static BigInteger pow(BigInteger x, BigInteger y) {
            BigInteger z = x;
            BigInteger result = BigInteger.ONE;
            byte[] bytes = y.toByteArray();
            for (int i = bytes.length - 1; i >= 0; i--) {
                byte bits = bytes[i];
                for (int j = 0; j < 8; j++) {
                    if ((bits & 1) != 0)
                        result = result.multiply(z);
                    if ((bits >>= 1) == 0 && i == 0)
                        return result;
                    z = z.multiply(z);
                }
            }
            return result;
        }
    }
}
