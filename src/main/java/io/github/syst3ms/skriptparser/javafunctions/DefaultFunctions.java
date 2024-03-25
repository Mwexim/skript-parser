package io.github.syst3ms.skriptparser.javafunctions;

import io.github.syst3ms.skriptparser.structures.functions.FunctionParameter;
import io.github.syst3ms.skriptparser.structures.functions.Functions;
import io.github.syst3ms.skriptparser.structures.functions.JavaFunction;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultFunctions {

	static {
		Functions.registerFunction(new JavaFunction<>(
				"mod",
				new FunctionParameter[]{new FunctionParameter<>("d", BigInteger.class, true), new FunctionParameter<>("m", BigInteger.class, true)},
				Number.class,
				true) {
			@Override
			public Number[] executeSimple(Object[][] params) {
				Number d = (Number) params[0][0];
				Number m = (Number) params[1][0];
				double mm = m.doubleValue();
				if (mm == 0)
					return new Number[] {BigDecimal.valueOf(Double.NaN)};
				return new Number[] {BigDecimal.valueOf((d.doubleValue() % mm + mm) % mm)};
			}
		});
	}

}
