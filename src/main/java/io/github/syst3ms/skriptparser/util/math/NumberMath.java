package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Utilities for various math operations using the generic {@link Number} type
 */
public class NumberMath {
    private static final BigDecimal RADIANS_TO_DEGREES = new BigDecimal(180).divide(BigDecimalMath.pi(BigDecimalMath.DEFAULT_CONTEXT), BigDecimalMath.DEFAULT_CONTEXT);
    private static final BigDecimal DEGREES_TO_RADIANS = BigDecimalMath.pi(BigDecimalMath.DEFAULT_CONTEXT).divide(new BigDecimal(180), BigDecimalMath.DEFAULT_CONTEXT);

    public static Number abs(Number n) {
        if (n instanceof Long) {
            return Math.abs(n.longValue());
        } else if (n instanceof Double) {
            return Math.abs(n.doubleValue());
        } else if (n instanceof BigInteger) {
            return ((BigInteger) n).abs();
        } else if (n instanceof BigDecimal) {
            return ((BigDecimal) n).abs();
        }
        throw new IllegalArgumentException();
    }

    public static Number sqrt(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.sqrt(n.doubleValue());
        } else {
            return BigDecimalMath.sqrt(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number ln(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.log(n.doubleValue());
        } else {
            return BigDecimalMath.log(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number log(Number base, Number n) {
        if ((n instanceof Long || n instanceof Double) && (base instanceof Long || base instanceof Double)) {
            return Math.log(n.doubleValue()) / Math.log(base.doubleValue());
        } else {
            BigDecimal bd = bigToBigDecimal(n);
            BigDecimal bdBase = bigToBigDecimal(base);
            if (bdBase.compareTo(BigDecimal.valueOf(2)) == 0) {
                return BigDecimalMath.log2(bd, BigDecimalMath.DEFAULT_CONTEXT);
            } else if (bdBase.compareTo(BigDecimal.TEN) == 0) {
                return BigDecimalMath.log10(bd, BigDecimalMath.DEFAULT_CONTEXT);
            } else {
                return BigDecimalMath.log(bd, BigDecimalMath.DEFAULT_CONTEXT)
                                     .divide(BigDecimalMath.log(bdBase, BigDecimalMath.DEFAULT_CONTEXT), BigDecimalMath.DEFAULT_ROUNDING_MODE);
            }
        }
    }

    public static Number factorial(Number n) {
        if (n instanceof Long && n.longValue() < 13)
            return BigDecimalMath.factorial(n.intValue()).longValue();
        BigDecimal fac = BigDecimalMath.factorial(new BigDecimal(n.toString()), BigDecimalMath.DEFAULT_CONTEXT);
        if (n instanceof Long || n instanceof BigInteger) {
            return fac.toBigInteger();
        } else {
            return fac;
        }
    }

    public static Number floor(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.floor(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).setScale(0, RoundingMode.FLOOR);
        }
    }

    public static Number ceil(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.ceil(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).setScale(0, RoundingMode.CEILING);
        }
    }

    public static Number round(Number n) {
        if (n instanceof Long || n instanceof BigInteger) {
            return n;
        } else if (n instanceof Double) {
            return Math.round(n.doubleValue());
        } else {
            assert n instanceof BigDecimal;
            return ((BigDecimal) n).setScale(0, RoundingMode.HALF_EVEN);
        }
    }

    public static Number sin(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.sin(Math.toDegrees(n.doubleValue()));
        } else {
            return BigDecimalMath.sin(bigToBigDecimal(n).multiply(DEGREES_TO_RADIANS), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number cos(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.cos(Math.toDegrees(n.doubleValue()));
        } else {
            return BigDecimalMath.cos(bigToBigDecimal(n).multiply(DEGREES_TO_RADIANS), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number tan(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.tan(Math.toDegrees(n.doubleValue()));
        } else {
            return BigDecimalMath.tan(bigToBigDecimal(n).multiply(DEGREES_TO_RADIANS), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number asin(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.asin(n.doubleValue());
        } else {
            return BigDecimalMath.asin(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT).multiply(RADIANS_TO_DEGREES);
        }
    }

    public static Number acos(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.acos(n.doubleValue());
        } else {
            return BigDecimalMath.acos(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT).multiply(RADIANS_TO_DEGREES);
        }
    }

    public static Number atan(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.atan(n.doubleValue());
        } else {
            return BigDecimalMath.atan(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT).multiply(RADIANS_TO_DEGREES);
        }
    }

    public static Number sinh(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.sinh(n.doubleValue());
        } else {
            return BigDecimalMath.exp(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT)
                    .subtract(BigDecimalMath.exp(bigToBigDecimal(n).negate(), BigDecimalMath.DEFAULT_CONTEXT))
                    .divide(BigDecimal.valueOf(2), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number cosh(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.cosh(n.doubleValue());
        } else {
            return BigDecimalMath.exp(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT)
                    .add(BigDecimalMath.exp(bigToBigDecimal(n).negate(), BigDecimalMath.DEFAULT_CONTEXT))
                    .divide(BigDecimal.valueOf(2), BigDecimalMath.DEFAULT_CONTEXT);
        }
    }

    public static Number tanh(Number n) {
        if (n instanceof Long || n instanceof Double) {
            return Math.tanh(n.doubleValue());
        } else {
            return BigDecimalMath.exp(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT)
                    .subtract(BigDecimalMath.exp(bigToBigDecimal(n).negate(), BigDecimalMath.DEFAULT_CONTEXT))
                    .divide(
                            BigDecimalMath.exp(bigToBigDecimal(n), BigDecimalMath.DEFAULT_CONTEXT)
                                    .add(BigDecimalMath.exp(bigToBigDecimal(n).negate(), BigDecimalMath.DEFAULT_CONTEXT)),
                            BigDecimalMath.DEFAULT_CONTEXT
                    );
        }
    }

    private static BigDecimal bigToBigDecimal(Number n) {
        return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal((BigInteger) n);
    }

}
