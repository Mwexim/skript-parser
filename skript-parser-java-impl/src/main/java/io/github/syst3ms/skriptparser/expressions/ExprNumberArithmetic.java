package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class ExprNumberArithmetic implements Expression<Number> {
    public static final RoundingMode DEFAULT_ROUDING_MODE = RoundingMode.HALF_UP;

    private enum Operator {
        PLUS('+') {
            @Override
            public Number calculate(Number left, Number right, boolean integer) {
                if (integer) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).add((BigInteger) right);
                    } else if (left instanceof Long && right instanceof Long) {
                        return left.longValue() + right.longValue();
                    } else {
                        return BigInteger.valueOf(left.longValue()).add(BigInteger.valueOf(right.longValue()));
                    }
                } else {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).add((BigDecimal) right);
                    } else if (left instanceof Double && right instanceof Double) {
                        return left.doubleValue() + right.doubleValue();
                    } else {
                        return new BigDecimal(left.toString()).add(new BigDecimal(right.toString()));
                    }
                }
            }
        },
        MINUS('-') {
            @Override
            public Number calculate(Number left, Number right, boolean integer) {
                if (integer) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).subtract((BigInteger) right);
                    } else if (left instanceof Long && right instanceof Long) {
                        return left.longValue() + right.longValue();
                    } else {
                        return BigInteger.valueOf(left.longValue()).add(BigInteger.valueOf(right.longValue()));
                    }
                } else {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).add((BigDecimal) right);
                    } else if (left instanceof Double && right instanceof Double) {
                        return left.doubleValue() + right.doubleValue();
                    } else {
                        return new BigDecimal(left.toString()).add(new BigDecimal(right.toString()));
                    }
                }
            }
        },
        MULT('*') {
            @Override
            public Number calculate(Number left, Number right, boolean integer) {
                if (integer) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).multiply((BigInteger) right);
                    } else if (left instanceof Long && right instanceof Long) {
                        return left.longValue() * right.longValue();
                    } else {
                        return BigInteger.valueOf(left.longValue()).multiply(BigInteger.valueOf(right.longValue()));
                    }
                } else {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).multiply((BigDecimal) right);
                    } else if (left instanceof Double && right instanceof Double) {
                        return left.doubleValue() * right.doubleValue();
                    } else {
                        return new BigDecimal(left.toString()).multiply(new BigDecimal(right.toString()));
                    }
                }
            }
        },
        DIV('/') {
            @Override
            public Number calculate(Number left, Number right, boolean integer) {
                if (integer) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).divide((BigInteger) right);
                    } else if (left instanceof Long && right instanceof Long) {
                        return left.longValue() / right.longValue();
                    } else {
                        return BigInteger.valueOf(left.longValue()).divide(BigInteger.valueOf(right.longValue()));
                    }
                } else {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).divide((BigDecimal) right, DEFAULT_ROUDING_MODE);
                    } else if (left instanceof Double && right instanceof Double) {
                        return left.doubleValue() / right.doubleValue();
                    } else {
                        return new BigDecimal(left.toString()).divide(new BigDecimal(right.toString()), DEFAULT_ROUDING_MODE);
                    }
                }
            }
        },
        EXP('^') {
            @Override
            public Number calculate(Number left, Number right, boolean integer) {
                if (integer) {
                    if (left instanceof BigInteger && right instanceof BigInteger) {
                        return ((BigInteger) left).pow(right.intValue());
                    } else if (left instanceof Long && right instanceof Long) {
                        return (long) Math.pow(left.longValue(), right.longValue());
                    } else {
                        return BigInteger.valueOf(left.longValue()).pow(right.intValue());
                    }
                } else {
                    if (left instanceof BigDecimal && right instanceof BigDecimal) {
                        return ((BigDecimal) left).pow(right.intValue());
                    } else if (left instanceof Double && right instanceof Double) {
                        return Math.pow(left.doubleValue(), right.doubleValue());
                    } else {
                        return new BigDecimal(left.toString()).pow(right.intValue());
                    }
                }
            }
        };

        public final char sign;

        Operator(char sign) {
            this.sign = sign;
        }

        public abstract Number calculate(Number left, Number right, boolean integer);

        @Override
        public String toString() {
            return "" + sign;
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
        Main.getMainRegistration()
            .addExpression(ExprNumberArithmetic.class, Number.class, true, PATTERNS.getPatterns());
    }

    private Expression<? extends Number> first, second;
    private Operator op;
    private Class<? extends Number> returnType;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, ParseResult parseResult) {
        first = (Expression<? extends Number>) exprs[0];
        second = (Expression<? extends Number>) exprs[1];
        op = PATTERNS.getInfo(matchedPattern);
        returnType = getReturnType(op, first.getReturnType(), second.getReturnType());
        return true;
    }

    private Class<? extends Number> getReturnType(Operator op, Class<? extends Number> first, Class<? extends Number> second) {
        if (op == Operator.EXP || op == Operator.DIV) {
            return first == BigDecimal.class || second == BigDecimal.class ? BigDecimal.class : Double.class;
        } else if (first == Number.class || second == Number.class) {
            return Number.class;
        } if (first == BigDecimal.class || second == BigDecimal.class) {
            return BigDecimal.class;
        } else if (first == Double.class || second == Double.class) {
            return Double.class;
        } else if (first == BigInteger.class || second == BigInteger.class) {
            return BigInteger.class;
        } else {
            return Long.class;
        }
    }

    @Override
    public Number[] getValues(Event e) {
        Number[] one = (Number[]) Array.newInstance(returnType, 1);
        Number n1 = first.getSingle(e), n2 = second.getSingle(e);
        if (n1 == null)
            n1 = 0;
        if (n2 == null)
            n2 = 0;
        boolean integer = (n1 instanceof Long || n1 instanceof BigInteger) && (n2 instanceof Long || n2 instanceof BigInteger);
        one[0] = op.calculate(n1, n2, integer);
        return one;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return returnType;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return first.toString(e, debug) + " " + op + " " + second.toString(e, debug);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Expression<? extends Number> simplify() {
        if (first instanceof Literal && second instanceof Literal)
            return new SimpleLiteral<>(Number.class, getValues(Event.DUMMY));
        return this;
    }

}
