package io.github.syst3ms.skriptparser.classes;

import org.intellij.lang.annotations.Language;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a type, without any mention of single nor plural, which is handled by {@link }
 */
public class Type<T> {
	public static final Predicate<String> pluralGroupChecker = Pattern.compile("(?<!\\\\)\\(\\?<plural>[a-zA-Z]+\\)").asPredicate();
	private Class<T> c;
	private String baseName;
	private Pattern syntaxPattern;

	/**
	 *  @param c the class this type represents
	 * @param pattern the regex pattern this type should match.
	 *                Plural form should be contained in a group named {@literal plural}.
	 *                If the name has an irregular plural (i.e party -> parties), then use the following contruct : {@literal part(y|(?<plural>ies))}
	 * @exception PatternSyntaxException if the regex is invalid
	 */
	public Type(Class<T> c, String baseName, @Language("Regexp") String pattern) {
		this.c = c;
		this.baseName = baseName;
		pattern = pattern.trim();
		// Not handling exceptions here, developer responsability
		if (!pluralGroupChecker.test(pattern)) {
			syntaxPattern = Pattern.compile(pattern + "(?<plural>)??"); // Lazy optional group is required in this case
		} else {
			syntaxPattern = Pattern.compile(pattern);
		}
	}

	public Pattern getSyntaxPattern() {
		return syntaxPattern;
	}

	public Class<T> getC() {
		return c;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Type)) {
			return false;
		} else {
			Type<?> o = (Type<?>) obj;
			return c.equals(o.c) && baseName.equals(o.baseName) && syntaxPattern.pattern().equals(o.syntaxPattern.pattern());
		}
	}

	@Override
	public int hashCode() {
		return syntaxPattern.pattern().hashCode();
	}

	public String getBaseName() {
		return baseName;
	}
}