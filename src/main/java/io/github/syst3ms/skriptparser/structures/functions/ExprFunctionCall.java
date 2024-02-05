package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;

import java.util.Optional;
import java.util.regex.MatchResult;

public class ExprFunctionCall implements Expression<Object> {

	static {
		Parser.getMainRegistration().addExpression(
				ExprFunctionCall.class,
				Object.class,
				true,
				6,
				Functions.FUNCTION_CALL_PATTERN);
	}

	private Function<?> function;
	private Expression<?>[] paramsExprs = new Expression<?>[0];

	private Expression<?> parsedExpr;

	@Override
	public Object[] getValues(TriggerContext ctx) {
		Object[][] params = new Object[paramsExprs.length][];
		for (int i = 0; i < paramsExprs.length; i++) {
			params[i] = paramsExprs[i].getValues(ctx);
			Optional<? extends Object[]> converted = Converters.convertArray(params[i], function.getParameters()[i].getType());
			if (converted.isEmpty()) {
				params[i] = new Object[0];
			} else {
				params[i] = converted.get();
			}
		}
		Object[] o = function.execute(params, ctx);
		System.out.println(toString(ctx, true));
		return o;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		MatchResult result = parseContext.getMatches().get(0); // whole pattern matched because it is one single regex
		String functionName = result.group(1);
		Optional<Function<?>> optionalFunction = Functions.getFunctionByName(functionName, parseContext.getLogger().getFileName());
		SkriptLogger logger = parseContext.getLogger();
		if (optionalFunction.isEmpty()) {
			logger.error("No function was found under the name '" + functionName + "'", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		function = optionalFunction.get();
		FunctionParameter<?>[] functionParameters = function.getParameters();
		String exprString = result.group(2);
		PatternType<?> objectType = TypeManager.getPatternType("objects").get();
		Optional<? extends Expression<?>> optionalExpression =
				SyntaxParser.parseExpression(exprString, objectType, parseContext.getParserState(), logger);
		if (optionalExpression.isPresent()) {
			parsedExpr = optionalExpression.get();
			if (functionParameters.length == 0) {
				logger.error("This function has no parameters, but 1 or more parameters were provided.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			if (parsedExpr instanceof ExpressionList<?> expressionList) {
				paramsExprs = expressionList.getExpressions();
				if (!(functionParameters.length == 1 && !functionParameters[0].isSingle())) { // allows for function f(ints: ints) | f(1, 2, 3, 4)
					if (paramsExprs.length != functionParameters.length) {
						logger.error("This function requires " + functionParameters.length + " parameters, but "
											 + paramsExprs.length + " were given.", ErrorType.SEMANTIC_ERROR);
						return false;
					}
					for (int i = 0; i < functionParameters.length; i++) {
						FunctionParameter<?> functionParameter = functionParameters[i];
						Expression<?> providedParamExpr = paramsExprs[i];
						if (functionParameter.isSingle() && !providedParamExpr.isSingle()) {
							logger.error("The '" + functionParameter.getName() + "' parameter accepts a single " +
												 "value, but was given more.", ErrorType.SEMANTIC_ERROR);
							return false;
						}
						// if (!functionParameter.getType().isAssignableFrom(providedParamExpr.getReturnType())) { // no converter check
						if (!functionParameter.getType().isAssignableFrom(providedParamExpr.getReturnType())
									&& !Converters.converterExists(functionParameter.getType(), providedParamExpr.getReturnType())) {
							String typeText = TypeManager.getByClass(functionParameter.getType()).get().withIndefiniteArticle(false);
							logger.error("The type of the provided value for the '" + functionParameter.getName()
												 + "' parameter is not " + typeText + "/couldn't be converted to "
												 + typeText, ErrorType.SEMANTIC_ERROR);
							return false;
						}
					}
				} else {
					paramsExprs = new Expression<?>[]{parsedExpr}; // single parameter setting it to multiple values
				}
			} else {
				paramsExprs = new Expression<?>[]{parsedExpr}; // required for function calling otherwise it can break
			}
		}
		else if (functionParameters.length > 0) {
			logger.error("The function has more than 1 parameter, but none were provided.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return function.getName() + "(" + (parsedExpr != null ? parsedExpr.toString(ctx, debug) : "") + ")";
	}

}
