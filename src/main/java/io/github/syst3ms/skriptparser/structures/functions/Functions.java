package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Functions {

	private static final List<Function<?>> functions = new ArrayList<>();

	static final String FUNCTION_NAME_REGEX = "^[a-zA-Z0-9_]*";
	private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile(FUNCTION_NAME_REGEX);
	static final String FUNCTION_CALL_PATTERN = "<(" + Functions.FUNCTION_NAME_REGEX + ")\\((.*)\\)>";

	private Functions() {}

	static void preRegisterFunction(ScriptFunction<?> function) {
		functions.add(function);
	}

	public static void registerFunction(ScriptFunction<?> function, Trigger trigger) {
		function.setTrigger(trigger);
	}

	public static void registerFunction(JavaFunction<?> function) {
		functions.add(function);
	}

	public static boolean isValidFunction(ScriptFunction<?> function, SkriptLogger logger) {
		for (Function<?> registeredFunction : functions) {
			String registeredFunctionName = registeredFunction.getName();
			String providedFunctionName = function.getName();
			if (!registeredFunctionName.equals(providedFunctionName)) continue;
			if (registeredFunction instanceof JavaFunction<?>) { // java functions take precedence over any script function
				logger.error("A java function already exists with the name '" + providedFunctionName + "'.",
						ErrorType.SEMANTIC_ERROR);
				return false;
			}
			ScriptFunction<?> registeredScriptFunction = (ScriptFunction<?>) registeredFunction;
			String registeredScriptName = registeredScriptFunction.getScriptName();
			if (!registeredScriptFunction.isLocal()) { // already registered function is global so it takes name precedence
				logger.error("A global script function named '" + providedFunctionName + "' already exists in " +
									  registeredScriptName + ".", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			if (!function.isLocal()) {
				// if a global function is trying to be defined when a local function already has that name, there will be problems in the script where the local function lies
				logger.error("A local script function named '" + providedFunctionName + "' already exists in " +
									 registeredScriptName + ".", ErrorType.SEMANTIC_ERROR);
				return false;
			}
			if (registeredScriptName.equals(function.getScriptName())) {
				logger.error("Two local functions with the same name ('" + registeredFunctionName + "')" +
									 " can't exist in the same script.", ErrorType.SEMANTIC_ERROR);
				return false;
			}
		}
		return true;
	}

	public static Optional<Function<?>> getFunctionByName(String name, String scriptName) {
		for (Function<?> registeredFunction : functions) {
			if (!registeredFunction.getName().equals(name)) continue; // we don't care then!!!! goodbye continue to the next one
			if (registeredFunction instanceof ScriptFunction<?> registeredScriptFunction
						&& registeredScriptFunction.isLocal()
						&& !scriptName.equals(registeredScriptFunction.getScriptName()))
				continue;
				//return Optional.of(registeredFunction); handled below
			return Optional.of(registeredFunction); // java function or global scriptfunction at this point
		}
		return Optional.empty();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isValidFunctionName(String name) {
		return FUNCTION_NAME_PATTERN.matcher(name).matches();
	}

}
