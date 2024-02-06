package io.github.syst3ms.skriptparser.expressions.arithmetic;

import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;

public class DefaultOperations {

	static {
		// Number - Number
		Arithmetics.registerOperation(Operator.ADDITION, Number.class, (left, right) -> {
			if (left instanceof BigDecimal || right instanceof BigDecimal) {
				var l = BigDecimalMath.getBigDecimal(left);
				var r = BigDecimalMath.getBigDecimal(right);
				return l.add(r);
			} else {
				assert left instanceof BigInteger && right instanceof BigInteger;
				return ((BigInteger) left).add(((BigInteger) right));
			}
		});
		Arithmetics.registerOperation(Operator.SUBTRACTION, Number.class, (left, right) -> {
			if (left instanceof BigDecimal || right instanceof BigDecimal) {
				var l = BigDecimalMath.getBigDecimal(left);
				var r = BigDecimalMath.getBigDecimal(right);
				return l.subtract(r);
			} else {
				assert left instanceof BigInteger && right instanceof BigInteger;
				return ((BigInteger) left).subtract(((BigInteger) right));
			}
		});
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Number.class, (left, right) -> {
			if (left instanceof BigDecimal || right instanceof BigDecimal) {
				var l = BigDecimalMath.getBigDecimal(left);
				var r = BigDecimalMath.getBigDecimal(right);
				return l.multiply(r);
			} else {
				assert left instanceof BigInteger && right instanceof BigInteger;
				return ((BigInteger) left).multiply(((BigInteger) right));
			}
		});
		Arithmetics.registerOperation(Operator.DIVISION, Number.class, (left, right) -> {
			if (isZero(right)) {
				return BigInteger.ZERO;
			} else {
				return BigDecimalMath.getBigDecimal(left).divide(BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
			}
		});
		Arithmetics.registerOperation(Operator.EXPONENTIATION, Number.class, (left, right) -> {
			if (isZero(right)) {
				return left instanceof BigDecimal ? BigDecimal.ONE : BigInteger.ONE;
			}
			if (left instanceof BigDecimal || right instanceof BigDecimal) {
				return BigDecimalMath.pow(BigDecimalMath.getBigDecimal(left), BigDecimalMath.getBigDecimal(right), BigDecimalMath.DEFAULT_CONTEXT);
			} else {
				assert left instanceof BigInteger && right instanceof BigInteger;
				return pow((BigInteger) left, (BigInteger) right);
			}
		});
		Arithmetics.registerDifference(Number.class, (left, right) -> {
			if (left instanceof BigDecimal || right instanceof BigDecimal) {
				var l = BigDecimalMath.getBigDecimal(left);
				var r = BigDecimalMath.getBigDecimal(right);
				return l.subtract(r).abs();
			} else {
				assert left instanceof BigInteger && right instanceof BigInteger;
				return ((BigInteger) left).subtract(((BigInteger) right)).abs();
			}
		});
		Arithmetics.registerDefaultValue(Number.class, () -> BigInteger.ZERO);

		/*// Vector - Vector
		Arithmetics.registerOperation(Operator.ADDITION, Vector.class, (left, right) -> left.clone().add(right));
		Arithmetics.registerOperation(Operator.SUBTRACTION, Vector.class, (left, right) -> left.clone().subtract(right));
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Vector.class, (left, right) -> left.clone().multiply(right));
		Arithmetics.registerOperation(Operator.DIVISION, Vector.class, (left, right) -> left.clone().divide(right));
		Arithmetics.registerDifference(Vector.class,
				(left, right) -> new Vector(Math.abs(left.getX() - right.getX()), Math.abs(left.getY() - right.getY()), Math.abs(left.getZ() - right.getZ())));
		Arithmetics.registerDefaultValue(Vector.class, Vector::new);

		// Vector - Number
		// Number - Vector
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Vector.class, Number.class, (left, right) -> left.clone().multiply(right.doubleValue()), (left, right) -> {
			double number = left.doubleValue();
			Vector leftVector = new Vector(number, number, number);
			return leftVector.multiply(right);
		});
		Arithmetics.registerOperation(Operator.DIVISION, Vector.class, Number.class, (left, right) -> {
			double number = right.doubleValue();
			Vector rightVector = new Vector(number, number, number);
			return left.clone().divide(rightVector);
		}, (left, right) -> {
			double number = left.doubleValue();
			Vector leftVector = new Vector(number, number, number);
			return leftVector.divide(right);
		});*/

		// Timespan - Timespan
		Arithmetics.registerOperation(Operator.ADDITION, Duration.class, (left, right) -> Duration.ofMillis(left.toMillis() + right.toMillis()));
		Arithmetics.registerOperation(Operator.SUBTRACTION, Duration.class, (left, right) -> Duration.ofMillis(Math.max(0, left.toMillis() - right.toMillis())));
		Arithmetics.registerDifference(Duration.class, (left, right) -> Duration.ofMillis(Math.abs(left.toMillis() - right.toMillis())));
		Arithmetics.registerDefaultValue(Duration.class, () -> Duration.ofMillis(0));

		// Duration - Number
		// Number - Duration
		Arithmetics.registerOperation(Operator.MULTIPLICATION, Duration.class, Number.class, (left, right) -> {
			long scalar = right.longValue();
			if (scalar < 0)
				return null;
			return Duration.ofMillis(left.toMillis() * scalar);
		}, (left, right) -> {
			long scalar = left.longValue();
			if (scalar < 0)
				return null;
			return Duration.ofMillis(scalar * right.toMillis());
		});
		Arithmetics.registerOperation(Operator.DIVISION, Duration.class, Number.class, (left, right) -> {
			long scalar = right.longValue();
			if (scalar <= 0)
				return null;
			return Duration.ofMillis(left.toMillis() / scalar);
		});

		// Date - Duration
		Arithmetics.registerOperation(Operator.ADDITION, SkriptDate.class, Duration.class, SkriptDate::plus);
		Arithmetics.registerOperation(Operator.SUBTRACTION, SkriptDate.class, Duration.class, SkriptDate::minus);
		Arithmetics.registerDifference(SkriptDate.class, Duration.class, SkriptDate::difference);

		// Time - Duration
		Arithmetics.registerOperation(Operator.ADDITION, Time.class, Duration.class, Time::plus);
		Arithmetics.registerOperation(Operator.SUBTRACTION, Time.class, Duration.class, Time::minus);
		Arithmetics.registerDifference(Time.class, Duration.class, Time::difference);

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
