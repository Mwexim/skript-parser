package io.github.syst3ms.skriptparser.variables;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Variables {
	public static final String LIST_SEPARATOR = "::";
	public static final String LOCAL_VARIABLE_TOKEN = "_";
	private static final Pattern listSplitPattern = Pattern.compile(Pattern.quote(LIST_SEPARATOR));
	private static VariableMap variableMap = new VariableMap();
	// Yes, I know it should be trigger-specific, but I haven't got to that part yet, ok ? TODO make the change
	private static Map<Event, VariableMap> localVariables = new HashMap<>();

	private static String[] splitList(String name) {
		return listSplitPattern.split(name);
	}

	public static Expression<?> parseVariable(String s) {
		if (!s.startsWith("{") || !s.endsWith("}")) {
			return null;
		}
		String name = StringUtils.getEnclosedText(s, '{', '}', 0);
		assert name != null;
		// Check if only a part of the string is enclosed
		if (name.length() < s.length() - 2) {
			return null;
		}
		return null;
	}

	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 *
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	public static Object getVariable(final String name, final Event e, final boolean local) {
		if (local) {
			final VariableMap map = localVariables.get(e);
			if (map == null)
				return null;
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
	public static void setVariable(final String name, Object value, final Event e, final boolean local) {
		if (local) {
			assert e != null : name;
			VariableMap map = localVariables.get(e);
			if (map == null)
				localVariables.put(e, map = new VariableMap());
			map.setVariable(name, value);
		} else {
			variableMap.setVariable(name, value);
		}
	}
}
