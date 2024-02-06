/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package io.github.syst3ms.skriptparser.expressions.arithmetic;


import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("null")
public class ExprArithmetic<L, R, T> implements Expression<T> {

	private static final Class<?>[] INTEGER_CLASSES = {Long.class, Integer.class, Short.class, Byte.class};

	private static class PatternInfo {
		public final Operator operator;
		public final boolean leftGrouped;
		public final boolean rightGrouped;

		public PatternInfo(Operator operator, boolean leftGrouped, boolean rightGrouped) {
			this.operator = operator;
			this.leftGrouped = leftGrouped;
			this.rightGrouped = rightGrouped;
		}
	}

	private static final PatternInfos<PatternInfo> patterns = new PatternInfos<>(new Object[][] {

		{"\\(%object%\\)[ ]+[ ]\\(%object%\\)", new PatternInfo(Operator.ADDITION, true, true)},
		{"\\(%object%\\)[ ]+[ ]%object%", new PatternInfo(Operator.ADDITION, true, false)},
		{"%object%[ ]+[ ]\\(%object%\\)", new PatternInfo(Operator.ADDITION, false, true)},
		{"%object%[ ]+[ ]%object%", new PatternInfo(Operator.ADDITION, false, false)},

		{"\\(%object%\\)[ ]-[ ]\\(%object%\\)", new PatternInfo(Operator.SUBTRACTION, true, true)},
		{"\\(%object%\\)[ ]-[ ]%object%", new PatternInfo(Operator.SUBTRACTION, true, false)},
		{"%object%[ ]-[ ]\\(%object%\\)", new PatternInfo(Operator.SUBTRACTION, false, true)},
		{"%object%[ ]-[ ]%object%", new PatternInfo(Operator.SUBTRACTION, false, false)},

		{"\\(%object%\\)[ ]*[ ]\\(%object%\\)", new PatternInfo(Operator.MULTIPLICATION, true, true)},
		{"\\(%object%\\)[ ]*[ ]%object%", new PatternInfo(Operator.MULTIPLICATION, true, false)},
		{"%object%[ ]*[ ]\\(%object%\\)", new PatternInfo(Operator.MULTIPLICATION, false, true)},
		{"%object%[ ]*[ ]%object%", new PatternInfo(Operator.MULTIPLICATION, false, false)},

		{"\\(%object%\\)[ ]/[ ]\\(%object%\\)", new PatternInfo(Operator.DIVISION, true, true)},
		{"\\(%object%\\)[ ]/[ ]%object%", new PatternInfo(Operator.DIVISION, true, false)},
		{"%object%[ ]/[ ]\\(%object%\\)", new PatternInfo(Operator.DIVISION, false, true)},
		{"%object%[ ]/[ ]%object%", new PatternInfo(Operator.DIVISION, false, false)},

		{"\\(%object%\\)[ ]^[ ]\\(%object%\\)", new PatternInfo(Operator.EXPONENTIATION, true, true)},
		{"\\(%object%\\)[ ]^[ ]%object%", new PatternInfo(Operator.EXPONENTIATION, true, false)},
		{"%object%[ ]^[ ]\\(%object%\\)", new PatternInfo(Operator.EXPONENTIATION, false, true)},
		{"%object%[ ]^[ ]%object%", new PatternInfo(Operator.EXPONENTIATION, false, false)},

	});

	static {
		//noinspection unchecked
		//Skript.registerExpression(ExprArithmetic.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
		Parser.getMainRegistration().addExpression(
				ExprArithmetic.class,
				Object.class,
				true,
				0,
				patterns.getPatterns());
	}

	private Expression<L> first;
	private Expression<R> second;
	private Operator operator;

	private Class<? extends T> returnType;

	// A chain of expressions and operators, alternating between the two. Always starts and ends with an expression.
	private final List<Object> chain = new ArrayList<>();

	// A parsed chain, like a tree
	private ArithmeticGettable<? extends T> arithmeticGettable;

	private boolean leftGrouped, rightGrouped;

