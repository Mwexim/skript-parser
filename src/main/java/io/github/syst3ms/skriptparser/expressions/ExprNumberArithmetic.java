package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Various arithmetic expressions, including addition, subtraction, multiplication, division and exponentiation.
 * Notes :
 * <ul>
 *      <li>All of the operations will accommodate for the type of the two operands.
 *          <ul>
 *              <li>Two operands of the same type will yield a result of that type, except in the following special cases
 *                  <ul>
 *                      <li>Trying to divide 0 by 0 will always return {@link Double#NaN} regardless of the original types.</li>
 *                      <li>Trying to divide any other value by 0 will always return {@link Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY}.</li>
 *                  </ul>
 *              </li>
 *              <li>Adding a decimal type to an integer type will yield a decimal result.</li>
 *              <li>If any of the operands is an arbitrary precision number, the result will be of arbitrary precision</li>
 *          </ul>
 *      </li>
 *      <li>0<sup>0</sup> is defined to be 1</li>
 *      <li>Longs and doubles will give an arbitrary-precision result in case of overflow/underflow</li>
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
public class ExprNumberArithmetic implements Expression<Number> {

    private enum Operator {
        PLUS('+') {
            @Override
            public Number calculate(Number left, Number right) {
                if (left instanceof Long && right instanceof Long) {
                    long l = left.longValue();
                    long r = right.longValue();
                    try {
                        return Math.addExact(l, r);
                    } catch (ArithmeticException e) {
                        return BigInteger.valueOf(l).add(BigInteger.valueOf(r));
                    }
                } else if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).add((BigDecimal) right);
                    } else if (left instanceof BigDecimal) {
                        return ((BigDecimal) left).add(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return BigDecimalMath.getBigDecimal(left).add((BigDecimal) right);
                    }
                } else if (left instanceof BigInteger || right instanceof BigInteger) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).add((BigInteger) right);
                    } else if (left instanceof Double || right instanceof Double) {
                        return BigDecimalMath.getBigDecimal(left).add(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return BigDecimalMath.getBigInteger(left).add(BigDecimalMath.getBigInteger(right));
                    }
                } else {
                    // Both Double, or mix of Long and Double
                    double l = left.doubleValue();
                    double r = right.doubleValue();
                    double s = l + r;
                    if (Double.isInfinite(s) && Double.isFinite(l) && Double.isFinite(r)) {
                        return BigDecimalMath.getBigDecimal(left).add(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return s;
                    }
                }
            }
        },
        MINUS('-') {
            @Override
            public Number calculate(Number left, Number right) {
                if (left instanceof Long && right instanceof Long) {
                    long l = left.longValue();
                    long r = right.longValue();
                    try {
                        return Math.subtractExact(l, r);
                    } catch (ArithmeticException e) {
                        return BigInteger.valueOf(l).subtract(BigInteger.valueOf(r));
                    }
                } else if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).subtract((BigDecimal) right);
                    } else if (left instanceof BigDecimal) {
                        return ((BigDecimal) left).subtract(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return BigDecimalMath.getBigDecimal(left).subtract((BigDecimal) right);
                    }
                } else if (left instanceof BigInteger || right instanceof BigInteger) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).subtract((BigInteger) right);
                    } else if (left instanceof Double || right instanceof Double) {
                        return BigDecimalMath.getBigDecimal(left).subtract(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return BigDecimalMath.getBigInteger(left).subtract(BigDecimalMath.getBigInteger(right));
                    }
                } else {
                    // Both Double, or mix of Long and Double
                    double l = left.doubleValue();
                    double r = right.doubleValue();
                    double s = l - r;
                    if (Double.isInfinite(s) && Double.isFinite(l) && Double.isFinite(r)) {
                        return BigDecimalMath.getBigDecimal(left).subtract(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return s;
                    }
                }
            }
        },
        MULT('*') {
            @Override
            public Number calculate(Number left, Number right) {
                if (left instanceof Long && right instanceof Long) {
                    long l = left.longValue();
                    long r = right.longValue();
                    try {
                        return Math.multiplyExact(l, r);
                    } catch (ArithmeticException e) {
                        return BigInteger.valueOf(l).multiply(BigInteger.valueOf(r));
                    }
                } else if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).multiply((BigDecimal) right);
                    } else if (left instanceof BigDecimal) {
                        return ((BigDecimal) left).multiply(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return BigDecimalMath.getBigDecimal(left).multiply((BigDecimal) right);
                    }
                } else if (left instanceof BigInteger || right instanceof BigInteger) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).multiply((BigInteger) right);
                    } else if (left instanceof Double || right instanceof Double) {
                        return BigDecimalMath.getBigDecimal(left).multiply(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return BigDecimalMath.getBigInteger(left).multiply(BigDecimalMath.getBigInteger(right));
                    }
                } else {
                    // Both Double, or mix of Long and Double
                    double l = left.doubleValue();
                    double r = right.doubleValue();
                    double s = l * r;
                    if (Double.isInfinite(s) && Double.isFinite(l) && Double.isFinite(r)) {
                        return BigDecimalMath.getBigDecimal(left).multiply(BigDecimalMath.getBigDecimal(right));
                    } else {
                        return s;
                    }
                }
            }
        },
        DIV('/') {
            @Override
            public Number calculate(Number left, Number right) {
                if (isZero(left) && isZero(right)) {
                    return Double.NaN;
                } else if (isZero(right)) {
                    return Math.copySign(Double.POSITIVE_INFINITY, left.doubleValue());
                } else if ((left instanceof Long || right instanceof Long) && (left instanceof Double || right instanceof Double)) {
                    return left.doubleValue() / right.doubleValue();
                } else {
                    return BigDecimalMath.getBigDecimal(left).divide(BigDecimalMath.getBigDecimal(right), RoundingMode.HALF_UP);
                }
            }
        },
        EXP('^') {
            @Override
            public Number calculate(Number left, Number right) {
                if (isZero(right)) {
                    if ((left instanceof Long || left instanceof Double) && (right instanceof Long || right instanceof Double)) {
                        if (left instanceof Long && right instanceof Long) {
                            return 1L;
                        } else {
                            return 1.0;
                        }
                    } else if (left instanceof BigInteger) {
                        return BigInteger.ONE;
                    } else {
                        return BigDecimal.ONE;
                    }
                }
                if (left instanceof Long && right instanceof Long) {
                    double p = Math.pow(left.doubleValue(), right.doubleValue());
                    if (Double.isInfinite(p) || p > Long.MAX_VALUE) {
                        return pow(BigDecimalMath.getBigInteger(left), BigDecimalMath.getBigInteger(right));
                    } else {
                        return (long) p;
                    }
                } else if (left instanceof BigDecimal || right instanceof BigDecimal) {
                    return BigDecimalMath.pow(BigDecimalMath.getBigDecimal(left), BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
                } else if (left instanceof BigInteger || right instanceof BigInteger) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return pow((BigInteger) left, (BigInteger) right);
                    } else if (left instanceof Long || right instanceof Long) {
                        return pow(BigDecimalMath.getBigInteger(left), BigDecimalMath.getBigInteger(right));
                    } else {
                        return BigDecimalMath.pow(BigDecimalMath.getBigDecimal(left), BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
                    }
                } else {
                    // mix of Long and Double
                    double l = left.doubleValue();
                    double r = right.doubleValue();
                    double p = Math.pow(l, r);
                    if (Double.isInfinite(p) && Double.isFinite(l) && Double.isFinite(r)) {
                        return BigDecimalMath.pow(BigDecimalMath.getBigDecimal(left), BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
                    } else {
                        return p;
                    }
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
            return n instanceof BigDecimal && ((BigDecimal) n).compareTo(BigDecimal.ZERO) == 0 || n.doubleValue() == 0;
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

    public static final PatternInfos<Operator> PATTERNS = new PatternInfos<>(new Object[][]{
            {"%number%[ ]+[ ]%number%", Operator.PLUS},
            {"%number%[ ]-[ ]%number%", Operator.MINUS},
            {"%number%[ ]*[ ]%number%", Operator.MULT},
            {"%number%[ ]/[ ]%number%", Operator.DIV},
            {"%number%[ ]^[ ]%number%", Operator.EXP},
        }
    );

    static {
        Main.getMainRegistration().addExpression(
            ExprNumberArithmetic.class,
            Number.class,
            true,
            3,
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
                parseContext.getLogger().error("Cannot divide by 0 !", ErrorType.SEMANTIC_ERROR);
                return false;
            }
        }
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                .map(f -> (Number) f, s -> (Number) s)
                .or(() -> DoubleOptional.of(0, 0))
                .mapToOptional((f, s) -> new Number[]{ op.calculate(f, s) })
                .orElse(new Number[0]);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return first.toString(ctx, debug) + " " + op + " " + second.toString(ctx, debug);
    }

    @Override
    public Expression<? extends Number> simplify() {
        if (first instanceof Literal && second instanceof Literal)
            return new SimpleLiteral<>(Number.class, getValues(TriggerContext.DUMMY));
        return this;
    }

}
