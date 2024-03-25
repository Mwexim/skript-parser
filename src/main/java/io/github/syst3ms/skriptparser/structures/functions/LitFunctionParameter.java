package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;

@SuppressWarnings("rawtypes")
public class LitFunctionParameter implements Literal<FunctionParameter> {

	static {
		Parser.getMainRegistration().addExpression(
				LitFunctionParameter.class,
				FunctionParameter.class,
				true,
				"<" + Functions.FUNCTION_NAME_REGEX + ">\\: <.+>"
		);
	}

	private String name;
	private String rawType;
	private Type<?> type;
	private Class<?> typeClass;
	private boolean single = true;

	@Override
	public FunctionParameter<?>[] getValues() {
		return new FunctionParameter[] {new FunctionParameter<>(name, typeClass, single)};
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		name = parseContext.getMatches().get(0).group();
		rawType = parseContext.getMatches().get(1).group(); // I have to use generic regex and parse it manually so I can get the raw string to use in this method
		type = TypeManager.parseType(rawType);
		SkriptLogger logger = parseContext.getLogger();
		if (type == null) {
			logger.error("The type provided was unable to be parsed.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		typeClass = type.getTypeClass();
		if (typeClass == FunctionParameter.class) {
			logger.error("This type should not be used as a parameter's type.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		if (type.isPlural(rawType)) single = false;
		/*typeLiteral = ((Literal<Type<?>>) expressions[0]);
		type = typeLiteral.getSingle().orElseThrow(AssertionError::new);
		typeClass = type.getTypeClass();
		if (typeClass == FunctionParameter.class) {
			parseContext.getLogger().error("This type should not be used as a parameter's type.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		String rawType = parseContext.getMatches().get(0).group();
		if (type.isPlural(rawType)) single = false;*/
		return true;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return name + ": " + rawType;
	}

}