	@Override
	@SuppressWarnings({"ConstantConditions", "rawtypes", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, ParseContext parseContext) {
		first = (Expression<L>) exprs[0];
		second = (Expression<R>) exprs[1];

		PatternInfo patternInfo = patterns.getInfo(matchedPattern);
		leftGrouped = patternInfo.leftGrouped;
		rightGrouped = patternInfo.rightGrouped;
		operator = patternInfo.operator;


		/*
		 * Step 2: Return Type Calculation
		 *
		 * After the first step, everything that can be known about 'first' and 'second' during parsing is known.
		 * As a result, it is time to determine the return type of the operation.
		 *
		 * If the types of 'first' or 'second' are object, it is possible that multiple operations with different return types
		 *  will be found. If that is the case, the supertype of these operations will be the return type (can be object).
		 * If the types of both are object (e.g. variables), the return type will be object (have to wait until runtime and hope it works).
		 * Of course, if no operations are found, init will fail.
		 *
		 * After these checks, it is safe to assume returnType has a value, as init should have failed by now if not.
		 * One final check is performed specifically for numerical types.
		 * Any numerical operation involving division or exponents have a return type of Double.
		 * Other operations will also return Double, UNLESS 'first' and 'second' are of integer types, in which case the return type will be Long.
		 *
		 * If the types of both are something meaningful, the search for a registered operation commences.
		 * If no operation can be found, init will fail.
		 */

		Class<? extends L> firstClass = first.getReturnType();
		Class<? extends R> secondClass = second.getReturnType();

		if (firstClass == Object.class || secondClass == Object.class) {
			// if either of the types is unknown, then we resolve the operation at runtime
			Class<?>[] returnTypes = null;
			if (!(firstClass == Object.class && secondClass == Object.class)) { // both aren't object
				if (firstClass == Object.class) {
					returnTypes = Arithmetics.getOperations(operator).stream()
							.filter(info -> info.getRight().isAssignableFrom(secondClass))
							.map(OperationInfo::getReturnType)
							.toArray(Class[]::new);
				} else { // secondClass is Object
					returnTypes = Arithmetics.getOperations(operator, firstClass).stream()
						.map(OperationInfo::getReturnType)
						.toArray(Class[]::new);
				}
			}
			if (returnTypes == null) { // both are object; can't determine anything
				returnType = (Class<? extends T>) Object.class;
			} else if (returnTypes.length == 0) { // one of the classes is known but doesn't have any operations
				return error(firstClass, secondClass, parseContext.getLogger());
			} else {
				returnType = (Class<? extends T>) TypeManager.getByClass(getSuperClass(returnTypes)).map(Type::getTypeClass).orElse(null);
			}
		} else if (returnType == null) { // lookup
			OperationInfo<L, R, T> operationInfo = (OperationInfo<L, R, T>) Arithmetics.lookupOperationInfo(operator, firstClass, secondClass);
			if (operationInfo == null) // we error if we couldn't find an operation between the two types
				return error(firstClass, secondClass, parseContext.getLogger());
			returnType = operationInfo.getReturnType();
		}

		// ensure proper return types for numerical operations
		if (Number.class.isAssignableFrom(returnType)) {
			if (operator == Operator.DIVISION || operator == Operator.EXPONENTIATION) {
				returnType = (Class<? extends T>) Double.class;
			} else {
				boolean firstIsInt = false;
				boolean secondIsInt = false;
				for (Class<?> i : INTEGER_CLASSES) {
					firstIsInt |= i.isAssignableFrom(first.getReturnType());
					secondIsInt |= i.isAssignableFrom(second.getReturnType());
				}
				returnType = (Class<? extends T>) (firstIsInt && secondIsInt ? Long.class : Double.class);
			}
		}

		/*
		 * Step 3: Chaining and Parsing
		 *
		 * This step builds the arithmetic chain that will be parsed into an ordered operation to be executed at runtime.
		 * With larger operations, it is possible that 'first' or 'second' will be instances of ExprArithmetic.
		 * As a result, their chains need to be incorporated into this instance's chain.
		 * This is to ensure that, during parsing, a "gettable" that follows the order of operations is built.
		 * However, in the case of parentheses, the chains will not be combined as the
		 *  order of operations dictates that the result of that chain be determined first.
		 *
		 * The chain (a list of values and operators) will then be parsed into a "gettable" that
		 *  can be evaluated during runtime for a final result.
		 */

		if (first instanceof ExprArithmetic && !leftGrouped) { // combine chain of 'first' if we do not have parentheses
			chain.addAll(((ExprArithmetic<?, ?, L>) first).chain);
		} else {
			chain.add(first);
		}
		chain.add(operator);
		if (second instanceof ExprArithmetic && !rightGrouped) { // combine chain of 'second' if we do not have parentheses
			chain.addAll(((ExprArithmetic<?, ?, R>) second).chain);
		} else {
			chain.add(second);
		}

		arithmeticGettable = ArithmeticChain.parse(chain);
		return arithmeticGettable != null || error(firstClass, secondClass, parseContext.getLogger());
	}

	@Override
	@SuppressWarnings("unchecked")
	public T[] getValues(TriggerContext ctx) {
		T result = arithmeticGettable.get(ctx);
		T[] one = (T[]) Array.newInstance(result == null ? returnType : result.getClass(), 1);
		one[0] = result;
		return one;
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	private boolean error(Class<?> firstClass, Class<?> secondClass, SkriptLogger logger) {
		Type<?> first = TypeManager.getByClass(firstClass).get(), second = TypeManager.getByClass(secondClass).get();
		if (first.getTypeClass() != Object.class && second.getTypeClass() != Object.class) { // errors with "object" are not very useful and often misleading
			logger.error(operator.getName() + " can't be performed on " + first.withIndefiniteArticle(false) + " and " + second.withIndefiniteArticle(false), ErrorType.SEMANTIC_ERROR);
		}
		return false;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		String one = first.toString(ctx, debug);
		String two = second.toString(ctx, debug);
		if (leftGrouped)
			one = '(' + one + ')';
		if (rightGrouped)
			two = '(' + two + ')';
		return one + ' ' + operator + ' ' + two;
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> Class<? super T> getSuperClass(Class<? extends T>... classes) {
		if (classes.length == 0)
			return Object.class;
		Class<? super T> currentSuperclass = null;
		for (Class<? extends T> c : classes) {
			if (currentSuperclass == null) {
				currentSuperclass = (Class<? super T>) c.getSuperclass();
				continue;
			}
			while (!currentSuperclass.isAssignableFrom(c))
				currentSuperclass = currentSuperclass.getSuperclass();
		}
		return currentSuperclass;
	}

}
