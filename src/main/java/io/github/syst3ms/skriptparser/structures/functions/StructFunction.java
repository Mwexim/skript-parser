package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;

public class StructFunction extends Structure {

	static {
		Parser.getMainRegistration()
				.newEvent(StructFunction.class, "*[:local[ ]] func[tion] <" + Functions.FUNCTION_NAME_REGEX + ">" +
														"\\([params:%*functionparameters%]\\)[return: \\:\\: <.+>]")
				.setHandledContexts(FunctionContext.class)
				.register();
	}

	private boolean local = false;
	private String functionName;
	private Literal<FunctionParameter<?>> params;
	private boolean returnSingle = true;
	private String rawReturnType;

	private ScriptFunction<?> function; // to be registered in the register method

	@Override
	public boolean check(TriggerContext ctx) {
		return ctx instanceof FunctionContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		local = parseContext.hasMark("local");
		functionName = parseContext.getMatches().get(0).group();
		SkriptLogger logger =parseContext.getLogger();
		if (!Functions.isValidFunctionName(functionName)) {
			logger.error("'" + functionName + "' is not a valid function name.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		FunctionParameter<?>[] parameters = null;
		if (parseContext.getMarks().contains("params")) {
			params = (Literal<FunctionParameter<?>>) expressions[0];
			parameters = params.getValues();
			for (FunctionParameter<?> parameter : parameters) {
				for (FunctionParameter<?> p : parameters) {
					if (p == parameter) continue;
					if (parameter.getName().equals(p.getName())) {
						logger.error("Functions parameters cannot have the same name.", ErrorType.SEMANTIC_ERROR);
						return false;
					}
				}
			}
		}
		Class<?> returnType = null;
		if (parseContext.getMarks().contains("return")) {
			rawReturnType = parseContext.getMatches().get(1).group();
			Type<?> type = TypeManager.parseType(rawReturnType);
			if (type == null) {
				logger.error("The type provided was unable to be parsed.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			returnType = type.getTypeClass();
			if (returnType == FunctionParameter.class) {
				logger.error("This type should not be used as a function's return type.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			if (type.isPlural(rawReturnType)) returnSingle = false;
		}
		function = new ScriptFunction<>(parseContext.getLogger().getFileName(), local, functionName, parameters, returnType, returnSingle);
		if (!Functions.isValidFunction(function, parseContext.getLogger())) {
			return false;
		}
		Functions.preRegisterFunction(function);
		return true;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return (local ? "local " : "") + "function " + functionName + "(" + params.toString(ctx, debug) + ")" +
					   (rawReturnType == null ? "" : " :: " + rawReturnType);
	}

	public String getStringName() {
		return functionName;
	}

	public void register(Trigger trigger) {
		Functions.registerFunction(function, trigger);
	}

}
