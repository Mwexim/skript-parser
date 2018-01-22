package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.classes.PatternType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Manages all different types.
 */
public class TypeManager {
	private static final TypeManager instance = new TypeManager();
	private Map<String, Type<?>> nameToType = new HashMap<>();
	private Map<Class<?>, Type<?>> classToType = new HashMap<>();
	private TypeManager(){}

	public static TypeManager getInstance() {
		return instance;
	}

	/**
	 * Gets a {@link Type} by its exact name (the baseName parameter used in {@link Type#Type(Class, String, String)})
	 * @param name the name to get the Type from
	 * @return the corresponding Type, or {@literal null} if nothing matched
	 */
	public Type<?> getByExactName(String name) {
		return nameToType.get(name);
	}

	/**
	 * Gets a {@link Type} using {@link Type#syntaxPattern}, which means this matches any alternate and/or plural form.
	 * @param name the name to get a Type from
	 * @return the matching Type, or {@literal null} if nothing matched
	 */
	public Type<?> getByName(String name) {
		for (Type<?> t : nameToType.values()) {
			Matcher m = t.getSyntaxPattern().matcher(name);
			if (m.matches()) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Gets a {@link Type} from its associated {@link Class}.
	 * @param c the Class to get the Type from
	 * @param <T> the underlying type of the Class and the returned Type
	 * @return the associated Type, or {@literal null}
	 */
	@SuppressWarnings("unchecked")
	public <T> Type<T> getByClass(Class<T> c) {
		return (Type<T>) classToType.get(c);
	}

	/**
	 * Gets a {@link PatternType} from a name. This determines the number (single/plural) from the input.
	 * If the input happens to be the base name of a type, then a single PatternType (as in "not plural") of the corresponding type is returned.
	 * @param name the name input
	 * @return a corresponding PatternType, or {@literal null} if nothing matched
	 */
	public PatternType<?> getPatternType(String name) {
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

	void register(SkriptRegistration reg) {
		for (Type<?> type : reg.getTypes()) {
			nameToType.put(type.getBaseName(), type);
			classToType.put(type.getC(), type);
		}
	}
}