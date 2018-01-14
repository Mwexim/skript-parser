package io.github.syst3ms.skriptparser.classes;

import org.intellij.lang.annotations.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Manages all different types.
 */
public class TypeManager {
	private static Map<String, Type<?>> nameToType = new HashMap<>();

	/**
	 * Gets a {@link Type} by its exact name (the baseName parameter used in {@link Type#Type(Class, String, String)})
	 * @param name the name to get the Type from
	 * @return the corresponding Type, or {@literal null} if nothing matched
	 */
	public static Type<?> getByExactName(String name) {
		return nameToType.get(name);
	}

	/**
	 * Gets a {@link Type} using {@link Type#syntaxPattern}, which means this matches any alternate and/or plural form.
	 * @param name the name to get a Type from
	 * @return the matching Type, or {@literal null} if nothing matched
	 */
	public static Type<?> getByName(String name) {
		for (Type<?> t : nameToType.values()) {
			Matcher m = t.getSyntaxPattern().matcher(name);
			if (m.matches()) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Gets a {@link PatternType} from a name. This determines the number (single/plural) from the input.
	 * If the input happens to be the base name of a type, then a single PatternType (as in "not plural") of the corresponding type is returned.
	 * @param name the name input
	 * @return a corresponding PatternType, or {@literal null} if nothing matched
	 */
	public static PatternType<?> getPatternType(String name) {
		if (nameToType.containsKey(name)) { // Might as well avoid the for loop in this case
			return new PatternType<>(nameToType.get(name), false);
		}
		for (Type<?> t : nameToType.values()) {
			Matcher m = t.getSyntaxPattern().matcher(name);
			if (m.matches()) {
				String pluralGroup = m.group("plural");
				return new PatternType<>(t, pluralGroup == null);
			}
		}
		return null;
	}

	public static <T> void registerType(Class<T> c, String name, @Language("Regexp") String pattern) {
		nameToType.put(name, new Type<>(c, name, pattern));
	}
}