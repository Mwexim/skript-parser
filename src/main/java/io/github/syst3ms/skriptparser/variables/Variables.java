package io.github.syst3ms.skriptparser.variables;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A class handling operations on variables
 */
public class Variables {
    public static final String LIST_SEPARATOR = "::";
    public static final String LOCAL_VARIABLE_TOKEN = "_";
    public static final Pattern REGEX_PATTERN = Pattern.compile("\\{([^{}]|%\\{|}%)+}");
    private static final VariableMap variableMap = new VariableMap();
    // Yes, I know it should be trigger-specific, but I haven't got to that part yet, ok ? TODO make the change
    private static final Map<TriggerContext, VariableMap> localVariables = new HashMap<>();

    public static <T> Optional<? extends Expression<T>> parseVariable(String s, Class<? extends T> types, ParserState parserState, SkriptLogger logger) {
        s = s.strip();
        if (REGEX_PATTERN.matcher(s).matches()) {
            s = s.substring(1, s.length() - 1);
        } else {
            return Optional.empty();
        }
        if (!isValidVariableName(s, true, logger)) {
            return Optional.empty();
        }
        var vs = VariableString.newInstance(
                s.startsWith(LOCAL_VARIABLE_TOKEN) ? s.substring(LOCAL_VARIABLE_TOKEN.length()).strip() : s,
                parserState,
                logger
        );
        var finalS = s;
        return vs.map(v -> new Variable<>(v, finalS.startsWith(LOCAL_VARIABLE_TOKEN), finalS.endsWith(LIST_SEPARATOR + "*"), types));
    }

    /**
     * Checks whether a string is a valid variable name. This is used to verify variable names as well as command and function arguments.
     *
     * @param name The name to test
     * @param printErrors Whether to print errors when they are encountered
     * @param logger the logger
     * @return true if the name is valid, false otherwise.
     */
    public static boolean isValidVariableName(String name, boolean printErrors, SkriptLogger logger) {
        name = name.startsWith(LOCAL_VARIABLE_TOKEN) ? name.substring(LOCAL_VARIABLE_TOKEN.length()).strip()
			: name.strip();
        if (name.startsWith(LIST_SEPARATOR) || name.endsWith(LIST_SEPARATOR)) {
            if (printErrors) {
                logger.error("A variable name cannot start nor end with the list separator " + LIST_SEPARATOR, ErrorType.MALFORMED_INPUT);
            }
            return false;
        } else if (name.contains("*") && (name.indexOf("*") != name.length() - 1 || !name.endsWith(LIST_SEPARATOR + "*"))) {
            if (printErrors) {
                logger.error("A variable name cannot contain an asterisk outside of a list declaration", ErrorType.MALFORMED_INPUT);
            }
            return false;
        } else if (name.contains(LIST_SEPARATOR + LIST_SEPARATOR)) {
            if (printErrors) {
                logger.error("A variable name cannot contain two list separators stuck together", ErrorType.MALFORMED_INPUT);
            }
            return false;
        }
        return true;
    }

    /**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 *
	 * @param name the name of the variable
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
    public static Optional<Object> getVariable(String name, TriggerContext e, boolean local) {
        if (local) {
            var map = localVariables.get(e);
            if (map == null)
                return Optional.empty();
            return map.getVariable(name);
        } else {
            return variableMap.getVariable(name);
        }
    }

    /**
	 * Sets a variable.
	 *
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
    public static void setVariable(String name, @Nullable Object value, @Nullable TriggerContext e, boolean local) {
        if (local) {
            assert e != null : name;
            var map = localVariables.get(e);
            if (map == null)
                localVariables.put(e, map = new VariableMap());
            map.setVariable(name, value);
        } else {
            variableMap.setVariable(name, value);
        }
    }
}
